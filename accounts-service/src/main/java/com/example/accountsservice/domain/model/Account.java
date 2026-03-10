package com.example.accountsservice.domain.model;

import java.math.BigDecimal;

public record Account(
    String id,
    String accountNumber,
    AccountType type,
    BigDecimal balance,
    String currency,
    String ownerId) {}
