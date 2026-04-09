package com.example.customersservice.customer.domain;

public record Customer(String id, String name, String email, CustomerStatus status) {}