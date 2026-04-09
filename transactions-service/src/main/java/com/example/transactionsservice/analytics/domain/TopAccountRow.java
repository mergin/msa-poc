package com.example.transactionsservice.analytics.domain;

import java.math.BigDecimal;

public record TopAccountRow(String accountId, BigDecimal totalVolume, long transactionCount) {}