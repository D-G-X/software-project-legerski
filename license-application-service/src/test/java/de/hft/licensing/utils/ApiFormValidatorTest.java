package de.hft.licensing.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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

}
