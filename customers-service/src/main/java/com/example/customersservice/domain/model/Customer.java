package com.example.customersservice.domain.model;

public record Customer(String id, String name, String email, CustomerStatus status) {}
