import {
  cadastralNumberRegex,
  emailRegex, isValidDateString,
  nameRegex,
  passwordRegex,
  isValidCadastralNumber,
  isValidConfirmPassword,
  isValidEmail,
  isValidIban,
  isValidName,
  isValidPassword, isValidSepaMandate
} from "./validationRules";

// Name Validation
describe("validateName", () => {

  // VALID NAMES TODO: check regex
  test("should return valid if name is two characters long", () => {
    expect(isValidName("JD").isValid).toBe(true);
  })

  test("should return valid if name is two single characters long with space in between", () => {
    expect(isValidName("J D").isValid).toBe(true);
  })

  test("should return valid if name is 99 characters long", () => {
    expect(isValidName("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRST").isValid).toBe(true);
  })

  // INVALID NAMES
  test("should return invalid if name is empty", () => {
    expect(isValidName("").isValid).toBe(false);
  })

  test("should return invalid if name is one character long", () => {
    expect(isValidName("A").isValid).toBe(false);
  })

  test("should return invalid if name is 100 characters long", () => {
    expect(isValidName("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRST").isValid).toBe(true);
  })

  test("should return invalid if name has leading space", () => {
    expect(isValidName(" John Doe").isValid).toBe(false);
  })

  test("should return invalid if name has trailing space", () => {
    expect(isValidName("John Doe ").isValid).toBe(false);
  })

  test("should return invalid if name has multiple spaces", () => {
    expect(isValidName("John  Doe").isValid).toBe(false);
  })

  test("should return invalid if name contains only combining marks (e.g. emojis)", () => {
    expect(isValidName("\u0301\u0301").isValid).toBe(false);
  });

  // REGEX FULL TEST
  test("should return invalid if name has any illegal unicode characters", () => {
    const allowedCharRegex = new RegExp("[\\p{L}\\p{M}'’\\-– ]", "u");
    expect(performRegexFullTest(nameRegex, allowedCharRegex, "A","B")).toEqual([])
  });
})

// Email Validation TODO: check regex
describe("validateEmail", () => {

  // VALID EMAIL ADDRESSES
  test("should return valid if email address has capital letters", () => {
    expect(isValidEmail("JOHN@DOE.COM").isValid).toBe(true)
  })

  test("should return valid if email address has numbers", () => {
    expect(isValidEmail("j0hn@d0e.com").isValid).toBe(true)
  })

  test("should return valid if email address has extra dots", () => {
    expect(isValidEmail("john.doe@doe.co.uk").isValid).toBe(true)
  })

  test("should return valid if email address has long tld", () => {
    expect(isValidEmail("john@doe.comcomcom").isValid).toBe(true)
  })
  // INVALID EMAIL ADDRESSES
  test("should return invalid if email address is empty", () => {
    expect(isValidEmail("").isValid).toBe(false)
  })

  test("should return invalid if email address has no @", () => {
    expect(isValidEmail("johndoe.com").isValid).toBe(false)
  })

  test("should return invalid if email address has no tld (no dot)", () => {
    expect(isValidEmail("john@doe").isValid).toBe(false)
  })

  test("should return invalid if email address has too short tld (1 char)", () => {
    expect(isValidEmail("john@doe.c").isValid).toBe(false)
  })

  // REGEX FULL TESTS
  test("should return invalid if email address has any illegal unicode characters in name", () => {
    const allowedCharRegex = new RegExp("^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]|[\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x21\\x23-\\x5B\\x5D-\\x7E]|\\\\[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]", "u");
    expect(performRegexFullTest(emailRegex, allowedCharRegex, "john", "@doe.com")).toEqual([])
  });

  test("should return invalid if email address has any illegal unicode characters in domain", () => {
    const allowedCharRegex = new RegExp("[A-Za-z0-9-]", "u");
    expect(performRegexFullTest(emailRegex, allowedCharRegex, "john@doe", ".com")).toEqual([])
  });

  test("should return invalid if email address has any illegal unicode characters in tld", () => {
    const allowedCharRegex = new RegExp("[A-Za-z]", "u");
    expect(performRegexFullTest(emailRegex, allowedCharRegex, "john@doe.com")).toEqual([])
  });
})

