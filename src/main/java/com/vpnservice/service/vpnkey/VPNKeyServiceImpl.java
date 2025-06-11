package com.vpnservice.service.vpnkey;

import com.vpnservice.config.ConfigKeys;
import com.vpnservice.config.RabbitMQConfig;
import com.vpnservice.exception.NotFoundException;
import com.vpnservice.messaging.VpnKeyGenerationRequest;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.VPNKeyRepository;
import com.vpnservice.repository.VPNSettingsRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VPNKeyServiceImpl implements VPNKeyService {


    @Autowired
    private VPNKeyRepository vpnKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VPNSettingsRepository vpnSettingsRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void generateKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        rabbitTemplate.convertAndSend(RabbitMQConfig.VPN_QUEUE,
                new VpnKeyGenerationRequest(userId, user.getEmail()));
    }

    @Override
    public void revokeKey(Long keyId) {
        if (!vpnKeyRepository.existsById(keyId)) {
            throw new IllegalArgumentException("VPN ключ не найден: " + keyId);
        }
        vpnKeyRepository.deleteById(keyId);
    }

    public void notifyExpiringKeys() {
        int monthsLimit = getKeyLifetimeMonths();
        LocalDateTime now = LocalDateTime.now();

        List<VPNKey> keys = vpnKeyRepository.findAll();

        for (VPNKey key : keys) {
            if (key.getNotifiedAboutExpiry()) continue;

            LocalDateTime expirationDate = key.getCreatedAt().plusMonths(monthsLimit);

            long daysUntilExpiry = java.time.Duration.between(now, expirationDate).toDays();

            if (daysUntilExpiry <= getKeyDaysBeforeNotifyExpiringKeys() && daysUntilExpiry >= 0) {
                User user = key.getUser();
                String email = user.getEmail();
                String subject = "Ваш VPN ключ скоро истекает";
                String message = String.format(
                        "Здравствуйте, %s!\n\nВаш VPN ключ истекает через %d дней — %s.\nПожалуйста, обновите или сгенерируйте новый ключ при необходимости.",
                        user.getEmail(),
                        daysUntilExpiry,
                        expirationDate.toLocalDate()
                );

                sendEmail(email, subject, message);

                key.setNotifiedAboutExpiry(true);
                vpnKeyRepository.save(key);
            }
        }
    }

    private int getKeyDaysBeforeNotifyExpiringKeys() {
        return vpnSettingsRepository.findByKey(ConfigKeys.DAYS_BEFORE_NOTIFY_EXPIRING_KEYS)
                .map(setting -> Integer.parseInt(setting.getValue()))
                .orElseThrow(() -> new IllegalArgumentException("DAYS_BEFORE_NOTIFY_EXPIRING_KEYS setting is missing in the database"));
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

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Не удалось отправить email: " + e.getMessage());
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
