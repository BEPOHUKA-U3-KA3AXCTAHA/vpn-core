package com.vpnservice.repository;

import com.vpnservice.model.VPNKey;
import com.vpnservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VPNKeyRepository extends JpaRepository<VPNKey, Long> {
    // 🔹 Получить ключ пользователя
    Optional<VPNKey> findByUser(User user);

    // Удалить ключ по id
    void deleteById(Long id);
}
