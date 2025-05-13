package com.vpnservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vpn_settings")
public class VPNSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double vpnPrice;  // Цена VPN-доступа

    // --- Constructors ---
    public VPNSettings() {}

    public VPNSettings(Double vpnPrice) {
        this.vpnPrice = vpnPrice;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public Double getVpnPrice() {
        return vpnPrice;
    }

    public void setVpnPrice(Double vpnPrice) {
        this.vpnPrice = vpnPrice;
    }
}
