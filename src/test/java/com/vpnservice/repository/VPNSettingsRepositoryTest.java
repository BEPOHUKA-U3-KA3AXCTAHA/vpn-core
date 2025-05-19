package com.vpnservice.repository;

import com.vpnservice.model.VPNSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class VPNSettingsRepositoryTest {

    @Autowired
    private VPNSettingsRepository vpnSettingsRepository;

    @Test
    public void ReadVpnPrice() {
        // Извлекаем настройку по ключу
        Optional<VPNSettings> setting = vpnSettingsRepository.findByKey("vpn_price");

        // Проверяем, что настройка была успешно сохранена и извлечена
        assertTrue(setting.isPresent());
    }
}
