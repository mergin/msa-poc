package com.example.transactionsservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
    String id,
    String accountId,
    TransactionType type,
    BigDecimal amount,
    String currency,
    String description,
    LocalDateTime timestamp,
    String category) {}
