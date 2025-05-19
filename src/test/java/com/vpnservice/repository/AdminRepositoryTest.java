package com.vpnservice.repository;

import com.vpnservice.model.Admin;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    private Admin createTestAdmin() {
        Admin admin = new Admin();
        admin.setEmail("admin@mail.com");
        admin.setPasswordHash("adminpass");
        admin.setToken("sometoken123");
        return adminRepository.save(admin);
    }

    @Test
    public void testCreateAdmin() {
        Admin saved = createTestAdmin();
        assertNotNull(saved.getId());
    }

    @Test
    public void testFindByEmail() {
        createTestAdmin();
        Optional<Admin> found = adminRepository.findByEmail("admin@mail.com");
        assertTrue(found.isPresent());
        assertEquals("admin@mail.com", found.get().getEmail());
    }

    @Test
    public void testUpdateAdmin() {
        Admin admin = createTestAdmin();
        admin.setPasswordHash("newpass");
        adminRepository.save(admin);

        Admin updated = adminRepository.findByEmail("admin@mail.com").get();
        assertEquals("newpass", updated.getPasswordHash());
    }

    @Test
    public void testDeleteAdmin() {
        Admin admin = createTestAdmin();
        adminRepository.delete(admin);

        Optional<Admin> deleted = adminRepository.findByEmail("admin@mail.com");
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testFindByEmailAndPasswordHash() {
        Admin admin = createTestAdmin();
        Optional<Admin> found = adminRepository.findByEmailAndPasswordHash("admin@mail.com", "adminpass");
        assertTrue(found.isPresent());
        assertEquals(admin.getId(), found.get().getId());
    }

    @Test
    public void testFindById() {
        Admin admin = createTestAdmin();
        Optional<Admin> found = adminRepository.findById(admin.getId());
        assertTrue(found.isPresent());
        assertEquals(admin.getEmail(), found.get().getEmail());
    }

    @Test
    public void testFindByToken() {
        Admin admin = createTestAdmin();
        Optional<Admin> found = adminRepository.findByToken("sometoken123");
        assertTrue(found.isPresent());
        assertEquals(admin.getEmail(), found.get().getEmail());
    }
}
