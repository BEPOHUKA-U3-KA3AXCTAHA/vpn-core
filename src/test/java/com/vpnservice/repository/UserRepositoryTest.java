package com.vpnservice.repository;

import com.vpnservice.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Вспомогательный метод для создания юзера
    private User createTestUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setPasswordHash("123456");
        user.setBalance(0.0);
        user.setToken("usertoken123");
        return userRepository.save(user);
    }

    @Test
    public void testCreateUser() {
        User saved = createTestUser();
        assertNotNull(saved.getId());
    }

    @Test
    public void testFindByEmail() {
        // Сначала создаём пользователя
        createTestUser();

        // Теперь пробуем найти
        Optional<User> foundUser = userRepository.findByEmail("test@mail.com");

        assertTrue(foundUser.isPresent());
        assertEquals("test@mail.com", foundUser.get().getEmail());
    }

    @Test
    public void testUpdateUser() {
        // Сначала создаём пользователя
        createTestUser();

        User user = userRepository.findByEmail("test@mail.com").get();
        user.setBalance(100.0);
        userRepository.save(user);

        User updated = userRepository.findByEmail("test@mail.com").get();
        assertEquals(100.0, updated.getBalance());
    }

    @Test
    public void testDeleteUser() {
        // Сначала создаём пользователя
        createTestUser();

        User user = userRepository.findByEmail("test@mail.com").get();
        userRepository.delete(user);

        Optional<User> deleted = userRepository.findByEmail("test@mail.com");
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testFindByEmailAndPasswordHash() {
        User user = createTestUser();
        Optional<User> found = userRepository.findByEmailAndPasswordHash("test@mail.com", "123456");
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
    }

    @Test
    public void testFindById() {
        User user = createTestUser();
        Optional<User> found = userRepository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals(user.getEmail(), found.get().getEmail());
    }

    @Test
    public void testFindByToken() {
        User user = createTestUser();
        Optional<User> found = userRepository.findByToken("usertoken123");
        assertTrue(found.isPresent());
        assertEquals(user.getEmail(), found.get().getEmail());
    }
}
