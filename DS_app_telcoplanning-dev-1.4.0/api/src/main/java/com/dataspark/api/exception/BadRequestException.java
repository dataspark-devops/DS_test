package com.dataspark.api.exception;

public class BadRequestException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = -7702270839723778295L;

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
