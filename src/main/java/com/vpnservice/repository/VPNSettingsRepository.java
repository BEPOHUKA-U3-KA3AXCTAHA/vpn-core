package com.vpnservice.repository;

import com.vpnservice.model.VPNSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VPNSettingsRepository extends JpaRepository<VPNSettings, Long> {
}
