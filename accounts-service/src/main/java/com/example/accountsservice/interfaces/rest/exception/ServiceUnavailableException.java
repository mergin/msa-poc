package com.example.accountsservice.interfaces.rest.exception;

public class ServiceUnavailableException extends RuntimeException {

  public ServiceUnavailableException(String message) {
    super(message);
  }
}
