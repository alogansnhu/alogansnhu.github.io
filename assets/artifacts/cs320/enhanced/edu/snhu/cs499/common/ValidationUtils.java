package edu.snhu.cs499.common;

import java.time.LocalDateTime;

import edu.snhu.cs499.exceptions.InvalidRequestException;

public final class ValidationUtils {
  private ValidationUtils() {
  }

  public static <T> T requireNotNull(T value, String fieldName) {
    if (value == null) {
      throw new InvalidRequestException(fieldName + " must not be null.");
    }
    return value;
  }

  public static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new InvalidRequestException(fieldName + " must not be null or blank.");
    }
    return value;
  }

  public static String requireMaxLength(String value, int maxLength, String fieldName) {
    requireNotNull(value, fieldName);
    if (value.length() > maxLength) {
      throw new InvalidRequestException(fieldName + " must be at most " + maxLength + " characters.");
    }
    return value;
  }

  public static String requirePattern(String value, String regex, String fieldName, String description) {
    requireNotNull(value, fieldName);
    if (!value.matches(regex)) {
      throw new InvalidRequestException(fieldName + " must match " + description + ".");
    }
    return value;
  }

  public static int requirePositive(int value, String fieldName) {
    if (value <= 0) {
      throw new InvalidRequestException(fieldName + " must be greater than zero.");
    }
    return value;
  }

  public static LocalDateTime requireFutureOrPresent(LocalDateTime value, String fieldName) {
    requireNotNull(value, fieldName);
    if (value.isBefore(LocalDateTime.now())) {
      throw new InvalidRequestException(fieldName + " must not be in the past.");
    }
    return value;
  }
}
