package com.vpnservice.service.admin;

import com.vpnservice.config.ConfigKeys;
import com.vpnservice.exception.NotFoundException;
import com.vpnservice.model.Admin;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.repository.AdminRepository;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.VPNKeyRepository;
import com.vpnservice.repository.VPNSettingsRepository;
import com.vpnservice.service.vpnkey.VPNKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VPNKeyRepository vpnKeyRepository;

    @Autowired
    private VPNSettingsRepository vpnSettingsRepository;

    @Autowired
    private VPNKeyService vpnKeyService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Admin login(String email, String password) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            // Сравниваем хэш пароля
            if (passwordEncoder.matches(password, admin.getPasswordHash())) {
                return admin;
            } else {
                throw new RuntimeException("Неверный пароль");
            }
        } else {
            throw new RuntimeException("Админ с таким email не найден");
        }
    }

    @Override
    public void setVPNPrice(Double price) {
        String priceAsString = String.valueOf(price);

        var settingsOpt = vpnSettingsRepository.findByKey(ConfigKeys.VPN_PRICE);

        if (settingsOpt.isEmpty()) {
            throw new IllegalStateException("VPN_PRICE setting is missing in the database");
        }

        var settings = settingsOpt.get();
        settings.setValue(priceAsString);
        vpnSettingsRepository.save(settings);
    }

    @Override
    public void deleteUserVPN(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        VPNKey vpnKey = vpnKeyRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("У пользователя нет VPN ключа"));

        vpnKeyRepository.deleteById(vpnKey.getId());
    }

    @Override
    public void createVPNKeyForUser(Long userId) {
        // Создание нового ключа VPN для пользователя
        // Проверяем, есть ли у пользователя VPN-ключ
        Optional<VPNKey> existingKeyOpt = vpnKeyService.getKeyByUser(userId);
        if (existingKeyOpt.isPresent()) {
            // Если ключ есть, обновляем дату
            VPNKey existingKey = existingKeyOpt.get();
            existingKey.setCreatedAt(java.time.LocalDateTime.now());
            vpnKeyService.updateKey(existingKey);
        } else {
            // Если ключа нет, создаём новый
            vpnKeyService.generateKey(userId);
        }
    }
}
