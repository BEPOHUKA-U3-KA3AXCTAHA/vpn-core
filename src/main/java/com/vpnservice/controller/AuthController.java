package com.vpnservice.controller;

import com.vpnservice.dto.UserRegistrationRequest;
import com.vpnservice.model.User;
import com.vpnservice.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRegistrationRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        userService.register(new User(email, password));
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "операция прошла успешно");

        return ResponseEntity.ok(response);
    }
}