import {
  cadastralNumberRegex,
  emailRegex,
  nameRegex,
  passwordRegex,
  validateCadastralNumber,
  validateConfirmPassword,
  validateEmail,
  validateIban,
  validateName,
  validatePassword
} from "./validationRules";

// Name Validation
describe("validateName", () => {

  // VALID NAMES TODO: check regex
  test("should return valid if name is two characters long", () => {
    expect(validateName("JD").isValid).toBe(true);
  })

  test("should return valid if name is two single characters long with space in between", () => {
    expect(validateName("J D").isValid).toBe(true);
  })

  test("should return valid if name is 99 characters long", () => {
    expect(validateName("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRST").isValid).toBe(true);
  })

  // INVALID NAMES
  test("should return invalid if name is empty", () => {
    expect(validateName("").isValid).toBe(false);
  })

  test("should return invalid if name is one character long", () => {
    expect(validateName("A").isValid).toBe(false);
  })

  test("should return invalid if name is 100 characters long", () => {
    expect(validateName("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRST").isValid).toBe(true);
  })

  test("should return invalid if name has leading space", () => {
    expect(validateName(" John Doe").isValid).toBe(false);
  })

  test("should return invalid if name has trailing space", () => {
    expect(validateName("John Doe ").isValid).toBe(false);
  })

  test("should return invalid if name has multiple spaces", () => {
    expect(validateName("John  Doe").isValid).toBe(false);
  })

  test("should return invalid if name contains only combining marks (e.g. emojis)", () => {
    expect(validateName("\u0301\u0301").isValid).toBe(false);
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
    expect(validateEmail("JOHN@DOE.COM").isValid).toBe(true)
  })

  test("should return valid if email address has numbers", () => {
    expect(validateEmail("j0hn@d0e.com").isValid).toBe(true)
  })

  test("should return valid if email address has extra dots", () => {
    expect(validateEmail("john.doe@doe.co.uk").isValid).toBe(true)
  })

  test("should return valid if email address has long tld", () => {
    expect(validateEmail("john@doe.comcomcom").isValid).toBe(true)
  })
  // INVALID EMAIL ADDRESSES
  test("should return invalid if email address is empty", () => {
    expect(validateEmail("").isValid).toBe(false)
  })

  test("should return invalid if email address has no @", () => {
    expect(validateEmail("johndoe.com").isValid).toBe(false)
  })

  test("should return invalid if email address has no tld (no dot)", () => {
    expect(validateEmail("john@doe").isValid).toBe(false)
  })

  test("should return invalid if email address has too short tld (1 char)", () => {
    expect(validateEmail("john@doe.c").isValid).toBe(false)
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
    expect(validateCadastralNumber("A1B2C3D4E5F6G7H8I9J0").isValid).toBe(true);
  })

  test("should return valid if cadastral number only contains uppercase letters", () => {
    expect(validateCadastralNumber("ABCDEFGHIJKLMNOPQRST").isValid).toBe(true);
  })

  test("should return valid if cadastral number only contains lowercase letters", () => {
    expect(validateCadastralNumber("abcdefghijklmnopqrst").isValid).toBe(true);
  })

  test("should return valid if cadastral number only contains numbers", () => {
    expect(validateCadastralNumber("12345678901234567890").isValid).toBe(true);
  })

  // INVALID CADASTRAL NUMBERS
  test("should return invalid if cadastral number is empty", () => {
    expect(validateCadastralNumber("").isValid).toBe(false);
  })

  test("should return invalid if cadastral number is shorter than 20 characters", () => {
    expect(validateCadastralNumber("A1B2C3D4E5F6G7H8I9J").isValid).toBe(false);
  })

  test("should return invalid if cadastral number is longer than 20 characters", () => {
    expect(validateCadastralNumber("A1B2C3D4E5F6G7H8I9J0K").isValid).toBe(false);
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
    expect(validatePassword("testabc!").isValid).toBe(true);
  })

  test("should return valid if password is 255 chars long and contains a special character", () => {
    expect(validatePassword("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrst!").isValid).toBe(true);
  })

  // INVALID PASSWORDS
  test("should return invalid if password is empty", () => {
    expect(validatePassword("").isValid).toBe(false);
  })

  test("should return invalid if password is 7 chars long and contains a special character", () => {
    expect(validatePassword("testab!").isValid).toBe(false);
  })

  test("should return invalid if password is 256 chars long and contains a special character", () => {
    expect(validatePassword("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstu!").isValid).toBe(false);
  })

  test("should return invalid if password contains no special character", () => {
    expect(validatePassword("test12345").isValid).toBe(false);
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
    expect(validateConfirmPassword("test123!", "test123!").isValid).toBe(true);
  })

  // INVALID CONFIRM PASSWORDS
  test("should return invalid if password and confirm password are empty", () => {
    expect(validateConfirmPassword("", "").isValid).toBe(false);
  })

  test("should return invalid if password is empty", () => {
    expect(validateConfirmPassword("", "test123!").isValid).toBe(false);
  })

  test("should return invalid if confirm password is empty", () => {
    expect(validateConfirmPassword("test123!", "").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (mistype)", () => {
    expect(validateConfirmPassword("test123!", "test123?").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (case mismatch)", () => {
    expect(validateConfirmPassword("test123!", "Test123!").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (leading space)", () => {
    expect(validateConfirmPassword("test123!", " test123!").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (trailing space)", () => {
    expect(validateConfirmPassword("test123!", "test123! ").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (leading newline [unix])", () => {
    expect(validateConfirmPassword("test123!", "\ntest123!").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (trailing newline [unix])", () => {
    expect(validateConfirmPassword("test123!", "test123!\n").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (leading newline [win])", () => {
    expect(validateConfirmPassword("test123!", "\r\ntest123!").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (trailing newline [win])", () => {
    expect(validateConfirmPassword("test123!", "test123!\r\n").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (invisible char \u200B)", () => {
    expect(validateConfirmPassword("test123!", "test123!\u200B").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (invisible char \u2060)", () => {
    expect(validateConfirmPassword("test123!", "test123!\u2060").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (invisible char \u00A0)", () => {
    expect(validateConfirmPassword("test123!", "test123!\u00A0").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (invisible char \uFEFF)", () => {
    expect(validateConfirmPassword("test123!", "test123!\uFEFF").isValid).toBe(false);
  })

  test("should return invalid if passwords not matching (umlaut unicode mismatch)", () => {
    expect(validateConfirmPassword("täst123!", "t\u0061\u0308st123!").isValid).toBe(false);
  })
})

// IBAN Validation
describe("validateIban", () => {

  // VALID IBANS
  test("should return valid if IBAN is 15 chars long and contains two digits country code followed only by numbers", () => {
    expect(validateIban("DE0123456789012").isValid).toBe(true);
  })

  test("should return valid if IBAN is 34 chars long and contains two digits country code followed only by numbers", () => {
    expect(validateIban("DE01234567890123456789012345678901").isValid).toBe(true);
  })

  // INVALID IBANS
  test("should return invalid if IBAN is empty", () => {
    expect(validateIban("").isValid).toBe(false);
  })

  test("should return invalid if IBAN is shorter than 15 chars", () => {
    expect(validateIban("DE001234567890").isValid).toBe(false);
  })

  test("should return invalid if IBAN is longer than 34 chars", () => {
    expect(validateIban("DE001234567891234567891234567891234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has no country code", () => {
    expect(validateIban("141592653589793238").isValid).toBe(false);
  })

  test("should return invalid if IBAN has single digit country code", () => {
    expect(validateIban("D141592653589793238").isValid).toBe(false);
  })

  test("should return invalid if IBAN has tree digit country code", () => {
    expect(validateIban("DEW141592653589793238").isValid).toBe(false);
  })

  test("should return invalid if IBAN has invalid country code", () => {
    expect(validateIban("120123456789012").isValid).toBe(false);
  })

  test("should return invalid if IBAN has umlaute", () => {
    expect(validateIban("ÖS141592653589793234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has french accents", () => {
    expect(validateIban("ÁU141592653589793234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has polish accents", () => {
    expect(validateIban("ŁU141592653589793234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has special chars in country code", () => {
    expect(validateIban("D.141592653589793234").isValid).toBe(false);
  })

  test("should return invalid if IBAN has special chars after country code", () => {
    expect(validateIban("DE.41592653589793234").isValid).toBe(false);
  })
})

// BIC Validation
describe("validateBic", () => {
  // TODO: Implement BIC validation tests
})

// SEPA Mandate Validation
describe("validateSepaMandateCheck", () => {
  /*
  // VALID SEPA MANDATE
  test("valid SEPA mandate", () => {
      expect(validateSepaMandateCheck(true).isValid).toBe();
  })
  // INVALID SEPA MANDATE
  test("invalid SEPA mandate", () => {
      expect(validateSepaMandateCheck(false);
      expect(result).toEqual({
          isValid: false,
      });
  }*/
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
