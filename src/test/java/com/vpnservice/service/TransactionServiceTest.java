package com.vpnservice.service;

import com.vpnservice.model.Transaction;
import com.vpnservice.model.User;
import com.vpnservice.repository.TransactionRepository;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.service.transaction.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;  // Тестируемый сервис

    @Mock
    private TransactionRepository transactionRepository;  // Мок репозитория транзакций

    @Mock
    private UserRepository userRepository;  // Мок репозитория пользователей

    private User user;  // Пользователь для тестирования

    @BeforeEach
    void setUp() {
        // Создание тестового пользователя
        user = new User();
        user.setEmail("user@example.com");
        user.setBalance(100.0);  // Начальный баланс пользователя
    }

    @Test
    void testProcessTopUpSuccess() {
        // Мокаем метод findByEmail для возврата пользователя
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // Мокаем сохранение транзакции
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // Вызов метода пополнения баланса
        Transaction transaction = transactionService.processTopUp("user@example.com", 50.0);

        // Проверяем, что баланс пользователя увеличился на указанную сумму
        assertEquals(150.0, user.getBalance(), "Баланс пользователя должен увеличиться на 50.0");

        // Проверяем, что транзакция была сохранена
        verify(transactionRepository).save(any(Transaction.class));

        // Проверяем, что баланс пользователя был обновлен в репозитории
        verify(userRepository).save(user);
    }

    @Test
    void testProcessTopUpFailureInvalidAmount() {
        // Попытка пополнить баланс с некорректной суммой (например, нулевой или отрицательной)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.processTopUp("user@example.com", -50.0)
        );
        assertEquals("Сумма пополнения должна быть положительной", exception.getMessage(), "Должно быть выброшено исключение с правильным сообщением");
    }

    @Test
    void testProcessTopUpFailureUserNotFound() {
        // Мокаем отсутствие пользователя в базе данных
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        // Попытка пополнить баланс для несуществующего пользователя
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.processTopUp("user@example.com", 50.0)
        );
        assertEquals("Пользователь не найден: user@example.com", exception.getMessage(), "Должно быть выброшено исключение с правильным сообщением");
    }

    @Test
    void testGetTransactionsByUserSuccess() {
        // Мокаем метод findByEmail для возврата пользователя
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // Мокаем репозиторий транзакций, чтобы он возвращал список транзакций
        when(transactionRepository.findByUser(user, null)).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(new Transaction())));

        // Вызов метода получения транзакций
        var transactions = transactionService.getTransactionsByUser("user@example.com");

        // Проверяем, что транзакции были получены
        assertNotNull(transactions, "Транзакции не должны быть пустыми");
        assertFalse(transactions.isEmpty(), "Список транзакций не должен быть пустым");
    }

    @Test
    void testGetTransactionsByUserFailureUserNotFound() {
        // Мокаем отсутствие пользователя
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        // Попытка получения транзакций для несуществующего пользователя
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionsByUser("user@example.com")
        );
        assertEquals("Пользователь не найден: user@example.com", exception.getMessage(), "Должно быть выброшено исключение с правильным сообщением");
    }
}
