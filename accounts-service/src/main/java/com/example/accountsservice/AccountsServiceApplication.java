package com.example.accountsservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class AccountsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AccountsServiceApplication.class, args);
  }

  @Bean
  public WebClient customersWebClient(
      @Value("${clients.customers.base-url:http://localhost:8081}") String baseUrl) {
    return WebClient.builder().baseUrl(baseUrl).build();
  }
}
