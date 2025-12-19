package de.hft.licensing.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ApiFormValidatorTest {

  static ApiFormValidator validator;

  @BeforeAll
  static void setup() {
    validator = new ApiFormValidator();
  }

  @Nested
  class testIsValidName {

    @Test
    void returnsFalse_whenNameIsEmpty() {
      assertFalse(validator.isValidName(""));
    }
  }

  @Nested
  class testIsValidEmail {

    @Test
    void returnsFalse_whenEmailIsEmpty() {
      assertFalse(validator.isValidEmail(""));
    }
  }

  @Nested
  class testIsValidCadastralNumber {

    @Test
    void returnsFalse_whenCadastralNumberIsEmpty() {
      assertFalse(validator.isValidCadastralNumber(""));
    }
  }

  @Nested
  class testIsValidPassword {

    @Test
    void returnsFalse_whenPasswordIsEmpty() {
      assertFalse(validator.isValidPassword(""));
    }
  }

  @Nested
  class testIsValidConfirmPassword {

    @Test
    void returnsFalse_whenConfirmPasswordAndPasswordIsEmpty() {
      assertFalse(validator.isValidConfirmPassword("", ""));
    }
  }

  @Nested
  class testIsValidIban {

    @Test
    void returnsFalse_whenIbanIsEmpty() {
      assertFalse(validator.isValidIban(""));
    }
  }

  @Nested
  class testIsValidBic {

    @Test
    void returnsFalse_whenBicAndIbanIsEmpty() {
      assertFalse(validator.isValidBic("", ""));
    }
  }

  @Nested
  class testIsValidDateString {

    @Test
    void returnsFalse_whenDateStringIsEmpty() {
      assertFalse(validator.isValidDateString(""));
    }
  }

}
