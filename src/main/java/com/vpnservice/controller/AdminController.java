package com.vpnservice.controller;

import com.vpnservice.dto.UserResponse;
import com.vpnservice.model.User;
import com.vpnservice.service.admin.AdminService;
import com.vpnservice.service.vpnkey.VPNKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "Админское управление VPN")
@Validated
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private VPNKeyService vpnKeyService;

    @Operation(summary = "Установить цену VPN")
    @PostMapping("/set-price")
    public ResponseEntity<Void> setVPNPrice(
            @RequestParam @NotNull @Min(0) Double price) {
        adminService.setVPNPrice(price);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить VPN пользователя")
    @DeleteMapping("/vpn/{userId}")
    public ResponseEntity<Void> deleteUserVPN(
            @PathVariable @NotNull @Min(1) Long userId) {
        adminService.deleteUserVPN(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Создать ключ для пользователя")
    @PostMapping("/vpn/{userId}/create")
    public ResponseEntity<Void> createKey(
            @PathVariable @NotNull @Min(1) Long userId) {
        adminService.createVPNKeyForUser(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Все пользователи")
    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        List<User> users = adminService.getUsersList();

        return users.stream()
                .map(tx -> new UserResponse(
                        tx.getEmail(),
                        tx.getId()))
                .toList();
    }

    @Operation(summary = "Удалить просроченные ключи у юзеров")
    @PostMapping("/revoke-old")
    public void revokeOldKeys() {
        vpnKeyService.revokeOldKeys();
    }
}
