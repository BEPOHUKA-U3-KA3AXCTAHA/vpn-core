package com.vpnservice.service.vpnsettings;

import com.vpnservice.model.VPNSettings;

public interface VPNSettingsService {
    VPNSettings getSettings();
    void updateVPNPrice(Double newPrice);
}
