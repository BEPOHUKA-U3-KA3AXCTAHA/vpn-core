package com.vpnservice.repository;

import com.vpnservice.model.Transaction;
import com.vpnservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
}
