package com.vpnservice.repository;

import com.vpnservice.model.VPNSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VPNSettingsRepository extends JpaRepository<VPNSettings, Long> {
    // позволяет искать по ключу, например "vpn_price"
    Optional<VPNSettings> findByKey(String key);
}
