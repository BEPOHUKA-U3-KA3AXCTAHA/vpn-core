package com.vpnservice.repository;

import com.vpnservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по email
    Optional<User> findByEmail(String email);

    // Найти пользователя по паролю (для проверки пароля, возможно, тебе нужно будет кастомное решение, так как пароль должен быть зашифрован)
    Optional<User> findByEmailAndPasswordHash(String email, String passwordHash);

    // Найти пользователя по id
    Optional<User> findById(Long id);

    // Найти пользователя по токену
    Optional<User> findByToken(String token);

    // Удаление пользователя по id
    void deleteById(Long id);
}
