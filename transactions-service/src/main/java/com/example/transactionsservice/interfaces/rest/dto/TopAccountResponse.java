package com.example.transactionsservice.interfaces.rest.dto;

import com.example.transactionsservice.domain.model.projection.TopAccountRow;
import java.math.BigDecimal;

public record TopAccountResponse(
    String accountId, BigDecimal totalVolume, long transactionCount) {

  public static TopAccountResponse fromProjection(TopAccountRow row) {
    return new TopAccountResponse(row.accountId(), row.totalVolume(), row.transactionCount());
  }
}
