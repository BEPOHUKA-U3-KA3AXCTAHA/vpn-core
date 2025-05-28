package com.vpnservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpnservice.config.TestSecurityConfig;
import com.vpnservice.dto.UserResponse;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.repository.VPNKeyRepository;
import com.vpnservice.service.admin.AdminService;
import com.vpnservice.service.vpnkey.VPNKeyService;
import com.vpnservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")  // Указываем использовать тестовый профиль
public class AdminControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AdminService adminService;

    @Autowired
    private VPNKeyService vpnKeyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VPNKeyRepository vpnKeyRepository;


    private User testUser;

    @BeforeEach
    public void setup() {
        // Создаём тестового пользователя через репозиторий
        testUser = new User("test@example.com", "hashed_password");
        userRepository.save(testUser); // Сохраняем пользователя в базу данных

        VPNKey key = new VPNKey(testUser, "vpn_data");
        vpnKeyRepository.save(key);
    }

    @AfterEach
    public void resetDb() {
        userRepository.deleteAll();
    }

    @Test
    public void testSetVPNPrice() {
        // Проверяем установку цены
        Double price = 10.0;
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/set-price?price=" + price, HttpMethod.POST, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testDeleteUserVPN() {
        // Проверяем удаление VPN
        Long userId = testUser.getId();
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/vpn/" + userId, HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testCreateKey() {
        // Проверяем создание ключа
        Long userId = testUser.getId();
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/admin/vpn/" + userId + "/create", HttpMethod.POST, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetUsers() {
        // Выполняем запрос к контроллеру
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/users",
                HttpMethod.GET,
                null,
                String.class
        );

        // Проверяем, что статус ответа OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Попробуем десериализовать вручную строку в список объектов UserResponse
        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        // Пробуем вручную десериализовать JSON в объект
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Используем параметризованный тип для списка UserResponse
            List<UserResponse> users = objectMapper.readValue(responseBody, new TypeReference<List<UserResponse>>(){});
            assertThat(users).isNotEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error deserializing response", e);
        }
    }

    @Test
    public void testRevokeOldKeys() {
        // Пример для старых ключей
        restTemplate.postForEntity("/api/admin/revoke-old", null, Void.class);
    }
}
