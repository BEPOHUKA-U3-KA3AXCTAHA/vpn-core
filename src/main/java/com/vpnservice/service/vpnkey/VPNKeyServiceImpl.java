package com.vpnservice.service.vpnkey;

import com.vpnservice.config.ConfigKeys;
import com.vpnservice.exception.NotFoundException;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.VPNKeyRepository;
import com.vpnservice.repository.VPNSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class VPNKeyServiceImpl implements VPNKeyService {


    @Autowired
    private VPNKeyRepository vpnKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VPNSettingsRepository vpnSettingsRepository;

    @Override
    public void generateKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        String configData = generateWireGuardConfigForUser(user);

        VPNKey key = new VPNKey();
        key.setUser(user);
        key.setKeyData(configData);

        vpnKeyRepository.save(key);
    }

    @Override
    public void revokeKey(Long keyId) {
        if (!vpnKeyRepository.existsById(keyId)) {
            throw new IllegalArgumentException("VPN ключ не найден: " + keyId);
        }
        vpnKeyRepository.deleteById(keyId);
    }

    private int getKeyLifetimeMonths() {
        return vpnSettingsRepository.findByKey(ConfigKeys.VPN_KEY_LIFETIME_MONTHS)
                .map(setting -> Integer.parseInt(setting.getValue()))
                .orElseThrow(() -> new IllegalArgumentException("VPN_KEY_LIFETIME_MONTHS setting is missing in the database"));
    }

    @Override
    public void revokeOldKeys() {
        int monthsLimit = getKeyLifetimeMonths();
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(monthsLimit);

        List<VPNKey> oldKeys = vpnKeyRepository.findAll().stream()
                .filter(k -> k.getCreatedAt().isBefore(cutoffDate))
                .toList();

        if (!oldKeys.isEmpty()) {
            vpnKeyRepository.deleteAll(oldKeys);
        }
    }

    public Optional<VPNKey> getKeyByUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        return vpnKeyRepository.findByUser(user);
    }

    public Optional<VPNKey> getKeyByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));

        return vpnKeyRepository.findByUser(user);
    }

    private String generateWireGuardConfigForUser(User user) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("vpnconfig")) {
            props.load(fis);
            return props.getProperty("vpn.key");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении конфигурации", e);
        }
    }

    @Override
    public void updateKey(VPNKey key) {
        vpnKeyRepository.save(key);
    }

    @Override
    public String getVPNConfig(String username) {
        VPNKey vpnKey = getKeyByUsername(username)
                .orElseThrow(() -> new NotFoundException("VPN ключ не найден для пользователя с id: " + username));

        return vpnKey.getKeyData();
    }
}
