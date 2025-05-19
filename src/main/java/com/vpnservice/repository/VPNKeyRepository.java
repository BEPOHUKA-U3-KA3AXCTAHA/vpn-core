package com.vpnservice.repository;

import com.vpnservice.model.VPNKey;
import com.vpnservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VPNKeyRepository extends JpaRepository<VPNKey, Long> {
    // 🔹 Получить все ключи пользователя
    List<VPNKey> findByUser(User user);

    // Удалить ключ по id
    void deleteById(Long id);
}
