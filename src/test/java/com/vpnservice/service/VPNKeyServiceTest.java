package com.vpnservice.service;

import com.vpnservice.exception.NotFoundException;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.VPNKeyRepository;
import com.vpnservice.repository.VPNSettingsRepository;
import com.vpnservice.service.vpnkey.VPNKeyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VPNKeyServiceTest {

    @InjectMocks
    private VPNKeyServiceImpl vpnKeyService;  // Тестируемый сервис

    @Mock
    private VPNKeyRepository vpnKeyRepository;  // Мок репозитория VPN-ключей

    @Mock
    private UserRepository userRepository;  // Мок репозитория пользователей

    @Mock
    private VPNSettingsRepository vpnSettingsRepository;  // Мок репозитория настроек VPN

    private User user;  // Пользователь для тестирования

    private VPNKey vpnKey;  // VPN ключ для тестирования

    @BeforeEach
    void setUp() {
        // Создание тестового пользователя
        user = new User();
        user.setEmail("user@example.com");

        // Создание тестового VPN ключа
        vpnKey = new VPNKey();
        vpnKey.setUser(user);
        vpnKey.setKeyData("testKeyData");
        vpnKey.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGenerateKeySuccess() {
        // Мокаем возвращение пользователя
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Мокаем сохранение VPN ключа
        when(vpnKeyRepository.save(any(VPNKey.class))).thenReturn(vpnKey);

        // Вызов метода генерации ключа
        vpnKeyService.generateKey(1L);

        // Проверяем, что ключ был сохранен
        verify(vpnKeyRepository).save(any(VPNKey.class));
    }

    @Test
    void testGenerateKeyFailureUserNotFound() {
        // Мокаем отсутствие пользователя
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Попытка генерации ключа для несуществующего пользователя
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vpnKeyService.generateKey(1L)
        );
        assertEquals("Пользователь не найден: 1", exception.getMessage(), "Должно быть выброшено исключение с правильным сообщением");
    }

    @Test
    void testRevokeKeySuccess() {
        // Мокаем существование ключа
        when(vpnKeyRepository.existsById(1L)).thenReturn(true);

        // Вызов метода отзыва ключа
        vpnKeyService.revokeKey(1L);

        // Проверяем, что ключ был удален
        verify(vpnKeyRepository).deleteById(1L);
    }

    @Test
    void testRevokeKeyFailureKeyNotFound() {
        // Мокаем отсутствие ключа
        when(vpnKeyRepository.existsById(1L)).thenReturn(false);

        // Попытка отзыва несуществующего ключа
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vpnKeyService.revokeKey(1L)
        );
        assertEquals("VPN ключ не найден: 1", exception.getMessage(), "Должно быть выброшено исключение с правильным сообщением");
    }

    @Test
    void testGetKeyByUserSuccess() {
        // Мокаем возвращение пользователя
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Мокаем возвращение ключа для пользователя
        when(vpnKeyRepository.findByUser(user)).thenReturn(Optional.of(vpnKey));

        // Вызов метода получения ключа
        Optional<VPNKey> result = vpnKeyService.getKeyByUser(1L);

        // Проверяем, что ключ был найден
        assertTrue(result.isPresent(), "VPN ключ должен быть найден");
        assertEquals(vpnKey, result.get(), "Должен быть возвращен правильный ключ");
    }

    @Test
    void testGetKeyByUserFailureUserNotFound() {
        // Мокаем отсутствие пользователя
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Попытка получения ключа для несуществующего пользователя
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vpnKeyService.getKeyByUser(1L)
        );
        assertEquals("Пользователь не найден: 1", exception.getMessage(), "Должно быть выброшено исключение с правильным сообщением");
    }

    @Test
    void testGetVPNConfigSuccess() {
        // Мокаем возвращение ключа по имени пользователя
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(vpnKeyRepository.findByUser(user)).thenReturn(Optional.of(vpnKey));

        // Вызов метода получения конфигурации VPN
        String config = vpnKeyService.getVPNConfig("user@example.com");

        // Проверяем, что конфигурация была получена
        assertEquals("testKeyData", config, "Конфигурация должна соответствовать ожидаемой");
    }

    @Test
    void testGetVPNConfigFailureKeyNotFound() {
        // Мокаем отсутствие ключа
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(vpnKeyRepository.findByUser(user)).thenReturn(Optional.empty());

        // Попытка получения конфигурации для пользователя без ключа
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                vpnKeyService.getVPNConfig("user@example.com")
        );
        assertEquals("VPN ключ не найден для пользователя с id: user@example.com", exception.getMessage(), "Должно быть выброшено исключение с правильным сообщением");
    }
}
