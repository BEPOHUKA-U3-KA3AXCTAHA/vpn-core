package com.vpnservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpnservice.dto.UserRegistrationRequest;
import com.vpnservice.model.User;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")  // Указываем использовать тестовый профиль
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private static final String REGISTER_URL = "/register";

    @BeforeEach
    public void setup() {
        // Перед каждым тестом очищаем базу данных
        userRepository.deleteAll();
    }

    @AfterEach
    public void resetDb() {
        // После каждого теста очищаем базу данных
        userRepository.deleteAll();
    }

    @Test
    public void testRegisterSuccess() {
        // Создаем тестовый запрос
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("testuser@example.com");
        request.setPassword("securePassword123");

        // Выполняем POST запрос
        ResponseEntity<Map> response = restTemplate.exchange(
                REGISTER_URL,
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(request),
                Map.class
        );

        // Проверяем, что статус ответа OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Проверяем, что ответ содержит правильное сообщение
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo("ok");
        assertThat(responseBody.get("message")).isEqualTo("операция прошла успешно");

        // Проверяем, что пользователь действительно создан в базе
        User user = userRepository.findByEmail("testuser@example.com")
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("testuser@example.com");
    }
}
