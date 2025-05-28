package com.vpnservice.controller;

import com.vpnservice.service.transaction.TransactionService;
import com.vpnservice.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/balance")
    public Double getBalance() {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userService.findByUsername(currentUser.getUsername()).getBalance();
    }

    @PostMapping("/topup")
    public ResponseEntity<Map<String, Object>> topUp(@RequestParam Double amount) {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        transactionService.processTopUp(currentUser.getUsername(), amount);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "операция прошла успешно");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyVPN() {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        userService.buyVPNAccess(currentUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "операция прошла успешно");

        return ResponseEntity.ok(response);
    }
}
