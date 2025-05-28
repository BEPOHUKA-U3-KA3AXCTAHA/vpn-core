package com.vpnservice.service;

import com.vpnservice.exception.NotEnoughBalanceException;
import com.vpnservice.exception.NotFoundException;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.model.VPNSettings;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.VPNSettingsRepository;
import com.vpnservice.service.transaction.TransactionService;
import com.vpnservice.service.user.UserServiceImpl;
import com.vpnservice.service.vpnkey.VPNKeyService;
import com.vpnservice.config.ConfigKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VPNKeyService vpnKeyService;

    @Mock
    private VPNSettingsRepository vpnSettingsRepository;

    private User user;
    private VPNKey vpnKey;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("$2a$10$W1u4YxM/qXgFvGHJ7z3vTObQRa6lZW9v4DJ7BaKVmVkOObT1iPFSq");
        user.setBalance(100.0);

        vpnKey = new VPNKey();
        vpnKey.setUser(user);
    }

    @Test
    void testRegisterUserSuccess() {
        when(userRepository.save(user)).thenReturn(user);

        User registeredUser = userService.register(user);

        assertNotNull(registeredUser);
        assertEquals("user@example.com", registeredUser.getEmail());
        assertNotNull(registeredUser.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void testBuyVPNAccessSuccess() {
        // Mocking dependencies
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(vpnSettingsRepository.findByKey(ConfigKeys.VPN_PRICE)).thenReturn(Optional.of(new VPNSettings("vpn_price", "50")));
        when(vpnKeyService.getKeyByUser(user.getId())).thenReturn(Optional.empty());

        userService.buyVPNAccess("user@example.com");

        assertEquals(50.0, user.getBalance());
        verify(vpnKeyService).generateKey(user.getId());
        verify(userRepository).save(user);
    }

    @Test
    void testBuyVPNAccessFailureNotEnoughBalance() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(vpnSettingsRepository.findByKey(ConfigKeys.VPN_PRICE)).thenReturn(Optional.of(new VPNSettings("vpn_price", "150")));

        NotEnoughBalanceException exception = assertThrows(NotEnoughBalanceException.class, () -> userService.buyVPNAccess("user@example.com"));
        assertEquals("Недостаточно средств", exception.getMessage());
    }

    @Test
    void testBuyVPNAccessFailureUserNotFound() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.buyVPNAccess("user@example.com"));
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void testFindByUsernameSuccess() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.findByUsername("user@example.com");

        assertNotNull(foundUser);
        assertEquals("user@example.com", foundUser.getEmail());
    }

    @Test
    void testFindByUsernameFailure() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findByUsername("user@example.com"));
        assertEquals("Пользователь не найден", exception.getMessage());
    }
}
