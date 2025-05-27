package com.vpnservice.service;

import com.vpnservice.exception.NotFoundException;
import com.vpnservice.model.Admin;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.model.VPNSettings;
import com.vpnservice.repository.AdminRepository;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.VPNKeyRepository;
import com.vpnservice.repository.VPNSettingsRepository;
import com.vpnservice.service.admin.AdminServiceImpl;
import com.vpnservice.service.vpnkey.VPNKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminServiceImpl adminService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VPNKeyRepository vpnKeyRepository;

    @Mock
    private VPNSettingsRepository vpnSettingsRepository;

    @Mock
    private VPNKeyService vpnKeyService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private Admin admin;
    private User user;
    private VPNKey vpnKey;

    @BeforeEach
    void setUp() {
        admin = new Admin();
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("$2a$10$W1u4YxM/qXgFvGHJ7z3vTObQRa6lZW9v4DJ7BaKVmVkOObT1iPFSq");

        user = new User();
        user.setEmail("user@example.com");

        vpnKey = new VPNKey();
        vpnKey.setUser(user);
    }

    @Test
    void testLoginFailureAdminNotFound() {
        when(adminRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.login("admin@example.com", "password"));
        assertEquals("Админ с таким email не найден", exception.getMessage());
    }

    @Test
    void testSetVPNPriceSuccess() {
        when(vpnSettingsRepository.findByKey("vpn_price")).thenReturn(Optional.of(new VPNSettings("vpn_price", "100")));

        adminService.setVPNPrice(200.0);

        verify(vpnSettingsRepository).save(any(VPNSettings.class));
    }

    @Test
    void testSetVPNPriceFailure() {
        when(vpnSettingsRepository.findByKey("vpn_price")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> adminService.setVPNPrice(200.0));
        assertEquals("VPN_PRICE setting is missing in the database", exception.getMessage());
    }

    @Test
    void testDeleteUserVPNSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vpnKeyRepository.findByUser(user)).thenReturn(Optional.of(vpnKey));

        adminService.deleteUserVPN(1L);

        verify(vpnKeyRepository).deleteById(vpnKey.getId());
    }

    @Test
    void testDeleteUserVPNFailureUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> adminService.deleteUserVPN(1L));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDeleteUserVPNFailureVPNKeyNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vpnKeyRepository.findByUser(user)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.deleteUserVPN(1L));
        assertEquals("У пользователя нет VPN ключа", exception.getMessage());
    }

    @Test
    void testCreateVPNKeyForUserCreateNewKey() {
        when(vpnKeyService.getKeyByUser(1L)).thenReturn(Optional.empty());

        adminService.createVPNKeyForUser(1L);

        verify(vpnKeyService).generateKey(1L);
    }

    @Test
    void testCreateVPNKeyForUserUpdateExistingKey() {
        when(vpnKeyService.getKeyByUser(1L)).thenReturn(Optional.of(vpnKey));

        adminService.createVPNKeyForUser(1L);

        verify(vpnKeyService).updateKey(vpnKey);
    }
}
