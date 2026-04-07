package com.example.transactionsservice.domain.model.projection;

import java.math.BigDecimal;

/**
 * Projection returned by the top-accounts ranking query.
 * Ranks accounts by total transaction volume (sum of all amounts regardless of type).
 */
public record TopAccountRow(String accountId, BigDecimal totalVolume, long transactionCount) {}
