package com.example.accountsservice.account.api.dto;

import com.example.accountsservice.account.domain.Account;

public record AccountResponse(
    String id,
    String accountNumber,
    String type,
    double balance,
    String currency,
    String ownerId) {

  public static AccountResponse fromDomain(Account account) {
    return new AccountResponse(
        account.id(),
        account.accountNumber(),
        account.type().name().toLowerCase(),
        account.balance().doubleValue(),
        account.currency(),
        account.ownerId());
  }
}