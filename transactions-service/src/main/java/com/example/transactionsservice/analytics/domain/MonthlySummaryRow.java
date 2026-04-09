package com.example.transactionsservice.analytics.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MonthlySummaryRow(
    LocalDateTime month,
    BigDecimal totalCredits,
    BigDecimal totalDebits,
    BigDecimal netAmount,
    long transactionCount) {}