package com.vpnservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vpn_settings")
public class VPNSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String value;

    // --- Constructors ---
    public VPNSettings() {}

    public VPNSettings(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
