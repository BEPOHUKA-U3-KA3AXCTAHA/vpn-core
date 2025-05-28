package com.vpnservice.controller;

import com.vpnservice.service.vpnkey.VPNKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vpn")
public class VPNKeyController {

    @Autowired
    private VPNKeyService vpnKeyService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getVPNConfig() {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String config = vpnKeyService.getVPNConfig(currentUser.getUsername());


        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("config", config);

        return ResponseEntity.ok(response);
    }
}
