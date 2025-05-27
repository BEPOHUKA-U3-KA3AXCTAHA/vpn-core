package com.vpnservice.controller;

import com.vpnservice.model.VPNKey;
import com.vpnservice.service.vpnkey.VPNKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vpn")
public class VPNKeyController {

    @Autowired
    private VPNKeyService vpnKeyService;

    @GetMapping("/config")
    public String getVPNConfig() {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return vpnKeyService.getVPNConfig(currentUser.getUsername());
    }

    @PostMapping("/revoke-old")
    public void revokeOldKeys() {
        vpnKeyService.revokeOldKeys();
    }
}
