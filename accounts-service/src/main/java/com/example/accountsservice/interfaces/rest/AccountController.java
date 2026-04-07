package com.example.accountsservice.interfaces.rest;

import com.example.accountsservice.application.usecase.GetAccountByIdUseCase;
import com.example.accountsservice.application.usecase.GetAccountOwnerUseCase;
import com.example.accountsservice.application.usecase.ListAccountsUseCase;
import com.example.accountsservice.interfaces.rest.dto.AccountOwnerResponse;
import com.example.accountsservice.interfaces.rest.dto.AccountResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accounts")
public class AccountController {

  private final ListAccountsUseCase listAccountsUseCase;
  private final GetAccountByIdUseCase getAccountByIdUseCase;
  private final GetAccountOwnerUseCase getAccountOwnerUseCase;

  public AccountController(
      ListAccountsUseCase listAccountsUseCase,
      GetAccountByIdUseCase getAccountByIdUseCase,
      GetAccountOwnerUseCase getAccountOwnerUseCase) {
    this.listAccountsUseCase = listAccountsUseCase;
    this.getAccountByIdUseCase = getAccountByIdUseCase;
    this.getAccountOwnerUseCase = getAccountOwnerUseCase;
  }

  @GetMapping
  public Flux<AccountResponse> getAccounts(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return listAccountsUseCase.execute(page, size).map(AccountResponse::fromDomain);
  }

  @GetMapping("/{id}")
  public Mono<AccountResponse> getAccountById(@PathVariable String id) {
    return getAccountByIdUseCase.execute(id).map(AccountResponse::fromDomain);
  }

  @GetMapping("/{id}/owner")
  public Mono<AccountOwnerResponse> getAccountOwner(@PathVariable String id) {
    return getAccountOwnerUseCase.execute(id);
  }
}
