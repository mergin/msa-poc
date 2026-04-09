package com.example.customersservice.customer.api.dto;

import com.example.customersservice.customer.domain.Customer;

public record CustomerResponse(String id, String name, String email, String status) {

  public static CustomerResponse fromDomain(Customer customer) {
    return new CustomerResponse(
        customer.id(),
        customer.name(),
        customer.email(),
        customer.status().name().toLowerCase());
  }
}