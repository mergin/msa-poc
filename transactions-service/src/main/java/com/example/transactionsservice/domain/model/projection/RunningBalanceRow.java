package com.example.transactionsservice.domain.model.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection returned by the running-balance window function query.
 * Each row represents one transaction with a cumulative signed balance
 * computed as {@code SUM(CASE WHEN type='CREDIT' THEN amount ELSE -amount END)
 * OVER (PARTITION BY account_id ORDER BY timestamp)}.
 */
public record RunningBalanceRow(
    String accountId,
    LocalDateTime timestamp,
    String type,
    BigDecimal amount,
    String description,
    BigDecimal runningBalance) {}
