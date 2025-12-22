import {
  bicRegex,
  cadastralNumberRegex, dateRegex,
  emailRegex, ibanRegex, isValidBic,
  isValidCadastralNumber,
  isValidConfirmPassword,
  isValidDateString,
  isValidEmail,
  isValidIban,
  isValidName, isValidOptionalText,
  isValidPassword,
  nameRegex,
  passwordRegex
} from "./validationRules";

// Name Validation
describe("validateName", () => {

  // VALID NAMES
  test("should return valid if name is two characters long", () => {
    expect(isValidName("JD").isValid).toBe(true);
  });

  test("should return valid if name is two single characters long with space in between", () => {
    expect(isValidName("J D").isValid).toBe(true);
  });

  test("should return valid if name is 99 characters long", () => {
    expect(isValidName("A".repeat(99)).isValid).toBe(true);
  });

  test("should return valid if name has leading accent", () => {
    expect(isValidName("John Doé").isValid).toBe(true);
    expect(isValidName("John DoeΩ").isValid).toBe(true);
    expect(isValidName("John Doe中").isValid).toBe(true);
  });

  // INVALID NAMES
  test("should return invalid if name is empty", () => {
    expect(isValidName(undefined).isValid).toBe(false);
    expect(isValidName(null).isValid).toBe(false);
    expect(isValidName("").isValid).toBe(false);
    expect(isValidName(" ").isValid).toBe(false);
  });

  test("should return invalid if name is one character long", () => {
    expect(isValidName("A").isValid).toBe(false);
    expect(isValidName("À").isValid).toBe(false);
  });

  test("should return invalid if name is 100 characters long", () => {
    expect(isValidName("A".repeat(100)).isValid).toBe(false);
  });

  test("should return invalid if name has leading number, accents, special character or space", () => {
    const allowedCharRegex = new RegExp("\\p{L}", "u");
    expect(performRegexFullTest(nameRegex, allowedCharRegex, "", "John Doe")).toEqual([]);
  });

  test("should return invalid if name has trailing number, special character or space", () => {
    const allowedCharRegex = new RegExp("[\\p{L}\\p{M}]", "u");
    expect(performRegexFullTest(nameRegex, allowedCharRegex, "John Doe", "")).toEqual([]);
  });

  test("should return invalid if name has multiple spaces", () => {
    expect(isValidName("John  Doe").isValid).toBe(false);
  });

  // UNICODE FULL TEST
  test("should return invalid if name has any illegal unicode characters", () => {
    const allowedCharRegex = new RegExp("[\\p{L}\\p{M}'’\\-– ]", "u");
    expect(performRegexFullTest(nameRegex, allowedCharRegex, "A", "B")).toEqual([]);
  });
});