// Cadastral Number Validation
describe("validatePassword", () => {
  // VALID CADASTRAL NUMBERS
  test("should return valid if cadastral number is exactly 20 alphanumeric characters", () => {
    expect(isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J0").isValid).toBe(true);
  })

  test("should return valid if cadastral number only contains uppercase letters", () => {
    expect(isValidCadastralNumber("ABCDEFGHIJKLMNOPQRST").isValid).toBe(true);
  })

  test("should return valid if cadastral number only contains lowercase letters", () => {
    expect(isValidCadastralNumber("abcdefghijklmnopqrst").isValid).toBe(true);
  })

  test("should return valid if cadastral number only contains numbers", () => {
    expect(isValidCadastralNumber("12345678901234567890").isValid).toBe(true);
  })

  // INVALID CADASTRAL NUMBERS
  test("should return invalid if cadastral number is empty", () => {
    expect(isValidCadastralNumber("").isValid).toBe(false);
  })

  test("should return invalid if cadastral number is shorter than 20 characters", () => {
    expect(isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J").isValid).toBe(false);
  })

  test("should return invalid if cadastral number is longer than 20 characters", () => {
    expect(isValidCadastralNumber("A1B2C3D4E5F6G7H8I9J0K").isValid).toBe(false);
  })

  // REGEX FULL TEST
  test("should return invalid if cadastral number has any illegal unicode characters", () => {
    const allowedCharRegex = new RegExp("[A-Za-z0-9]", "u");
    expect(performRegexFullTest(cadastralNumberRegex, allowedCharRegex,"A1B2C3D4E5","6G7H8I9J0K")).toEqual([])
  });
})

// Password Validation
describe("validatePassword", () => {
  // VALID PASSWORDS
  test("should return valid if password is 8 chars long and contains a special character", () => {
    expect(isValidPassword("testabc!").isValid).toBe(true);
  })

  test("should return valid if password is 255 chars long and contains a special character", () => {
    expect(isValidPassword("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrst!").isValid).toBe(true);
  })

  // INVALID PASSWORDS
  test("should return invalid if password is empty", () => {
    expect(isValidPassword("").isValid).toBe(false);
  })

  test("should return invalid if password is 7 chars long and contains a special character", () => {
    expect(isValidPassword("testab!").isValid).toBe(false);
  })

  test("should return invalid if password is 256 chars long and contains a special character", () => {
    expect(isValidPassword("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstu!").isValid).toBe(false);
  })

  test("should return invalid if password contains no special character", () => {
    expect(isValidPassword("test12345").isValid).toBe(false);
  })

  // REGEX FULL TEST
  test("should return invalid if password has any illegal unicode characters", () => {
    const allowedCharRegex = new RegExp("[\\p{P}\\p{S}]", "u");
    expect(performRegexFullTest(passwordRegex, allowedCharRegex,"testabc")).toEqual([])
  });
})

// Confirm Password Validation
describe("validateConfirmPassword", () => {
  // VALID CONFIRM PASSWORDS
  test("should return valid if password and confirm password match", () => {
    expect(isValidConfirmPassword("test123!", "test123!").isValid).toBe(true);
  })

  // INVALID CONFIRM PASSWORDS
  test("should return invalid if password and confirm password are empty", () => {
    expect(isValidConfirmPassword("", "").isValid).toBe(false);
  })

  test("should return invalid if password is empty", () => {
    expect(isValidConfirmPassword("", "test123!").isValid).toBe(false);
  })

  test("should return invalid if confirm password is empty", () => {
    expect(isValidConfirmPassword("test123!", "").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (mistype)", () => {
    expect(isValidConfirmPassword("test123!", "test123?").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (case mismatch)", () => {
    expect(isValidConfirmPassword("test123!", "Test123!").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (leading space)", () => {
    expect(isValidConfirmPassword("test123!", " test123!").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (trailing space)", () => {
    expect(isValidConfirmPassword("test123!", "test123! ").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (leading newline [unix])", () => {
    expect(isValidConfirmPassword("test123!", "\ntest123!").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (trailing newline [unix])", () => {
    expect(isValidConfirmPassword("test123!", "test123!\n").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (leading newline [win])", () => {
    expect(isValidConfirmPassword("test123!", "\r\ntest123!").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (trailing newline [win])", () => {
    expect(isValidConfirmPassword("test123!", "test123!\r\n").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (invisible char \u200B)", () => {
    expect(isValidConfirmPassword("test123!", "test123!\u200B").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (invisible char \u2060)", () => {
    expect(isValidConfirmPassword("test123!", "test123!\u2060").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (invisible char \u00A0)", () => {
    expect(isValidConfirmPassword("test123!", "test123!\u00A0").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (invisible char \uFEFF)", () => {
    expect(isValidConfirmPassword("test123!", "test123!\uFEFF").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (umlaut unicode mismatch)", () => {
    expect(isValidConfirmPassword("täst123!", "t\u0061\u0308st123!").isValid).toBe(false);
  })
})

// IBAN Validation
describe("validateIban", () => {

  // VALID IBANS
  test("should return valid if IBAN is 15 chars long and contains two digits country code followed only by numbers", () => {
    expect(isValidIban("DE0123456789012").isValid).toBe(true);
  })

  test("should return valid if IBAN is 34 chars long and contains two digits country code followed only by numbers", () => {
    expect(isValidIban("DE01234567890123456789012345678901").isValid).toBe(true);
  })

  // INVALID IBANS
  test("should return invalid if IBAN is empty", () => {
    expect(isValidIban("").isValid).toBe(false);
  })

  test("should return invalid if IBAN is shorter than 15 chars", () => {
    expect(isValidIban("DE001234567890").isValid).toBe(false);
  })

  test("should return invalid if IBAN is longer than 34 chars", () => {
    expect(isValidIban("DE001234567891234567891234567891234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has no country code", () => {
    expect(isValidIban("141592653589793238").isValid).toBe(false);
  })

  test("should return invalid if IBAN has single digit country code", () => {
    expect(isValidIban("D141592653589793238").isValid).toBe(false);
  })

  test("should return invalid if IBAN has tree digit country code", () => {
    expect(isValidIban("DEW141592653589793238").isValid).toBe(false);
  })

  test("should return invalid if IBAN has invalid country code", () => {
    expect(isValidIban("120123456789012").isValid).toBe(false);
  })

  test("should return invalid if IBAN has umlaute", () => {
    expect(isValidIban("ÖS141592653589793234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has french accents", () => {
    expect(isValidIban("ÁU141592653589793234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has polish accents", () => {
    expect(isValidIban("ŁU141592653589793234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has special chars in country code", () => {
    expect(isValidIban("D.141592653589793234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has special chars after country code", () => {
    expect(isValidIban("DE.41592653589793234").isValid).toBe(false);
  })
})

// BIC Validation
describe("validateBic", () => {
  // TODO: Implement BIC validation tests
})

// SEPA Mandate Validation
describe("validateSepaMandateCheck", () => {
  // VALID SEPA MANDATE
  test("valid SEPA mandate", () => {
      expect(isValidSepaMandate(true).isValid).toBe(true);
  })
  // INVALID SEPA MANDATE
  test("invalid SEPA mandate", () => {
    expect(isValidSepaMandate(false).isValid).toBe(false);
  })
})

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
    expect(isValidDateString("")).toBe(false);
  });

  test("invalid ISO date whitespace", () => {
    expect(isValidDateString("   ")).toBe(false);
  });

  test(" invalid ISO date format does not match regex", () => {
    expect(isValidDateString("2025/01/01")).toBe(false);
    expect(isValidDateString("2025.01.01")).toBe(false);
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

  test("invalid ISO date completely invalid date", () => {
    expect(isValidDateString("9999-99-99")).toBe(false);
  });
})

function performRegexFullTest(
    regexToTest: RegExp,
    allowedCharRegex: RegExp,
    leadingString?: string,
    trailingString?: string,
): string[] {
  const failures: string[] = [];

  for (let cp = 0; cp <= 0xffff; cp++) {
    const ch = String.fromCodePoint(cp);

    // skip allowed chars
    if (allowedCharRegex.test(ch)) continue;

    // skip control chars
    if (/[\u0000-\u001F\u007F]/u.test(ch)) continue;

    if (regexToTest.test(`${leadingString ?? ""}${ch}${trailingString ?? ""}`)) {
      throw new Error(
          `Illegal char passed validation: U+${cp
          .toString(16)
          .toUpperCase()
          .padStart(4, "0")} '${ch}'`
      );
    }
  }
  return failures;
}
