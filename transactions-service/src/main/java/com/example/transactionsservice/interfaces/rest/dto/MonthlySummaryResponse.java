package com.example.transactionsservice.interfaces.rest.dto;

import com.example.transactionsservice.domain.model.projection.MonthlySummaryRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MonthlySummaryResponse(
    LocalDateTime month,
    BigDecimal totalCredits,
    BigDecimal totalDebits,
    BigDecimal netAmount,
    long transactionCount) {

  public static MonthlySummaryResponse fromProjection(MonthlySummaryRow row) {
    return new MonthlySummaryResponse(
        row.month(),
        row.totalCredits(),
        row.totalDebits(),
        row.netAmount(),
        row.transactionCount());
  }
}
