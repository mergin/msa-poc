package com.example.accountsservice.application.usecase;

import com.example.accountsservice.domain.model.Account;
import com.example.accountsservice.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListAccountsUseCase {

  private final AccountRepository accountRepository;

  public ListAccountsUseCase(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Flux<Account> execute(int page, int size) {
    int resolvedPage = Math.max(0, page);
    int resolvedSize = Math.max(1, Math.min(size, 100));
    return accountRepository.findPage(resolvedPage, resolvedSize);
  }
}
