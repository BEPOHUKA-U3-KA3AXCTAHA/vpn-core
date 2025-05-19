package com.vpnservice.repository;

import com.vpnservice.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Найти админа по email
    Optional<Admin> findByEmail(String email);

    // Найти админа по email и паролю
    Optional<Admin> findByEmailAndPasswordHash(String email, String passwordHash);

    // Найти админа по id
    Optional<Admin> findById(Long id);

    // Найти админа по токену
    Optional<Admin> findByToken(String token);

    // Удалить админа по id
    void deleteById(Long id);
}
