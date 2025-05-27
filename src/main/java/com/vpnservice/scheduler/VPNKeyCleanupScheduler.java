package com.vpnservice.scheduler;

import com.vpnservice.service.vpnkey.VPNKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VPNKeyCleanupScheduler {

    @Autowired
    private VPNKeyService vpnKeyService;

    // Запускается раз в сутки (86400000 мс)
    @Scheduled(fixedRate = 8000)
    public void cleanupOldKeys() {
        vpnKeyService.revokeOldKeys();
    }
}
