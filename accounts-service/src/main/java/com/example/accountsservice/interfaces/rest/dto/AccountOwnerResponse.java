package com.example.accountsservice.interfaces.rest.dto;

import com.example.accountsservice.domain.model.Account;
import com.example.accountsservice.infrastructure.client.dto.CustomerSummary;

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
