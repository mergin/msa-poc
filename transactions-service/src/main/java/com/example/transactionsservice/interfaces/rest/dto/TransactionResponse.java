package com.example.transactionsservice.interfaces.rest.dto;

import com.example.transactionsservice.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    String id,
    String accountId,
    String type,
    BigDecimal amount,
    String currency,
    String description,
    LocalDateTime timestamp,
    String category) {

  public static TransactionResponse fromDomain(Transaction t) {
    return new TransactionResponse(
        t.id(),
        t.accountId(),
        t.type().name().toLowerCase(),
        t.amount(),
        t.currency(),
        t.description(),
        t.timestamp(),
        t.category());
  }
}
