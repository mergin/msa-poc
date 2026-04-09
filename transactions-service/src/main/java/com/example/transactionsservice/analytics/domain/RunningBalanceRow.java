package com.example.transactionsservice.analytics.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RunningBalanceRow(
    String accountId,
    LocalDateTime timestamp,
    String type,
    BigDecimal amount,
    String description,
    BigDecimal runningBalance) {}