import {
    //validateBic,
    validateConfirmPassword,
    validateEmail,
    //validateIban,
    validateName,
    validatePassword,
    validateSepaMandateCheck
} from "./validationRules";

// Name Validation
describe("validateName", () => {
    // VALID NAMES
    test("should return valid if name has apostrophs", () => {
        const result = validateName("D'Arcy O’Connor");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if name has umlaute and en-dashes (u+2013)", () => {
        const result = validateName("Älfrüd Ömër Bärbel Gößlöw–Bräunhöz");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if name has french accents and hyphen", () => {
        const result = validateName("Álvaro Maëlys-Célèste Dûçânoët");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if name has polish accents", () => {
        const result = validateName("Łukasz Jiří–Žižka");
        expect(result).toEqual({
            isValid: true,
        });
    })
    // INVALID NAMES
    test("should return invalid if name is empty", () => {
        const result = validateName("");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name is one character", () => {
        const result = validateName("?");
        expect(result).toEqual({
            isValid: false, // Min length check runs first
        });
    })

    test("should return invalid if name has numbers", () => {
        const result = validateName("A1fred");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has dots", () => {
        const result = validateName("Prof. Dr. Peter Heusch");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has commas", () => {
        const result = validateName("Doe, Jane");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has several spaces", () => {
        const result = validateName("John  Doe");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has exclamation marks", () => {
        const result = validateName("VfB Stuttgart!");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has question marks", () => {
        const result = validateName("What?");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has underscores", () => {
        const result = validateName("snake_case");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has hashtags", () => {
        const result = validateName("#coolname");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has @ symbols", () => {
        const result = validateName("M@llorca");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has & symbols", () => {
        const result = validateName("Tom&Jerry");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has wildcard symbols", () => {
        const result = validateName("*Wildcard");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has wildcard currency symbols", () => {
        const result = validateName("¤");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has BTC symbols", () => {
        const result = validateName("₿");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has EUR symbols", () => {
        const result = validateName("€");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has GBP symbols", () => {
        const result = validateName("£");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has INR symbols", () => {
        const result = validateName("₹");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has USD symbols", () => {
        const result = validateName("$");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has KRW symbols", () => {
        const result = validateName("₩");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has JPY symbols", () => {
        const result = validateName("¥");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has paragraph symbols", () => {
        const result = validateName("§ 185 StGB");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has quotes (double)", () => {
        const result = validateName("\"Quote\"");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has backtick quotes", () => {
        const result = validateName("`Quote`");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has slashes", () => {
        const result = validateName("A/B Testing");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has backslashes", () => {
        const result = validateName("\\Escape");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has 'greater' symbols", () => {
        const result = validateName("Stuttgart>Karlsruhe");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has 'less' symbols", () => {
        const result = validateName("Karlsruhe<Stuttgart");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has 'equals' symbols", () => {
        const result = validateName("A=B");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has colons", () => {
        const result = validateName("This:");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has semicolons", () => {
        const result = validateName("Java;");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has trademark symbols", () => {
        const result = validateName("TM™");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has copyright symbols", () => {
        const result = validateName("©2024");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if name has register symbols", () => {
        const result = validateName("Registered®");
        expect(result).toEqual({
            isValid: false,
        });
    })
})

