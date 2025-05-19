package com.vpnservice.repository;

import com.vpnservice.model.Transaction;
import com.vpnservice.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User createTestUser() {
        User user = new User();
        user.setEmail("trans@mail.com");
        user.setPasswordHash("123");
        user.setBalance(0.0);
        return userRepository.save(user);
    }

    private Transaction createTestTransaction(User user) {
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setAmount(99.99);
        return transactionRepository.save(tx);
    }

    @Test
    public void testCreateTransaction() {
        User user = createTestUser();
        Transaction tx = createTestTransaction(user);
        assertNotNull(tx.getId());
    }

    @Test
    public void testReadTransactionsByUser() {
        User user = createTestUser();
        createTestTransaction(user);
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> list = transactionRepository.findByUser(user, pageable).getContent();
        assertEquals(1, list.size());
    }

    @Test
    public void testDeleteTransaction() {
        User user = createTestUser();
        Transaction tx = createTestTransaction(user);
        transactionRepository.delete(tx);

        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> list = transactionRepository.findByUser(user, pageable).getContent();
        assertEquals(0, list.size());
    }
}
