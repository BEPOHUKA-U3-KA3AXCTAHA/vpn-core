package com.vpnservice.dto;

import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private Long userId;
    private Double amount;
    private LocalDateTime date;

    public TransactionResponse(Long id, Long userId, Double amount, LocalDateTime date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Double getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