// Email Validation
describe("validateEmail", () => {

  // VALID EMAIL ADDRESSES
  test("should return valid if email address has 5 characters", () => {
    expect(isValidEmail("j@d.co").isValid).toBe(true);
  });

  test("should return valid if email address has 254 characters", () => {
    expect(isValidEmail("j" + "o".repeat(243) + "hn@doe.com").isValid).toBe(true);
    expect(isValidEmail("john@d" + "o".repeat(243) + "e.com").isValid).toBe(true);
    expect(isValidEmail("john@doe.c" + "o".repeat(243) + "m").isValid).toBe(true);
  });

  test("should return valid if email address has capital letters", () => {
    expect(isValidEmail("JOHN@DOE.COM").isValid).toBe(true);
  });

  test("should return valid if email address has numbers", () => {
    expect(isValidEmail("j0hn@d0e.com").isValid).toBe(true);
  });

  test("should return valid if email address has quotes", () => {
    expect(isValidEmail("\"john\"@doe.com").isValid).toBe(true);
  });

  test("should return valid if email address has extra dots", () => {
    expect(isValidEmail("john.doe.doe@doe.com").isValid).toBe(true);
    expect(isValidEmail("john@doe.doe.com").isValid).toBe(true);
    expect(isValidEmail("john@doe.co.uk").isValid).toBe(true);
  });

  // INVALID EMAIL ADDRESSES
  test("should return invalid if email address is empty", () => {
    expect(isValidEmail(null).isValid).toBe(false);
    expect(isValidEmail(undefined).isValid).toBe(false);
    expect(isValidEmail("").isValid).toBe(false);
    expect(isValidEmail(" ").isValid).toBe(false);
  });

  test("should return invalid if email address has only spaces", () => {
    expect(isValidEmail(" ").isValid).toBe(false);
  });

  test("should return invalid if email address has 4 characters", () => {
    expect(isValidEmail("j@dc").isValid).toBe(false);
  });

  test("should return invalid if email address has 255 characters", () => {
    expect(isValidEmail("j" + "o".repeat(244) + "hn@doe.com").isValid).toBe(false);
    expect(isValidEmail("john@d" + "o".repeat(244) + "e.com").isValid).toBe(false);
    expect(isValidEmail("john@doe.c" + "o".repeat(244) + "m").isValid).toBe(false);
  });

  test("should return invalid if email address has no @", () => {
    expect(isValidEmail("johndoe.com").isValid).toBe(false);
  });

  test("should return invalid if email address has no tld (no dot)", () => {
    expect(isValidEmail("john@doe").isValid).toBe(false);
  });

  test("should return invalid if email address has too short tld (1 char)", () => {
    expect(isValidEmail("john@doe.c").isValid).toBe(false);
  });

  test("should return invalid if email address has consecutive dots in name", () => {
    expect(isValidEmail("john..doe@doe.com").isValid).toBe(false);
  });

  test("should return invalid if domain starts with special character", () => {
    const allowedCharRegex = new RegExp("[A-Za-z0-9]");
    expect(performRegexFullTest(emailRegex, allowedCharRegex, "john@", "doe.com")).toEqual([]);
  });

  test("should return invalid if domain ends with special character", () => {
    const allowedCharRegex = new RegExp("[A-Za-z0-9]");
    expect(performRegexFullTest(emailRegex, allowedCharRegex, "john@doe", ".com")).toEqual([]);
  });

  // UNICODE FULL TESTS
  test("should return invalid if email address has any illegal unicode characters in name", () => {
    const allowedCharRegex = new RegExp("^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]|[\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x21\\x23-\\x5B\\x5D-\\x7E]|\\\\[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]");
    expect(performRegexFullTest(emailRegex, allowedCharRegex, "john", "@doe.com")).toEqual([]);
  });

  test("should return invalid if email address has any illegal unicode characters in domain", () => {
    const allowedCharRegex = new RegExp("[A-Za-z0-9-]");
    expect(performRegexFullTest(emailRegex, allowedCharRegex, "john@doe", ".com")).toEqual([]);
  });

  test("should return invalid if email address has any illegal unicode characters in tld", () => {
    const allowedCharRegex = new RegExp("[A-Za-z]");
    expect(performRegexFullTest(emailRegex, allowedCharRegex, "john@doe.com", "")).toEqual([]);
  });
});

