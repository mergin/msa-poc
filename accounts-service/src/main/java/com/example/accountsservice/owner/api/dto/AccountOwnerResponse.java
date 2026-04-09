package com.example.accountsservice.owner.api.dto;

import com.example.accountsservice.account.domain.Account;
import com.example.accountsservice.owner.infrastructure.client.dto.CustomerSummary;

public record AccountOwnerResponse(
    String accountId,
    String accountNumber,
    String ownerId,
    String ownerFirstName,
    String ownerLastName,
    String ownerEmail) {

  public static AccountOwnerResponse of(Account account, CustomerSummary owner) {
    return new AccountOwnerResponse(
        account.id(),
        account.accountNumber(),
        owner.id(),
        owner.firstName(),
        owner.lastName(),
        owner.email());
  }
}