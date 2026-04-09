package com.example.transactionsservice.analytics.api.dto;

import com.example.transactionsservice.analytics.domain.TopAccountRow;
import java.math.BigDecimal;

public record TopAccountResponse(
    String accountId, BigDecimal totalVolume, long transactionCount) {

  public static TopAccountResponse fromProjection(TopAccountRow row) {
    return new TopAccountResponse(row.accountId(), row.totalVolume(), row.transactionCount());
  }
}