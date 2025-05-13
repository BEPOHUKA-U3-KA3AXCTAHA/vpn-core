package com.vpnservice.service.vpnkey;

import com.vpnservice.model.VPNKey;

import java.util.List;

public interface VPNKeyService {
    VPNKey generateKey(Long userId);
    void revokeKey(Long keyId);
    List<VPNKey> getKeysByUser(Long userId);
}
