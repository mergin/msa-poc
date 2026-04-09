package com.example.transactionsservice.analytics.api.dto;

import com.example.transactionsservice.analytics.domain.RunningBalanceRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RunningBalanceResponse(
    String accountId,
    LocalDateTime timestamp,
    String type,
    BigDecimal amount,
    String description,
    BigDecimal runningBalance) {

  public static RunningBalanceResponse fromProjection(RunningBalanceRow row) {
    return new RunningBalanceResponse(
        row.accountId(),
        row.timestamp(),
        row.type(),
        row.amount(),
        row.description(),
        row.runningBalance());
  }
}