package com.example.accountsservice.owner.api;

import com.example.accountsservice.owner.api.dto.AccountOwnerResponse;
import com.example.accountsservice.owner.application.GetAccountOwnerUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accounts")
public class AccountOwnerController {

  private final GetAccountOwnerUseCase getAccountOwnerUseCase;

  public AccountOwnerController(GetAccountOwnerUseCase getAccountOwnerUseCase) {
    this.getAccountOwnerUseCase = getAccountOwnerUseCase;
  }

  @GetMapping("/{id}/owner")
  public Mono<AccountOwnerResponse> getAccountOwner(@PathVariable String id) {
    return getAccountOwnerUseCase.execute(id);
  }
}