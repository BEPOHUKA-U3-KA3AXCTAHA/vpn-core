package com.vpnservice.repository;

import com.vpnservice.model.VPNKey;
import com.vpnservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VPNKeyRepository extends JpaRepository<VPNKey, Long> {
    List<VPNKey> findByUser(User user);
}
