package com.vpnservice.messaging;

import java.io.Serializable;

public class VpnKeyGenerationRequest implements Serializable {
    private Long userId;
    private String email;

    public VpnKeyGenerationRequest() {}

    public VpnKeyGenerationRequest(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
