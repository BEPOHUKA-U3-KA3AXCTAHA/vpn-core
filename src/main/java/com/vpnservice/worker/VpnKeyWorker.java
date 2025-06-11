package com.vpnservice.worker;

import com.vpnservice.config.RabbitMQConfig;
import com.vpnservice.messaging.VpnKeyGenerationRequest;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.VPNKeyRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class VpnKeyWorker {

    private final UserRepository userRepository;
    private final VPNKeyRepository vpnKeyRepository;
    private final JavaMailSender mailSender;

    public VpnKeyWorker(UserRepository userRepository,
                        VPNKeyRepository vpnKeyRepository,
                        JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.vpnKeyRepository = vpnKeyRepository;
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = RabbitMQConfig.VPN_QUEUE)
    public void handleVpnKeyRequest(VpnKeyGenerationRequest request) throws IOException, InterruptedException {
        try {
            Thread.sleep(3000); // пауза 3 секунды
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("🔥 Генерация VPN ключа для: " + request.getEmail());

        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("Пользователь не найден: " + request.getUserId());
        }

        User user = userOpt.get();

        String config = generateWireGuardConfig(user);

        VPNKey key = new VPNKey();
        key.setUser(user);
        key.setKeyData(config);
        key.setCreatedAt(LocalDateTime.now());
        sendEmail(user.getEmail(), "Ваш VPN-конфиг готов!", config);

        System.out.println("Конфигурация создана и отправлена: " + user.getEmail());
        vpnKeyRepository.save(key);
        saveConfigToFile(config, user.getEmail());
    }

    private String generateWireGuardConfig(User user) throws IOException, InterruptedException {
        // Генерация приватного ключа клиента
        String clientPrivateKey = exec("wg genkey");

        // Генерация публичного ключа клиента
        String clientPublicKey = execWithInput("wg pubkey", clientPrivateKey);

        // Назначаем IP-адрес клиенту
        String clientIp = "10.0.0." + (int) (Math.random() * 100 + 2);

        // 🔐 Добавляем клиента на сервер
        exec("sudo wg set wg0 peer %s allowed-ips %s/32".formatted(clientPublicKey.trim(), clientIp));

        // 📄 Генерим клиентский конфиг
        String serverPublicKey = execWithInput("wg pubkey", readFile("wireguard-secrets/server_private_key")).trim();

        return """
            [Interface]
            PrivateKey = %s
            Address = %s/32
            DNS = 1.1.1.1

            [Peer]
            PublicKey = %s
            Endpoint = %s:51820
            AllowedIPs = 0.0.0.0/0
            PersistentKeepalive = 25
            """.formatted(
                clientPrivateKey.trim(),
                clientIp,
                serverPublicKey,
                "vpn.example.com" // заменишь на свой IP или домен
        );
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private void saveConfigToFile(String configData, String username) {
        Path directoryPath = Path.of("configs");

        // Проверяем, если директория не существует, создаем её
        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Путь к файлу конфигурации для конкретного пользователя
            Path filePath = directoryPath.resolve(username + "_vpn_config.conf");

            // Сохраняем данные конфигурации в файл
            Files.writeString(filePath, configData);
            System.out.println("Конфигурация сохранена в файл: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Ошибка при сохранении конфигурации в файл.");
        }
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

    private String readFile(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

}
