package com.example.accountsservice.application.usecase;

import com.example.accountsservice.domain.repository.AccountRepository;
import com.example.accountsservice.infrastructure.client.CustomersClient;
import com.example.accountsservice.interfaces.rest.dto.AccountOwnerResponse;
import com.example.accountsservice.interfaces.rest.exception.ResourceNotFoundException;
import com.example.accountsservice.interfaces.rest.exception.ServiceUnavailableException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetAccountOwnerUseCase {

  private final AccountRepository accountRepository;
  private final CustomersClient customersClient;

  public GetAccountOwnerUseCase(
      AccountRepository accountRepository, CustomersClient customersClient) {
    this.accountRepository = accountRepository;
    this.customersClient = customersClient;
  }

  public Mono<AccountOwnerResponse> execute(String accountId) {
    return accountRepository
        .findById(accountId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Account not found")))
        .flatMap(
            account ->
                customersClient
                    .findById(account.ownerId())
                    .switchIfEmpty(
                        Mono.error(
                            new ServiceUnavailableException(
                                "customers-service is unavailable, unable to resolve owner")))
                    .map(owner -> AccountOwnerResponse.of(account, owner)));
  }
}
