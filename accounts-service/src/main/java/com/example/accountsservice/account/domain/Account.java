package com.example.accountsservice.account.domain;

import java.math.BigDecimal;

public record Account(
    String id,
    String accountNumber,
    AccountType type,
    BigDecimal balance,
    String currency,
    String ownerId) {}