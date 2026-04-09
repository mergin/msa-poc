package com.example.accountsservice.account.application;

import com.example.accountsservice.account.api.exception.ResourceNotFoundException;
import com.example.accountsservice.account.domain.Account;
import com.example.accountsservice.account.domain.AccountRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetAccountByIdUseCase {

  private final AccountRepository accountRepository;

  public GetAccountByIdUseCase(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Mono<Account> execute(String id) {
    return accountRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Account not found")));
  }
}