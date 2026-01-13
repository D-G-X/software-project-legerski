package de.hft.licensing.utils;

import java.util.regex.Pattern;

public class ApiFormValidator {

  // Regex for names: letters (including accented), marks, apostrophes, hyphens, single spaces
  public static final Pattern nameRegex = Pattern.compile(
      "^\\p{L}(?:[\\p{L}\\p{M}'’\\-–]*" +
          "[\\p{L}\\p{M}])?(?: \\p{L}(?:[\\p{L}\\p{M}'’\\-]*[\\p{L}\\p{M}])?)*$",
      Pattern.UNICODE_CHARACTER_CLASS
  );

  // Regex for emails based on HTML 5 specification
  public static final Pattern emailRegex = Pattern.compile(
      "^(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+" +
          "(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
          "|" +
          "\"(?:[\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x21\\x23-\\x5B\\x5D-\\x7E]|\\\\[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F])*\""
          +
          ")" +
          "@" +
          "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+" +
          "[A-Za-z]{2,}$"
  );

  // Regex for cadastral number: exactly 20 alphanumeric characters
  public static final Pattern cadastralNumberRegex = Pattern.compile("^[A-Za-z0-9]*$");

  // Regex for password: at least one special character (punctuation or symbol)
  public static final Pattern passwordRegex = Pattern.compile(".*[\\p{P}\\p{S}].*",
      Pattern.UNICODE_CHARACTER_CLASS);

  // Regex for IBAN: 15 to 34 characters, first two letters, then 2 numbers, then 11 to 30 alphanumeric characters
  public static final Pattern ibanRegex = Pattern.compile("^[A-Z]{2}[0-9]{2}[0-9A-Z]{11,30}$",
      Pattern.CASE_INSENSITIVE);

  // Regex for BIC: 8 or 11 characters, first 6 letters, then 2 alphanumeric, optional 3 alphanumeric
  public static final Pattern bicRegex = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$",
      Pattern.CASE_INSENSITIVE);

  // Name Validation
  public boolean isValidName(String name) {
    if (name.trim().isEmpty()) {
      return false;
    }
    if (name.length() < 2) {
      return false;
    }
    if (name.length() >= 100) {
      return false;
    }
    return nameRegex.matcher(name).matches();
  }

  // Email validation
  public boolean isValidEmail(String email) {
    if (email.trim().isEmpty()) {
      return false;
    }
    if (email.length() < 5) {
      return false;
    }
    if (email.length() >= 255) {
      return false;
    }
    return emailRegex.matcher(email).matches();
  }

  // Cadastral number validation: exactly 20 characters long
  public boolean isValidCadastralNumber(String cadastral_number) {
    if (cadastral_number.trim().isEmpty()) {
      return false;
    }
    if (cadastral_number.length() != 20) {
      return false;
    }
    return cadastralNumberRegex.matcher(cadastral_number).matches();
  }

  // Password validation: min. 8 and max. 255 characters long and at least one special character
  public boolean isValidPassword(String password) {
    if (password.trim().isEmpty()) {
      return false;
    }
    if (password.length() < 8) {
      return false;
    }
    // avoid cutting off passwords that are too long for database
    if (password.length() >= 255) {
      return false;
    }
    // At least one special character (non letter or number)
    return passwordRegex.matcher(password).matches();
  }

  // IBAN validation: length between 15 and 34 characters, valid characters
  public boolean isValidIban(String iban) {
    if (iban.trim().isEmpty()) {
      return false;
    }
    if (iban.length() < 15) {
      return false;
    }
    if (iban.length() >= 35) {
      return false;
    }
    return ibanRegex.matcher(iban).matches();
  }

  public boolean isValidBic(String bic) {
    if (bic.trim().isEmpty()) {
      return false;
    }
    if (bic.length() != 8 && bic.length() != 11) {
      return false;
    }
    return bicRegex.matcher(bic).matches();
  }
}