// Cadastral Number Validation
describe("validateCadastralNumber", () => {
  // VALID CADASTRAL NUMBERS
  test("should return valid if cadastral number is exactly 20 alphanumeric characters", () => {
    expect(isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J0").isValid).toBe(true);
  });

  test("should return valid if cadastral number only contains uppercase letters", () => {
    expect(isValidCadastralNumber("ABCDEFGHIJKLMNOPQRST").isValid).toBe(true);
  });

  test("should return valid if cadastral number only contains lowercase letters", () => {
    expect(isValidCadastralNumber("abcdefghijklmnopqrst").isValid).toBe(true);
  });

  test("should return valid if cadastral number only contains numbers", () => {
    expect(isValidCadastralNumber("12345678901234567890").isValid).toBe(true);
  });

  // INVALID CADASTRAL NUMBERS
  test("should return invalid if cadastral number is empty", () => {
    expect(isValidCadastralNumber(null).isValid).toBe(false);
    expect(isValidCadastralNumber(undefined).isValid).toBe(false);
    expect(isValidCadastralNumber("").isValid).toBe(false);
    expect(isValidCadastralNumber(" ").isValid).toBe(false);
  });

  test("should return invalid if cadastral number is 19 characters long", () => {
    expect(isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J").isValid).toBe(false);
  });

  test("should return invalid if cadastral number is 21 characters long", () => {
    expect(isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J0K").isValid).toBe(false);
  });

  test("should return invalid if cadastral number has leading space", () => {
    expect(isValidCadastralNumber(" A1B2C3D4E5F6G7H8I9J0").isValid).toBe(false);
  });

  test("should return invalid if cadastral number has trailing space", () => {
    expect(isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J0 ").isValid).toBe(false);
  });

  // UNICODE FULL TEST
  test("should return invalid if cadastral number has any illegal unicode characters", () => {
    const allowedCharRegex = new RegExp("[A-Za-z0-9]");
    expect(performRegexFullTest(cadastralNumberRegex, allowedCharRegex, "A1B2C3D4E5", "6G7H8I9J0K")).toEqual([]);
  });
});

// Password Validation
describe("validatePassword", () => {
  // VALID PASSWORDS
  test("should return valid if password is 8 chars long and contains a special character", () => {
    expect(isValidPassword("testabc!").isValid).toBe(true);
  });

  test("should return valid if password is 254 chars long and contains a special character", () => {
    expect(isValidPassword("a".repeat(253) + "!").isValid).toBe(true);
  });

  // INVALID PASSWORDS
  test("should return invalid if password is empty", () => {
    expect(isValidPassword(null).isValid).toBe(false);
    expect(isValidPassword(undefined).isValid).toBe(false);
    expect(isValidPassword("").isValid).toBe(false);
    expect(isValidPassword(" ").isValid).toBe(false);
  });

  test("should return invalid if password is 7 chars long and contains a special character", () => {
    expect(isValidPassword("testab!").isValid).toBe(false);
  });

  test("should return invalid if password is 255 chars long and contains a special character", () => {
    expect(isValidPassword("a".repeat(254) + "!").isValid).toBe(false);
  });

  test("should return invalid if password contains no special character", () => {
    expect(isValidPassword("testtest").isValid).toBe(false);
    expect(isValidPassword("testtest").isValid).toBe(false);
    expect(isValidPassword("test test").isValid).toBe(false);
    expect(isValidPassword("test12345").isValid).toBe(false);
    expect(isValidPassword("漢字仮名").isValid).toBe(false);
    // Unicode letters full test (no special characters)
    const unicodeCharsRegex = new RegExp("[^\\p{L}]", "u");
    expect(performRegexFullTest(passwordRegex, unicodeCharsRegex, "test", "test")).toEqual([]);
  });

  // UNICODE FULL TEST
  test("should return invalid if password has any illegal unicode characters", () => {
    const allowedCharRegex = new RegExp("[\\p{P}\\p{S}]", "u");
    expect(performRegexFullTest(passwordRegex, allowedCharRegex, "test", "test")).toEqual([]);
  });
});

// Confirm Password Validation
describe("validateConfirmPassword", () => {
  // VALID CONFIRM PASSWORDS
  test("should return valid if password and confirm password match", () => {
    expect(isValidConfirmPassword("test123!", "test123!").isValid).toBe(true);
  });

  // INVALID CONFIRM PASSWORDS
  test("should return invalid if password and confirm password are empty", () => {
    expect(isValidConfirmPassword(null, null).isValid).toBe(false);
    expect(isValidConfirmPassword(null, undefined).isValid).toBe(false);
    expect(isValidConfirmPassword(null, "").isValid).toBe(false);
    expect(isValidConfirmPassword(null, " ").isValid).toBe(false);
    expect(isValidConfirmPassword(undefined, null).isValid).toBe(false);
    expect(isValidConfirmPassword(undefined, undefined).isValid).toBe(false);
    expect(isValidConfirmPassword(undefined, "").isValid).toBe(false);
    expect(isValidConfirmPassword(undefined, " ").isValid).toBe(false);
    expect(isValidConfirmPassword("", null).isValid).toBe(false);
    expect(isValidConfirmPassword("", undefined).isValid).toBe(false);
    expect(isValidConfirmPassword("", "").isValid).toBe(false);
    expect(isValidConfirmPassword("", " ").isValid).toBe(false);
    expect(isValidConfirmPassword(" ", null).isValid).toBe(false);
    expect(isValidConfirmPassword(" ", undefined).isValid).toBe(false);
    expect(isValidConfirmPassword(" ", "").isValid).toBe(false);
    expect(isValidConfirmPassword(" ", " ").isValid).toBe(false);
  });

  test("should return invalid if password is empty", () => {
    expect(isValidConfirmPassword(null, "test123!").isValid).toBe(false);
    expect(isValidConfirmPassword(undefined, "test123!").isValid).toBe(false);
    expect(isValidConfirmPassword("", "test123!").isValid).toBe(false);
    expect(isValidConfirmPassword(" ", "test123!").isValid).toBe(false);
  });

  test("should return invalid if confirm password is empty", () => {
    expect(isValidConfirmPassword("test123!", null).isValid).toBe(false);
    expect(isValidConfirmPassword("test123!", undefined).isValid).toBe(false);
    expect(isValidConfirmPassword("test123!", "").isValid).toBe(false);
    expect(isValidConfirmPassword("test123!", " ").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (mistype)", () => {
    expect(isValidConfirmPassword("test123!", "test123?").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (case mismatch)", () => {
    expect(isValidConfirmPassword("test123!", "Test123!").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (leading space)", () => {
    expect(isValidConfirmPassword("test123!", " test123!").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (trailing space)", () => {
    expect(isValidConfirmPassword("test123!", "test123! ").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (leading newline [unix])", () => {
    expect(isValidConfirmPassword("test123!", "\ntest123!").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (trailing newline [unix])", () => {
    expect(isValidConfirmPassword("test123!", "test123!\n").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (leading newline [win])", () => {
    expect(isValidConfirmPassword("test123!", "\r\ntest123!").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (trailing newline [win])", () => {
    expect(isValidConfirmPassword("test123!", "test123!\r\n").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (invisible char \u200B)", () => {
    expect(isValidConfirmPassword("test123!", "test123!\u200B").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (invisible char \u2060)", () => {
    expect(isValidConfirmPassword("test123!", "test123!\u2060").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (invisible char \u00A0)", () => {
    expect(isValidConfirmPassword("test123!", "test123!\u00A0").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (invisible char \uFEFF)", () => {
    expect(isValidConfirmPassword("test123!", "test123!\uFEFF").isValid).toBe(false);
  });

  test("should return invalid if passwords not matching (unicode mismatch)", () => {
    expect(isValidConfirmPassword("täst123!", "t\u0061\u0308st123!").isValid).toBe(false);
  });
});

// Text Validation (Optional Text)
describe("validateTextArea", () => {
  // VALID OPTIONAL TEXT
  test("should return valid if text area is empty", () => {
    expect(isValidOptionalText(null).isValid).toBe(true);
    expect(isValidOptionalText(undefined).isValid).toBe(true);
    expect(isValidOptionalText("").isValid).toBe(true);
    expect(isValidOptionalText(" ").isValid).toBe(true);
  });
  test("should return valid if text area is 10,000 characters long", () => {
    expect(isValidOptionalText("A".repeat(10_000)).isValid).toBe(true);
  });

  // INVALID OPTIONAL TEXT
  test("should return invalid if text area is 10,001 characters long", () => {
    expect(isValidOptionalText("A".repeat(10_001)).isValid).toBe(false);
  });
});

// IBAN Validation
describe("validateIban", () => {

  // VALID IBANS
  test("should return valid if IBAN is 15 chars long and contains first two letters, then 2 numbers, then 11 to 30 alphanumeric characters", () => {
    expect(isValidIban("DE0123456789012").isValid).toBe(true);
    expect(isValidIban("DE01ABCDEFGHIJK").isValid).toBe(true);
  });

  test("should return valid if IBAN is 34 chars long and contains first two letters, then 2 numbers, then 11 to 30 alphanumeric characters", () => {
    expect(isValidIban("DE01234567890123456789012345678901").isValid).toBe(true);
    expect(isValidIban("DE01ABCDEFGHIJKLMNOPQRSTUVWXYZABCD").isValid).toBe(true);
  });

  test("should return valid if IBAN has lowercase letters", () => {
    expect(isValidIban("de0123456789012").isValid).toBe(true);
    expect(isValidIban("DE01abcdefghijk").isValid).toBe(true);
  });

  // INVALID IBANS
  test("should return invalid if IBAN is empty", () => {
    expect(isValidIban(null).isValid).toBe(false);
    expect(isValidIban(undefined).isValid).toBe(false);
    expect(isValidIban("").isValid).toBe(false);
    expect(isValidIban(" ").isValid).toBe(false);
  });

  test("should return invalid if IBAN is 14 chars long", () => {
    expect(isValidIban("DE001234567890").isValid).toBe(false);
  });

  test("should return invalid if IBAN is 35 chars long", () => {
    expect(isValidIban("DE001234567891234567891234567891234").isValid).toBe(false);
  });

  test("should return invalid if IBAN has no country code", () => {
    expect(isValidIban("141592653589793238").isValid).toBe(false);
  });

  test("should return invalid if IBAN has single digit country code", () => {
    expect(isValidIban("D141592653589793238").isValid).toBe(false);
  });

  test("should return invalid if IBAN has tree digit country code", () => {
    expect(isValidIban("DEW141592653589793238").isValid).toBe(false);
  });

  test("should return invalid if IBAN has no checksum", () => {
    expect(isValidIban("DEABCDEFGHIJKLM").isValid).toBe(false);
  });

  test("should return invalid if IBAN has single digit checksum", () => {
    expect(isValidIban("DE1ABCDEFGHIJKL").isValid).toBe(false);
  });

  // UNICODE FULL TEST
  test("should return invalid if IBAN has any illegal characters in country code", () => {
    const allowedCharRegex = new RegExp("[A-Z]", "i");
    expect(performRegexFullTest(ibanRegex, allowedCharRegex, "", "E0123456789012")).toEqual([]);
    expect(performRegexFullTest(ibanRegex, allowedCharRegex, "D", "0123456789012")).toEqual([]);
  });

  test("should return invalid if IBAN has any illegal characters in checksum", () => {
    const allowedCharRegex = new RegExp("[0-9]", "i");
    expect(performRegexFullTest(ibanRegex, allowedCharRegex, "DE", "123456789012")).toEqual([]);
    expect(performRegexFullTest(ibanRegex, allowedCharRegex, "DE0", "23456789012")).toEqual([]);
  });

  test("should return invalid if IBAN has any illegal characters in alphanumeric part", () => {
    const allowedCharRegex = new RegExp("[0-9A-Z]", "i");
    expect(performRegexFullTest(ibanRegex, allowedCharRegex, "DE0123", "456789012")).toEqual([]);
  });
});

// BIC Validation
describe("validateBic", () => {
  // VALID BICS
  test("should return valid if BIC is empty (domestic)", () => {
    expect(isValidBic(null, "ES0123456789012").isValid).toBe(true);
    expect(isValidBic(undefined, "ES0123456789012").isValid).toBe(true);
    expect(isValidBic("", "ES0123456789012").isValid).toBe(true);
    expect(isValidBic(" ", "ES0123456789012").isValid).toBe(true);
  });

  test("should return valid if BIC is 8 chars long and contains first 6 letters, then 2 alphanumeric", () => {
    expect(isValidBic("SEPADEEF", "DE0123456789012").isValid).toBe(true);
  });

  test("should return valid if BIC is 11 chars long and contains first 6 letters, then 2 alphanumeric, then 3 alphanumeric", () => {
    expect(isValidBic("GENODES1DEH", "DE0123456789012").isValid).toBe(true);
  });

  test("should return valid if BIC has lowercase letters", () => {
    expect(isValidBic("genodes1deh", "DE0123456789012").isValid).toBe(true);
  });

  test("should return valid if BIC has only letters in alphanumeric part", () => {
    expect(isValidBic("GENODESIDEH", "DE0123456789012").isValid).toBe(true);
  });

  test("should return valid if BIC has only numbers in alphanumeric part", () => {
    expect(isValidBic("GENODE12345", "DE0123456789012").isValid).toBe(true);
  });

  // INVALID BICS
  test("should return invalid if BIC is empty (non domestic)", () => {
    expect(isValidBic(null, "DE0123456789012").isValid).toBe(false);
    expect(isValidBic(undefined, "DE0123456789012").isValid).toBe(false);
    expect(isValidBic("", "DE0123456789012").isValid).toBe(false);
    expect(isValidBic(" ", "DE0123456789012").isValid).toBe(false);
  });

  test("should return invalid if IBAN is empty", () => {
    expect(isValidBic("GENODES1DEH", null).isValid).toBe(false);
    expect(isValidBic("GENODES1DEH", undefined).isValid).toBe(false);
    expect(isValidBic("GENODES1DEH", "").isValid).toBe(false);
    expect(isValidBic("GENODES1DEH", " ").isValid).toBe(false);
  });

  test("should return invalid if BIC is 7 chars long", () => {
    expect(isValidBic("GENODES", "DE0123456789012").isValid).toBe(false);
  });

  test("should return invalid if BIC is 9 chars long", () => {
    expect(isValidBic("GENODES1D", "DE0123456789012").isValid).toBe(false);
  });

  test("should return invalid if BIC is 10 chars long", () => {
    expect(isValidBic("GENODES1DE", "DE0123456789012").isValid).toBe(false);
  });

  test("should return invalid if BIC is 12 chars long", () => {
    expect(isValidBic("GENODES1DEHI", "DE0123456789012").isValid).toBe(false);
  });

  // UNICODE FULL TEST
  test("should return invalid if BIC has any illegal characters in bank code part", () => {
    const allowedCharRegex = new RegExp("[0-9A-Z]", "i");
    expect(performRegexFullTest(bicRegex, allowedCharRegex, "", "ENODES1DEH")).toEqual([]);
    expect(performRegexFullTest(bicRegex, allowedCharRegex, "G", "NODES1DEH")).toEqual([]);
    expect(performRegexFullTest(bicRegex, allowedCharRegex, "GE", "NDES1DEH")).toEqual([]);
    expect(performRegexFullTest(bicRegex, allowedCharRegex, "GEN", "DES1DEH")).toEqual([]);
  });

  test("should return invalid if BIC has any illegal characters in cuntry code part", () => {
    const allowedCharRegex = new RegExp("[0-9A-Z]", "i");
    expect(performRegexFullTest(bicRegex, allowedCharRegex, "GENO", "ES1DEH")).toEqual([]);
    expect(performRegexFullTest(bicRegex, allowedCharRegex, "GENOD", "S1DEH")).toEqual([]);
  });

  test("should return invalid if BIC has any illegal characters in alphanumeric part", () => {
    const allowedCharRegex = new RegExp("[0-9A-Z]", "i");
    expect(performRegexFullTest(bicRegex, allowedCharRegex, "GENODES", "DEH")).toEqual([]);
  });
});

// Date String Validation
describe("validateDateString", () => {
  // VALID DATE STRINGS
  test("valid ISO date", () => {
    expect(isValidDateString("2024-01-01")).toBe(true);
  });

  test("valid ISO date year 0", () => {
    expect(isValidDateString("0000-01-01")).toBe(true);
  });

  test("valid ISO date year 9999", () => {
    expect(isValidDateString("9999-12-31")).toBe(true);
  });

  test("valid ISO date leap day in leap year", () => {
    expect(isValidDateString("2024-02-29")).toBe(true);
  });

  // INVALID DATE STRINGS
  test("invalid ISO date empty string", () => {
    expect(isValidDateString(null)).toBe(false);
    expect(isValidDateString(undefined)).toBe(false);
    expect(isValidDateString("")).toBe(false);
    expect(isValidDateString(" ")).toBe(false);
  });

  test("invalid ISO date whitespace (correct length)", () => {
    expect(isValidDateString("   ")).toBe(false);
  });

  test("invalid ISO date format does not match regex", () => {
    expect(isValidDateString("2025/01/01")).toBe(false);
    expect(isValidDateString("2025.01.01")).toBe(false);
  });

  test("invalid ISO date format leading zeros", () => {
    expect(isValidDateString("2025-1-01")).toBe(false);
    expect(isValidDateString("2025-01-1")).toBe(false);
    expect(isValidDateString("25-01-01")).toBe(false);
  });

  test("invalid ISO date too short", () => {
    expect(isValidDateString("202-01-01")).toBe(false);
    expect(isValidDateString("2024-1-01")).toBe(false);
    expect(isValidDateString("2024-01-1")).toBe(false);
  });

  test("invalid ISO date too long", () => {
    expect(isValidDateString("20240-01-01")).toBe(false);
    expect(isValidDateString("2024-010-01")).toBe(false);
    expect(isValidDateString("2024-01-010")).toBe(false);
  });

  test("invalid non-ISO date", () => {
    expect(isValidDateString("abcd-ef-gh")).toBe(false);
  });

  test("invalid auto-corrected ISO date (Feb 30)", () => {
    expect(isValidDateString("2025-02-30")).toBe(false);
  });

  test("invalid ISO date day in month", () => {
    expect(isValidDateString("2025-04-31")).toBe(false);
  });

  test("invalid ISO date month 0", () => {
    expect(isValidDateString("2024-00-10")).toBe(false);
  });

  test("invalid ISO date month 13", () => {
    expect(isValidDateString("2025-13-01")).toBe(false);
  });

  test("invalid ISO date day 0", () => {
    expect(isValidDateString("2025-01-00")).toBe(false);
  });

  test("invalid ISO date day 32", () => {
    expect(isValidDateString("2025-01-32")).toBe(false);
  });

  test("invalid ISO date leap day in non-leap year", () => {
    expect(isValidDateString("2025-02-29")).toBe(false);
  });
  // UNICODE FULL TEST
  test("should return invalid if ISO date has any illegal characters", () => {
    const allowedCharRegex = new RegExp("[0-9]", "i");
    expect(performRegexFullTest(dateRegex, allowedCharRegex, "202", "-01-01")).toEqual([]);
    expect(performRegexFullTest(dateRegex, allowedCharRegex, "2024-", "1-01")).toEqual([]);
    expect(performRegexFullTest(dateRegex, allowedCharRegex, "2024-0", "-01")).toEqual([]);
    expect(performRegexFullTest(dateRegex, allowedCharRegex, "2024-01-", "1")).toEqual([]);
    expect(performRegexFullTest(dateRegex, allowedCharRegex, "2024-01-0", "")).toEqual([]);
  });
});

function performRegexFullTest(
    regexToTest: RegExp,
    allowedCharRegex: RegExp,
    leadingString: string,
    trailingString: string,
): string[] {
  const failures: string[] = [];

  for (let cp = 0; cp <= 0xffff; cp++) {
    const ch = String.fromCodePoint(cp);

    // skip allowed chars
    if (allowedCharRegex.test(ch)) continue;

    // skip control chars
    if (/[\u0000-\u001F\u007F]/u.test(ch)) continue;

    if (regexToTest.test(`${leadingString}${ch}${trailingString}`)) {
      failures.push(
          `U+${cp.toString(16).toUpperCase().padStart(4, "0")} '${ch}'`
      );
    }
  }

  return failures;
}
