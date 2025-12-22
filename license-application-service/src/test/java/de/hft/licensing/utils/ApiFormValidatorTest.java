package de.hft.licensing.utils;

import static de.hft.licensing.utils.ApiFormValidator.bicRegex;
import static de.hft.licensing.utils.ApiFormValidator.cadastralNumberRegex;
import static de.hft.licensing.utils.ApiFormValidator.emailRegex;
import static de.hft.licensing.utils.ApiFormValidator.ibanRegex;
import static de.hft.licensing.utils.ApiFormValidator.nameRegex;
import static de.hft.licensing.utils.ApiFormValidator.passwordRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ApiFormValidatorTest {

  private static ApiFormValidator validator;

  @BeforeAll
  static void setup() {
    validator = new ApiFormValidator();
  }

  @Nested
  class testIsValidName {
    // VALID NAMES

    @Test
    void shouldReturnValidIfNameIsTwoCharactersLong() {
      assertTrue(validator.isValidName("JD"));
    }

    @Test
    void shouldReturnValidIfNameIsTwoSingleCharactersLongWithSpaceInBetween() {
      assertTrue(validator.isValidName("J D"));
    }

    @Test
    void shouldReturnValidIfNameIs99CharactersLong() {
      assertTrue(validator.isValidName("A".repeat(99)));
    }

    @Test
    void shouldReturnValidIfNameHasLeadingAccent() {
      assertTrue(validator.isValidName("John Doé"));
      assertTrue(validator.isValidName("John DoeΩ"));
      assertTrue(validator.isValidName("John Doe中"));
    }

    // INVALID NAMES

    @Test
    void shouldReturnInvalidIfNameIsEmpty() {
      assertFalse(validator.isValidName(""));
      assertFalse(validator.isValidName(" "));
    }

    @Test
    void shouldReturnInvalidIfNameIsOneCharacterLong() {
      assertFalse(validator.isValidName("A"));
      assertFalse(validator.isValidName("À"));
    }

    @Test
    void shouldReturnInvalidIfNameIs100CharactersLong() {
      assertFalse(validator.isValidName("A".repeat(100)));
    }

    @Test
    void shouldReturnInvalidIfNameHasLeadingNumberAccentSpecialCharacterOrSpace() {
      Pattern allowedCharRegex = Pattern.compile("\\p{L}", Pattern.UNICODE_CHARACTER_CLASS);

      List<?> result = performRegexFullTest(nameRegex, allowedCharRegex, "", "John Doe");

      assertEquals(List.of(), result);
    }

    @Test
    void shouldReturnInvalidIfNameHasTrailingNumberSpecialCharacterOrSpace() {
      Pattern allowedCharRegex = Pattern.compile("[\\p{L}\\p{M}]", Pattern.UNICODE_CHARACTER_CLASS);

      List<?> result = performRegexFullTest(nameRegex, allowedCharRegex, "John Doe", "");

      assertEquals(List.of(), result);
    }

    @Test
    void shouldReturnInvalidIfNameHasMultipleSpaces() {
      assertFalse(validator.isValidName("John  Doe"));
    }

    // UNICODE FULL TEST

    @Test
    void shouldReturnInvalidIfNameHasAnyIllegalUnicodeCharacters() {
      Pattern allowedCharRegex = Pattern.compile("[\\p{L}\\p{M}'’\\-– ]",
          Pattern.UNICODE_CHARACTER_CLASS);

      List<?> result = performRegexFullTest(nameRegex, allowedCharRegex, "A", "B");

      assertEquals(List.of(), result);
    }
  }

  @Nested
  class testIsValidEmail {

    // VALID EMAIL ADDRESSES

    @Test
    void shouldReturnValidIfEmailAddressHas5Characters() {
      assertTrue(validator.isValidEmail("j@d.co"));
    }

    @Test
    void shouldReturnValidIfEmailAddressHas254Characters() {
      assertTrue(validator.isValidEmail("j" + "o".repeat(243) + "hn@doe.com"));
      assertTrue(validator.isValidEmail("john@d" + "o".repeat(243) + "e.com"));
      assertTrue(validator.isValidEmail("john@doe.c" + "o".repeat(243) + "m"));
    }

    @Test
    void shouldReturnValidIfEmailAddressHasCapitalLetters() {
      assertTrue(validator.isValidEmail("JOHN@DOE.COM"));
    }

    @Test
    void shouldReturnValidIfEmailAddressHasNumbers() {
      assertTrue(validator.isValidEmail("j0hn@d0e.com"));
    }

    @Test
    void shouldReturnValidIfEmailAddressHasQuotes() {
      assertTrue(validator.isValidEmail("\"john\"@doe.com"));
    }

    @Test
    void shouldReturnValidIfEmailAddressHasExtraDots() {
      assertTrue(validator.isValidEmail("john.doe.doe@doe.com"));
      assertTrue(validator.isValidEmail("john@doe.doe.com"));
      assertTrue(validator.isValidEmail("john@doe.co.uk"));
    }

    // INVALID EMAIL ADDRESSES

    @Test
    void shouldReturnInvalidIfEmailAddressIsEmpty() {
      assertFalse(validator.isValidEmail(""));
      assertFalse(validator.isValidEmail(" "));
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHasOnlySpaces() {
      assertFalse(validator.isValidEmail(" "));
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHas4Characters() {
      assertFalse(validator.isValidEmail("j@dc"));
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHas255Characters() {
      assertFalse(validator.isValidEmail("j" + "o".repeat(244) + "hn@doe.com"));
      assertFalse(validator.isValidEmail("john@d" + "o".repeat(244) + "e.com"));
      assertFalse(validator.isValidEmail("john@doe.c" + "o".repeat(244) + "m"));
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHasNoAt() {
      assertFalse(validator.isValidEmail("johndoe.com"));
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHasNoTld() {
      assertFalse(validator.isValidEmail("john@doe"));
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHasTooShortTld() {
      assertFalse(validator.isValidEmail("john@doe.c"));
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHasConsecutiveDotsInName() {
      assertFalse(validator.isValidEmail("john..doe@doe.com"));
    }

    @Test
    void shouldReturnInvalidIfDomainStartsWithSpecialCharacter() {
      Pattern allowedCharRegex = Pattern.compile("[A-Za-z0-9]");

      List<?> result = performRegexFullTest(emailRegex, allowedCharRegex, "john@", "doe.com");

      assertEquals(List.of(), result);
    }

    @Test
    void shouldReturnInvalidIfDomainEndsWithSpecialCharacter() {
      Pattern allowedCharRegex = Pattern.compile("[A-Za-z0-9]");

      List<?> result = performRegexFullTest(emailRegex, allowedCharRegex, "john@doe", ".com");

      assertEquals(List.of(), result);
    }

    // UNICODE FULL TESTS

    @Test
    void shouldReturnInvalidIfEmailAddressHasIllegalUnicodeCharactersInName() {
      Pattern allowedCharRegex = Pattern.compile("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]"
          + "|[\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x21\\x23-\\x5B\\x5D-\\x7E]"
          + "|\\\\[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]");

      List<?> result = performRegexFullTest(emailRegex, allowedCharRegex, "john", "@doe.com");

      assertEquals(List.of(), result);
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHasIllegalUnicodeCharactersInDomain() {
      Pattern allowedCharRegex = Pattern.compile("[A-Za-z0-9-]");

      List<?> result = performRegexFullTest(emailRegex, allowedCharRegex, "john@doe", ".com");

      assertEquals(List.of(), result);
    }

    @Test
    void shouldReturnInvalidIfEmailAddressHasIllegalUnicodeCharactersInTld() {
      Pattern allowedCharRegex = Pattern.compile("[A-Za-z]");

      List<?> result = performRegexFullTest(emailRegex, allowedCharRegex, "john@doe.com", "");

      assertEquals(List.of(), result);
    }
  }

  @Nested
  class testIsValidCadastralNumber {

    // VALID CADASTRAL NUMBERS

    @Test
    void shouldReturnValidIfCadastralNumberIsExactly20AlphanumericCharacters() {
      assertTrue(validator.isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J0"));
    }

    @Test
    void shouldReturnValidIfCadastralNumberOnlyContainsUppercaseLetters() {
      assertTrue(validator.isValidCadastralNumber("ABCDEFGHIJKLMNOPQRST"));
    }

    @Test
    void shouldReturnValidIfCadastralNumberOnlyContainsLowercaseLetters() {
      assertTrue(validator.isValidCadastralNumber("abcdefghijklmnopqrst"));
    }

    @Test
    void shouldReturnValidIfCadastralNumberOnlyContainsNumbers() {
      assertTrue(validator.isValidCadastralNumber("12345678901234567890"));
    }

    // INVALID CADASTRAL NUMBERS

    @Test
    void shouldReturnInvalidIfCadastralNumberIsEmpty() {
      assertFalse(validator.isValidCadastralNumber(""));
      assertFalse(validator.isValidCadastralNumber(" "));
    }

    @Test
    void shouldReturnInvalidIfCadastralNumberIs19CharactersLong() {
      assertFalse(validator.isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J"));
    }

    @Test
    void shouldReturnInvalidIfCadastralNumberIs21CharactersLong() {
      assertFalse(validator.isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J0K"));
    }

    @Test
    void shouldReturnInvalidIfCadastralNumberHasLeadingSpace() {
      assertFalse(validator.isValidCadastralNumber(" A1B2C3D4E5F6G7H8I9J0"));
    }

    @Test
    void shouldReturnInvalidIfCadastralNumberHasTrailingSpace() {
      assertFalse(validator.isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J0 "));
    }

    // UNICODE FULL TEST

    @Test
    void shouldReturnInvalidIfCadastralNumberHasAnyIllegalUnicodeCharacters() {
      Pattern allowedCharRegex = Pattern.compile("[A-Za-z0-9]");

      List<?> result = performRegexFullTest(cadastralNumberRegex, allowedCharRegex, "A1B2C3D4E5",
          "6G7H8I9J0K");

      assertEquals(List.of(), result);
    }

  }

  @Nested
  class testIsValidPassword {

    // VALID PASSWORDS

    @Test
    void shouldReturnValidIfPasswordIs8CharsLongAndContainsSpecialCharacter() {
      assertTrue(validator.isValidPassword("testabc!"));
    }

    @Test
    void shouldReturnValidIfPasswordIs254CharsLongAndContainsSpecialCharacter() {
      assertTrue(validator.isValidPassword("a".repeat(253) + "!"));
    }

    // INVALID PASSWORDS

    @Test
    void shouldReturnInvalidIfPasswordIsEmpty() {
      assertFalse(validator.isValidPassword(""));
      assertFalse(validator.isValidPassword(" "));
    }

    @Test
    void shouldReturnInvalidIfPasswordIs7CharsLongAndContainsSpecialCharacter() {
      assertFalse(validator.isValidPassword("testab!"));
    }

    @Test
    void shouldReturnInvalidIfPasswordIs255CharsLongAndContainsSpecialCharacter() {
      assertFalse(validator.isValidPassword("a".repeat(254) + "!"));
    }

    @Test
    void shouldReturnInvalidIfPasswordContainsNoSpecialCharacter() {
      assertFalse(validator.isValidPassword("testtest"));
      assertFalse(validator.isValidPassword("testtest"));
      assertFalse(validator.isValidPassword("test test"));
      assertFalse(validator.isValidPassword("test12345"));
      assertFalse(validator.isValidPassword("漢字仮名"));

      // Unicode letters full test (no special characters)
      Pattern unicodeCharsRegex = Pattern.compile("[^\\p{L}]", Pattern.UNICODE_CHARACTER_CLASS);

      List<?> result = performRegexFullTest(passwordRegex, unicodeCharsRegex, "test", "test");

      assertEquals(List.of(), result);
    }

    // UNICODE FULL TEST

    @Test
    void shouldReturnInvalidIfPasswordHasAnyIllegalUnicodeCharacters() {
      Pattern allowedCharRegex = Pattern.compile("[\\p{P}\\p{S}]", Pattern.UNICODE_CHARACTER_CLASS);

      List<?> result = performRegexFullTest(passwordRegex, allowedCharRegex, "test", "test");

      assertEquals(List.of(), result);
    }

  }

  @Nested
  class testIsValidIban {

    // VALID IBANS

    @Test
    void shouldReturnValidIfIbanIs15CharsLong() {
      assertTrue(validator.isValidIban("DE0123456789012"));
      assertTrue(validator.isValidIban("DE01ABCDEFGHIJK"));
    }

    @Test
    void shouldReturnValidIfIbanIs34CharsLong() {
      assertTrue(validator.isValidIban("DE01234567890123456789012345678901"));
      assertTrue(validator.isValidIban("DE01ABCDEFGHIJKLMNOPQRSTUVWXYZABCD"));
    }

    @Test
    void shouldReturnValidIfIbanHasLowercaseLetters() {
      assertTrue(validator.isValidIban("de0123456789012"));
      assertTrue(validator.isValidIban("DE01abcdefghijk"));
    }

    // INVALID IBANS

    @Test
    void shouldReturnInvalidIfIbanIsEmpty() {
      assertFalse(validator.isValidIban(""));
      assertFalse(validator.isValidIban(" "));
    }

    @Test
    void shouldReturnInvalidIfIbanIs14CharsLong() {
      assertFalse(validator.isValidIban("DE001234567890"));
    }

    @Test
    void shouldReturnInvalidIfIbanIs35CharsLong() {
      assertFalse(validator.isValidIban("DE001234567891234567891234567891234"));
    }

    @Test
    void shouldReturnInvalidIfIbanHasNoCountryCode() {
      assertFalse(validator.isValidIban("141592653589793238"));
    }

    @Test
    void shouldReturnInvalidIfIbanHasSingleDigitCountryCode() {
      assertFalse(validator.isValidIban("D141592653589793238"));
    }

    @Test
    void shouldReturnInvalidIfIbanHasThreeDigitCountryCode() {
      assertFalse(validator.isValidIban("DEW141592653589793238"));
    }

    @Test
    void shouldReturnInvalidIfIbanHasNoChecksum() {
      assertFalse(validator.isValidIban("DEABCDEFGHIJKLM"));
    }

    @Test
    void shouldReturnInvalidIfIbanHasSingleDigitChecksum() {
      assertFalse(validator.isValidIban("DE1ABCDEFGHIJKL"));
    }

    // UNICODE FULL TEST

    @Test
    void shouldReturnInvalidIfIbanHasIllegalCharactersInCountryCode() {
      Pattern allowedCharRegex = Pattern.compile("[A-Z]", Pattern.CASE_INSENSITIVE);

      List<?> result1 = performRegexFullTest(ibanRegex, allowedCharRegex, "", "E0123456789012");
      List<?> result2 = performRegexFullTest(ibanRegex, allowedCharRegex, "D", "0123456789012");

      assertEquals(List.of(), result1);
      assertEquals(List.of(), result2);
    }

    @Test
    void shouldReturnInvalidIfIbanHasIllegalCharactersInChecksum() {
      Pattern allowedCharRegex = Pattern.compile("[0-9]");

      List<?> result1 = performRegexFullTest(ibanRegex, allowedCharRegex, "DE", "123456789012");
      List<?> result2 = performRegexFullTest(ibanRegex, allowedCharRegex, "DE0", "23456789012");

      assertEquals(List.of(), result1);
      assertEquals(List.of(), result2);
    }

    @Test
    void shouldReturnInvalidIfIbanHasIllegalCharactersInAlphanumericPart() {
      Pattern allowedCharRegex = Pattern.compile("[0-9A-Z]", Pattern.CASE_INSENSITIVE);

      List<?> result = performRegexFullTest(ibanRegex, allowedCharRegex, "DE0123", "456789012");

      assertEquals(List.of(), result);
    }
  }

  @Nested
  class testIsValidBic {

    // VALID BICS

    @Test
    void shouldReturnValidIfBicIsEmptyDomestic() {
      assertTrue(validator.isValidBic("", "ES0123456789012"));
      assertTrue(validator.isValidBic(" ", "ES0123456789012"));
    }

    @Test
    void shouldReturnValidIfBicIs8CharsLong() {
      assertTrue(validator.isValidBic("SEPADEEF", "DE0123456789012"));
    }

    @Test
    void shouldReturnValidIfBicIs11CharsLong() {
      assertTrue(validator.isValidBic("GENODES1DEH", "DE0123456789012"));
    }

    @Test
    void shouldReturnValidIfBicHasLowercaseLetters() {
      assertTrue(validator.isValidBic("genodes1deh", "DE0123456789012"));
    }

    @Test
    void shouldReturnValidIfBicHasOnlyLettersInAlphanumericPart() {
      assertTrue(validator.isValidBic("GENODESIDEH", "DE0123456789012"));
    }

    @Test
    void shouldReturnValidIfBicHasOnlyNumbersInAlphanumericPart() {
      assertTrue(validator.isValidBic("GENODE12345", "DE0123456789012"));
    }

    // INVALID BICS

    @Test
    void shouldReturnInvalidIfBicIsEmptyNonDomestic() {
      assertFalse(validator.isValidBic("", "DE0123456789012"));
      assertFalse(validator.isValidBic(" ", "DE0123456789012"));
    }

    @Test
    void shouldReturnInvalidIfIbanIsEmpty() {
      assertFalse(validator.isValidBic("GENODES1DEH", ""));
      assertFalse(validator.isValidBic("GENODES1DEH", " "));
    }

    @Test
    void shouldReturnInvalidIfBicIs7CharsLong() {
      assertFalse(validator.isValidBic("GENODES", "DE0123456789012"));
    }

    @Test
    void shouldReturnInvalidIfBicIs9CharsLong() {
      assertFalse(validator.isValidBic("GENODES1D", "DE0123456789012"));
    }

    @Test
    void shouldReturnInvalidIfBicIs10CharsLong() {
      assertFalse(validator.isValidBic("GENODES1DE", "DE0123456789012"));
    }

    @Test
    void shouldReturnInvalidIfBicIs12CharsLong() {
      assertFalse(validator.isValidBic("GENODES1DEHI", "DE0123456789012"));
    }

    // UNICODE FULL TEST

    @Test
    void shouldReturnInvalidIfBicHasIllegalCharactersInBankCodePart() {
      Pattern allowedCharRegex = Pattern.compile("[0-9A-Z]", Pattern.CASE_INSENSITIVE);

      assertEquals(List.of(), performRegexFullTest(bicRegex, allowedCharRegex, "", "ENODES1DEH"));
      assertEquals(List.of(), performRegexFullTest(bicRegex, allowedCharRegex, "G", "NODES1DEH"));
      assertEquals(List.of(), performRegexFullTest(bicRegex, allowedCharRegex, "GE", "NDES1DEH"));
      assertEquals(List.of(), performRegexFullTest(bicRegex, allowedCharRegex, "GEN", "DES1DEH"));
    }

    @Test
    void shouldReturnInvalidIfBicHasIllegalCharactersInCountryCodePart() {
      Pattern allowedCharRegex = Pattern.compile("[0-9A-Z]", Pattern.CASE_INSENSITIVE);

      assertEquals(List.of(), performRegexFullTest(bicRegex, allowedCharRegex, "GENO", "ES1DEH"));
      assertEquals(List.of(), performRegexFullTest(bicRegex, allowedCharRegex, "GENOD", "S1DEH"));
    }

    @Test
    void shouldReturnInvalidIfBicHasIllegalCharactersInAlphanumericPart() {
      Pattern allowedCharRegex = Pattern.compile("[0-9A-Z]", Pattern.CASE_INSENSITIVE);

      assertEquals(List.of(), performRegexFullTest(bicRegex, allowedCharRegex, "GENODES", "DEH"));
    }
  }

  public static List<String> performRegexFullTest(Pattern regexToTest, Pattern allowedCharRegex,
      String leadingString, String trailingString) {
    List<String> failures = new ArrayList<>();

    for (int cp = 0; cp <= 0xFFFF; cp++) {
      char ch = (char) cp;
      String s = String.valueOf(ch);

      // skip allowed chars
      if (allowedCharRegex.matcher(s).matches()) {
        continue;
      }

      // skip control chars
      if (cp <= 0x1F || cp == 0x7F) {
        continue;
      }

      if (regexToTest.matcher(leadingString + ch + trailingString).matches()) {
        failures.add(String.format("U+%04X '%s'", cp, ch));
      }
    }

    return failures;
  }

}
