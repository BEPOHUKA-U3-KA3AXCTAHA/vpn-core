package com.vpnservice.repository;

import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class VpnKeyRepositoryTest {

    @Autowired
    private VPNKeyRepository vpnKeyRepository;

    @Autowired
    private UserRepository userRepository;

    private User createTestUser() {
        User user = new User();
        user.setEmail("vpnkey@mail.com");
        user.setPasswordHash("pass123");
        user.setBalance(0.0);
        return userRepository.save(user);
    }

    private VPNKey createTestVpnKey(User user) {
        VPNKey key = new VPNKey();
        key.setKeyData("test-key-123");
        key.setUser(user);
        return vpnKeyRepository.save(key);
    }

    @Test
    public void testCreateVpnKey() {
        User user = createTestUser();
        VPNKey key = createTestVpnKey(user);
        assertNotNull(key.getId());
    }

    @Test
    public void testFindByUser() {
        User user = createTestUser();
        createTestVpnKey(user);

        Optional<VPNKey> key = vpnKeyRepository.findByUser(user);
        assertTrue(key.isPresent());
    }

    @Test
    public void testDeleteVpnKey() {
        User user = createTestUser();
        VPNKey key = createTestVpnKey(user);
        vpnKeyRepository.delete(key);

        Optional<VPNKey> noKey = vpnKeyRepository.findByUser(user);
        assertFalse(noKey.isPresent());}
}
