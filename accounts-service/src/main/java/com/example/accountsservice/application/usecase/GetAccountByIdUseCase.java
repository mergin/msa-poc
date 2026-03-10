package com.example.accountsservice.application.usecase;

import com.example.accountsservice.domain.model.Account;
import com.example.accountsservice.domain.repository.AccountRepository;
import com.example.accountsservice.interfaces.rest.exception.ResourceNotFoundException;
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
