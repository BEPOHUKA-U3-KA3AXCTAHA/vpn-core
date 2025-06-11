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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Находим старые ключи
        List<VPNKey> oldKeys = vpnKeyRepository.findAll().stream()
                .filter(k -> k.getCreatedAt().isBefore(cutoffDate))
                .toList();

        if (!oldKeys.isEmpty()) {
            // Удаляем конфигурацию из WireGuard для каждого старого ключа
            for (VPNKey key : oldKeys) {
                String config = key.getKeyData(); // Получаем полный конфиг
                String privateKey = extractPrivateKey(config); // Извлекаем приватный ключ клиента из конфигурации

                if (privateKey != null) {
                    // Генерируем публичный ключ из приватного
                    String publicKey = generatePublicKeyFromPrivate(privateKey);
                    try {
                        // Выполняем команду для удаления клиента из конфигурации WireGuard
                        exec("sudo wg set wg0 peer " + publicKey.trim() + " remove");
                        System.out.println("Удален клиент с публичным ключом: " + publicKey);

                        // Удаляем конфигурационный файл клиента из папки configs
                        String username = key.getUser().getEmail(); // Предполагаем, что у ключа есть имя пользователя
                        Path configFilePath = Path.of("configs", username + "_vpn_config.conf");

                        // Удаляем файл, если он существует
                        if (Files.exists(configFilePath)) {
                            Files.delete(configFilePath);
                            System.out.println("Конфигурационный файл удален: " + configFilePath);
                        }

                        // Удаляем ключ из базы данных (по одному)
                        vpnKeyRepository.delete(key);
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Не удалось удалить конфигурацию для ключа: " + publicKey);
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Не удалось извлечь публичный ключ из конфига для пользователя.");
                }
            }
        }
    }

    // Метод для извлечения приватного ключа из конфигурации
    private String extractPrivateKey(String config) {
        // Регулярное выражение для извлечения приватного ключа
        String privateKeyPattern = "PrivateKey = ([A-Za-z0-9+/=]+)";
        Pattern pattern = Pattern.compile(privateKeyPattern);
        Matcher matcher = pattern.matcher(config);

        if (matcher.find()) {
            return matcher.group(1); // Возвращаем найденный приватный ключ
        }

        return null; // Если приватный ключ не найден
    }

    // Метод для генерации публичного ключа из приватного
    private String generatePublicKeyFromPrivate(String privateKey) {
        try {
            // Используем команду wg pubkey для генерации публичного ключа
            String publicKey = execWithInput("wg pubkey", privateKey.trim());
            return publicKey.trim(); // Возвращаем публичный ключ
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Ошибка при генерации публичного ключа");
        }

        return null; // В случае ошибки
    }

    private String exec(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command.split(" "))
                .redirectErrorStream(true)
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Ошибка выполнения команды: " + command);
        }

        return new String(process.getInputStream().readAllBytes());
    }

    private String execWithInput(String command, String input) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("bash", "-c", command).start();
        try (var os = process.getOutputStream()) {
            os.write(input.getBytes());
            os.flush();
        }
        process.waitFor();
        return new String(process.getInputStream().readAllBytes());
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
