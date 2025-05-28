package com.vpnservice.controller;

import com.vpnservice.dto.TransactionResponse;
import com.vpnservice.model.Transaction;
import com.vpnservice.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/user/")
    public List<TransactionResponse> getUserTransactions() {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Transaction> transactions =  transactionService.getTransactionsByUser(currentUser.getUsername());

        return transactions.stream()
                .map(tx -> new TransactionResponse(
                        tx.getId(),
                        tx.getUser().getId(),
                        tx.getAmount(),
                        tx.getDate()))
                .toList();
    }
}
