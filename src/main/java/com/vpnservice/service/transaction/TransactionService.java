package com.vpnservice.service.transaction;

import com.vpnservice.model.Transaction;
import java.util.List;

public interface TransactionService {
    Transaction processTopUp(String username, Double amount);
    List<Transaction> getTransactionsByUser(Long userId);
}
