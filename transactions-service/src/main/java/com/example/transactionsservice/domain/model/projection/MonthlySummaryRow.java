package com.example.transactionsservice.domain.model.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection returned by the monthly-summary GROUP BY query.
 * Aggregates debits, credits, net movement, and count per calendar month
 * for a given account.
 */
public record MonthlySummaryRow(
    LocalDateTime month,
    BigDecimal totalCredits,
    BigDecimal totalDebits,
    BigDecimal netAmount,
    long transactionCount) {}