// Email Validation
describe("validateEmail", () => {
    // VALID EMAIL ADDRESSES
    test("should return valid if email address has capital letters", () => {
        const result = validateEmail("Alfred@Test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has numbers", () => {
        const result = validateEmail("a1fred@1test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has extra dots", () => {
        const result = validateEmail("peter.heusch@test.co.uk");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has exclamation marks in name", () => {
        const result = validateEmail("vfb!stuttgart@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has question marks in name", () => {
        const result = validateEmail("what?@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has underscores in name", () => {
        const result = validateEmail("snake_case@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has wildcard symbols in name", () => {
        const result = validateEmail("*@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has USD symbols in name", () => {
        const result = validateEmail("$@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has & symbols in name", () => {
        const result = validateEmail("tom&jerry@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has quotes (double) in name", () => {
        const result = validateEmail("\"quote\"@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has backtick quotes in name", () => {
        const result = validateEmail("`quote`@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has slashes in name", () => {
        const result = validateEmail("a/b@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has 'equals' symbols in name", () => {
        const result = validateEmail("a=b@test.com");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if email address has long tld", () => {
        const result = validateEmail("test@test.hftstuttgart");
        expect(result).toEqual({
            isValid: true,
        });
    })
    // INVALID EMAIL ADDRESSES
    test("should return invalid if email address is empty", () => {
        const result = validateEmail("");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has no @", () => {
        const result = validateEmail("test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has no dot", () => {
        const result = validateEmail("test@com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has no tld (no dot)", () => {
        const result = validateEmail("test@com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has too short tld (1 char)", () => {
        const result = validateEmail("test@test.c");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has number in tld", () => {
        const result = validateEmail("test@test.com1");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has hyphen in tld", () => {
        const result = validateEmail("test@test.co-m");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has apostrophs", () => {
        const result = validateEmail("d'arcy@o’connor.ir");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has umlaute", () => {
        const result = validateEmail("älfrüdömërbärbelgößlöw@bräunhöz.de");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has french accents", () => {
        const result = validateEmail("álvaromaëlyscélèste@ûçânoët.fr");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has polish accents", () => {
        const result = validateEmail("łukasz@jiřížižka.pl");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has commas", () => {
        const result = validateEmail("doe,jane@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has exclamation marks in domain", () => {
        const result = validateEmail("vfb!stuttgart@test!.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has question marks in domain", () => {
        const result = validateEmail("what?@test?.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has underscores in domain", () => {
        const result = validateEmail("snake_case@te_st.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has & symbols in name", () => {
        const result = validateEmail("tom&jerry@test&.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has quotes (double) in domain", () => {
        const result = validateEmail("\"quote\"@\"test\".com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has backtick quotes in domain", () => {
        const result = validateEmail("`quote`@`test`.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has spaces", () => {
        const result = validateEmail("john doe@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has several @ symbols", () => {
        const result = validateEmail("m@llorca@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has & symbols", () => {
        const result = validateEmail("tom@tom&jerry.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has wildcard symbols in domain", () => {
        const result = validateEmail("wildcard@*.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has wildcard currency symbols", () => {
        const result = validateEmail("¤@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has BTC symbols", () => {
        const result = validateEmail("₿@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has EUR symbols", () => {
        const result = validateEmail("€@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has GBP symbols", () => {
        const result = validateEmail("£@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has INR symbols", () => {
        const result = validateEmail("₹@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has USD symbols in domain", () => {
        const result = validateEmail("us@$.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has KRW symbols", () => {
        const result = validateEmail("₩@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has JPY symbols", () => {
        const result = validateEmail("¥@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has paragraph symbols", () => {
        const result = validateEmail("§185StGB@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has backslashes", () => {
        const result = validateEmail("\\Escape@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has 'greater' symbols", () => {
        const result = validateEmail("Stuttgart>Karlsruhe@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has 'less' symbols", () => {
        const result = validateEmail("Karlsruhe<Stuttgart@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has slashes in domain", () => {
        const result = validateEmail("a/b@te/st.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has 'equals' symbols in domain", () => {
        const result = validateEmail("test@a=b.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has colons", () => {
        const result = validateEmail("this:@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has semicolons", () => {
        const result = validateEmail("java;@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has trademark symbols", () => {
        const result = validateEmail("tm™@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has copyright symbols", () => {
        const result = validateEmail("©2024@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if email address has register symbols", () => {
        const result = validateEmail("registered®@test.com");
        expect(result).toEqual({
            isValid: false,
        });
    })
})

// Password Validation
describe("validatePassword", () => {
    // VALID PASSWORDS
    test("should return valid if password is longer than 7 chars, shorter than 256 chars and contains at least one special character", () => {
        const result = validatePassword("test-123!");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if password is longer than 7 chars, shorter than 256 chars and contains at least one special character", () => {
        const result = validatePassword("%\"*(4fIh1M2z3fv7wM}J9xxAc4(\"N/9SAt({_!\\>{Sh:Lw_\"QJv9_y-I;`UdGBF&+VW?3)5o%pqT5H%'47S7=wDyAZR+&Ib*'>Y?J.P\"61j@8q0,pkm4ieFAV7wd']:)s;K^wSHvS}V~&T\\pXJR%3jWd0|NkAOF56?3pStc<39ds:XK\\*AR<P/IeVva]OLPh^I#h5_LBf<'£50e?vLCD'r}Lb3KfA*Q:{BM]7~?bK?5Q9>*R9[0lL!nfl#d1E11");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if password is longer than 7 chars, shorter than 256 chars and contains at least one special character", () => {
        const result = validatePassword("olaf-scholz");
        expect(result).toEqual({
            isValid: true,
        });
    })

    // INVALID PASSWORDS
    test("should return invalid if password is empty", () => {
        const result = validatePassword("");
        expect(result).toEqual({
            isValid: false,
        });
    })
    test("should return invalid if password is shorter than 8 chars", () => {
        const result = validatePassword("test12!");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if password is longer than 255 chars", () => {
        const result = validatePassword("%\"*(4fIh1M2z3fv7wM}J9xxAc4(\"N/9SAt({_!\\>{Sh:Lw_\"QJv9_y-I;`UdGBF&+VW?3)5o%pqT5H%'47S7=wDyAZR+&Ib*'>Y?J.P\"61j@8q0,pkm4ieFAV7wd']:)s;K^wSHvS}V~&T\\pXJR%3jWd0|NkAOF56?3pStc<39ds:XK\\*AR<P/IeVva]OLPh^I#h5_LBf<'£50e?vLCD'r}Lb3KfA*Q:{BM]7~?bK?5Q9>*R9[0lL!nfl#d1E11s");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if password contains no special character", () => {
        const result = validatePassword("Test1234");
        expect(result).toEqual({
            isValid: false,
        });
    })
})

// Confirm Password Validation
describe("validateConfirmPassword", () => {
    // VALID CONFIRM PASSWORDS
    test("should return valid if password and confirm password match", () => {
        const result = validateConfirmPassword("test-123!", "test-123!");
        expect(result).toEqual({
            isValid: true,
        });
    })

    // INVALID CONFIRM PASSWORDS
    test("should return invalid if password and confirm password are empty", () => {
        const result = validateConfirmPassword("", "");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if password is empty", () => {
        const result = validateConfirmPassword("", "test");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if confirm password is empty", () => {
        const result = validateConfirmPassword("test", "");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (mistype)", () => {
        const result = validateConfirmPassword("test", "tset");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (case mismatch)", () => {
        const result = validateConfirmPassword("test", "Test");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (leading space)", () => {
        const result = validateConfirmPassword("test", " test");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (trailing space)", () => {
        const result = validateConfirmPassword("test", "test ");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (leading newline [unix])", () => {
        const result = validateConfirmPassword("test", "\ntest");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (trailing newline [unix])", () => {
        const result = validateConfirmPassword("test", "test\n");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (leading newline [win])", () => {
        const result = validateConfirmPassword("test", "\r\ntest");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (trailing newline [win])", () => {
        const result = validateConfirmPassword("test", "test\r\n");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (invisible char \u200B)", () => {
        const result = validateConfirmPassword("test", "test\u200B");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (invisible char \u2060)", () => {
        const result = validateConfirmPassword("test", "test\u2060");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (invisible char \u00A0)", () => {
        const result = validateConfirmPassword("test", "test\u00A0");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (invisible char \uFEFF)", () => {
        const result = validateConfirmPassword("test", "test\uFEFF");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if passwords not matching (umlaut unicode mismatch)", () => {
        const result = validateConfirmPassword("täst", "t\u0061\u0308st");
        expect(result).toEqual({
            isValid: false,
        });
    })
})

// IBAN Validation
describe("validateIban", () => {
  // TODO: Fix IBAN validation tests
    /*
    // VALID IBANS
    test("should return valid if IBAN is longer than 14 chars and contains valid country code followed only by numbers", () => {
        const result = validateIban("DE3301234567890");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if IBAN contains multiple spaces and valid country code followed only by numbers", () => {
        const result = validateIban("FR00 1123 5813 2134");
        expect(result).toEqual({
            isValid: true,
        });
    })

    test("should return valid if IBAN is shorter than 35 chars and contains valid country code followed only by numbers", () => {
        const result = validateIban(" es  01  01  01  01  01  01  01  01  01  01  01  01  01  01  01  01 ");
        expect(result).toEqual({
            isValid: true,
        });
    })

    // INVALID IBANS
    test("should return invalid if IBAN is empty", () => {
        const result = validateIban("");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN is shorter than 15 chars", () => {
        const result = validateIban("DE001234567890");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN is longer than 34 chars", () => {
        const result = validateIban("DE001234567891234567891234567891234");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has no country code", () => {
        const result = validateIban("141592653589793238");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has single digit country code", () => {
        const result = validateIban("D141592653589793238");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has tree digit country code", () => {
        const result = validateIban("DEW141592653589793238");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has invalid country code", () => {
        const result = validateIban("XX141592653589793238");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has digits after country code", () => {
        const result = validateIban("DE14159265358979323A");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has umlaute", () => {
        const result = validateIban("ÖS141592653589793234");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has french accents", () => {
        const result = validateIban("ÁU141592653589793234");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has polish accents", () => {
        const result = validateIban("ŁU141592653589793234");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has special chars in country code", () => {
        const result = validateIban("D.141592653589793234");
        expect(result).toEqual({
            isValid: false,
        });
    })

    test("should return invalid if IBAN has special chars after country code", () => {
        const result = validateIban("DE.41592653589793234");
        expect(result).toEqual({
            isValid: false,
        });
    })*/
})

// BIC Validation
describe("validateBic", () => {
    // TODO: Implement BIC validation tests
})

// SEPA Mandate Validation
describe("validateSepaMandateCheck", () => {
    // VALID SEPA MANDATE
    test("valid SEPA mandate", () => {
        const result = validateSepaMandateCheck(true);
        expect(result).toEqual({
            isValid: true,
        });
    })
    // INVALID SEPA MANDATE
    test("invalid SEPA mandate", () => {
        const result = validateSepaMandateCheck(false);
        expect(result).toEqual({
            isValid: false,
        });
    })
})
