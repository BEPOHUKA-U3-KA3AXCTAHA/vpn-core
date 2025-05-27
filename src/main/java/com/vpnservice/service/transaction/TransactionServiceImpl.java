package com.vpnservice.service.transaction;

import com.vpnservice.model.Transaction;
import com.vpnservice.model.User;
import com.vpnservice.repository.TransactionRepository;
import com.vpnservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Transaction processTopUp(String username, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));

        // Обновляем баланс пользователя
        double newBalance = user.getBalance() + amount;
        user.setBalance(newBalance);
        userRepository.save(user);

        // Создаем и сохраняем транзакцию
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactionsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        return transactionRepository.findByUser(user, null).getContent();
    }
}
