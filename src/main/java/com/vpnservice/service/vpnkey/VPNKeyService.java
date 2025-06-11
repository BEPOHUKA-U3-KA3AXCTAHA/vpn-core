package com.vpnservice.service.vpnkey;

import com.vpnservice.exception.NotFoundException;
import com.vpnservice.model.VPNKey;

import java.util.Optional;

public interface VPNKeyService {
    void generateKey(Long userId);
    void revokeKey(Long keyId);
    void notifyExpiringKeys();
    void revokeOldKeys();
    Optional<VPNKey> getKeyByUser(Long id);
    Optional<VPNKey> getKeyByUsername(String username);
    void updateKey(VPNKey key);
    String getVPNConfig(String username);
}
