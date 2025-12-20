import {t} from "i18next";

// Regex for names: letters (including accented), marks, apostrophes, hyphens, single spaces
export const nameRegex = new RegExp(
    "^\\p{L}(?:[\\p{L}\\p{M}\'’\\-–]*" +
    "[\\p{L}\\p{M}])?(?: \\p{L}(?:[\\p{L}\\p{M}\'’\\-]*[\\p{L}\\p{M}])?)*$",
    "u"
);

// Regex for emails based on HTML 5 specification
export const emailRegex = new RegExp(
    "^(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+" +
    "(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
    "|" +
    '"(?:[\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x21\\x23-\\x5B\\x5D-\\x7E]|\\\\[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F])*"' +
    ")" +
    "@" +
    "(?:(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)\\.)+" +
    "[A-Za-z]{2,}$"
);

// Regex for cadastral number: exactly 20 alphanumeric characters
export const cadastralNumberRegex = new RegExp("^[A-Za-z0-9]*$");

// Regex for password: at least one special character (punctuation or symbol)
export const passwordRegex = new RegExp(".*[\\p{P}\\p{S}].*", "u"
);

// Regex for IBAN: 15 to 34 characters, first two letters, then 2 numbers, then 11 to 30 alphanumeric characters
export const ibanRegex = new RegExp("^[A-Z]{2}[0-9]{2}[0-9A-Z]{11,30}$", "i");

// Regex for BIC: 8 or 11 characters, first 6 letters, then 2 alphanumeric, optional 3 alphanumeric
export const bicRegex = new RegExp("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", "i");

// Regex for date in YYYY-MM-DD format
export const dateRegex = new RegExp("^\\d{4}-\\d{2}-\\d{2}$");

// Allowed file types & size for document uploads
const ALLOWED_TYPES = ["application/pdf"];
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB in bytes

// Name Validation
export const isValidName = (name: string | undefined | null) => {
  if (!name || name.trim() === "")
    return {isValid: false, message: t("validation.name.required")};

  if (name.length < 2)
    return {
      isValid: false,
      message: t("validation.name.minLength"),
    };

  if (name.length >= 100)
    return {
      isValid: false,
      message: t("validation.name.maxLength"),
    };

  if (!nameRegex.test(name))
    return {
      isValid: false,
      message: t("validation.name.invalid"),
    };

  return {isValid: true, message: t("validation.name.valid")};
};

// Email validation
export const isValidEmail = (email: string | undefined | null) => {
  if (!email || email.trim() === "")
    return {isValid: false, message: t("validation.email.required")};

  if (email.length < 5)
    return {
      isValid: false,
      message: t("validation.email.minLength"),
    };

  if (email.length >= 255)
    return {
      isValid: false,
      message: t("validation.email.maxLength"),
    };

  if (!emailRegex.test(email))
    return {
      isValid: false,
      message: t("validation.email.invalid"),
    };

  return {isValid: true, message: t("validation.email.valid")};
};

// Cadastral number validation: exactly 20 characters long
export const isValidCadastralNumber = (cadastral_number: string | undefined | null) => {
  if (!cadastral_number || cadastral_number.trim() === "")
    return {
      isValid: false,
      message: t("validation.cadastral_number.required"),
    };

  if (cadastral_number.length != 20)
    return {
      isValid: false,
      message: t("validation.cadastral_number.length"),
    };

  if (!cadastralNumberRegex.test(cadastral_number))
    return {
      isValid: false,
      message: t("validation.cadastral_number.invalid"),
    };

  return {isValid: true, message: t("validation.cadastral_number.valid")};
};

// Password validation: min. 8 and max. 255 characters long and at least one special character
export const isValidPassword = (password: string | undefined | null) => {
  if (!password || password.trim() === "")
    return {isValid: false, message: t("validation.password.required")};

  if (password.length < 8)
    return {
      isValid: false,
      message: t("validation.password.minLength"),
    };
  // avoid cutting off passwords that are too long for database
  if (password.length >= 255)
    return {
      isValid: false,
      message: t("validation.password.maxLength"),
    };

  // At least one special character (non letter or number)
  if (!passwordRegex.test(password))
    return {
      isValid: false,
      message: t("validation.password.specialChar"),
    };

  return {isValid: true, message: t("validation.password.valid")};
};

// Confirm Password validation: must match password
export const isValidConfirmPassword = (
    password: string | undefined | null,
    confirmPassword: string | undefined | null
) => {
  if (!confirmPassword || confirmPassword.trim() === "")
    return {
      isValid: false,
      message: t("validation.confirmPassword.required"),
    };

  if (password !== confirmPassword)
    return {
      isValid: false,
      message: t("validation.confirmPassword.mismatch"),
    };

  return {isValid: true, message: t("validation.confirmPassword.valid")};
};

// File validation for document uploads (file type and size)
export const isValidFile = (file: File | undefined | null) => {
  if (!file)
    return {
      isValid: false,
      message: t("license.document_upload.input.fileRequired"),
    };

  if (!ALLOWED_TYPES.includes(file.type))
    return {
      isValid: false,
      message: t("license.document_upload.input.fileTypeError"),
    };

  if (file.size > MAX_FILE_SIZE)
    return {
      isValid: false,
      message: t("license.document_upload.input.fileTypeError"),
    };

  return {isValid: true, message: t("license.document_upload.valid")};
};

// IBAN validation: length between 15 and 34 characters, valid characters
export const isValidIban = (iban: string | undefined | null) => {
  if (!iban || iban.trim() === "")
    return {isValid: false, message: t("validation.iban.required")};

  if (iban.length < 15)
    return {
      isValid: false,
      message: t("validation.iban.minLength"),
    };

  if (iban.length >= 35)
    return {
      isValid: false,
      message: t("validation.iban.maxLength"),
    };

  if (!ibanRegex.test(iban))
    return {
      isValid: false,
      message: t("validation.iban.specialChar"),
    };

  return {isValid: true, message: t("validation.iban.valid")};
};

export const isValidBic = (bic: string | undefined | null, iban: string | undefined | null) => {
  if (!iban || iban.trim() === "")
    return {isValid: false, message: t("validation.iban.required")};

  if (iban.slice(0, 2) === 'ES' && (!bic || bic.trim() === ""))
    return {isValid: true, message: t("validation.bic.valid")};

  if (!bic || bic.trim() === "")
    return {isValid: false, message: t("validation.bic.required")};

  if (bic.length != 8 && bic.length != 11)
    return {
      isValid: false,
      message: t("validation.bic.length"),
    };

  if (!bicRegex.test(bic))
    return {
      isValid: false,
      message: t("validation.bic.specialChar"),
    };

  return {isValid: true, message: t("validation.iban.valid")};
};

export const isValidDateString = (value: string | undefined | null): boolean => {
  if(!value || value.trim() === "")
    return false;

  if (!dateRegex.test(value))
    return false;

  const date = new Date(value);

  // Check that date is valid and not auto-corrected by JS
  return !isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value;
};

// not used currently
/*
export const validatePhoneNumber = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.phone.required") };

  const phoneRegex = new RegExp("^[+]?[(]?[0-9]{1,4}[)]?[-\\s0-9]{5,15}$");

  if (!phoneRegex.test(value))
    return {
      isValid: false,
      message: t("validation.phone.invalid"),
    };

  return { isValid: true, message: t("validation.phone.valid") };
};

export const validateReason = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.reason.required") };

  if (value.length < 3)
    return {
      isValid: false,
      message: t("validation.reason.minLength"),
    };

  if (value.length > 500)
    return {
      isValid: false,
      message: t("validation.reason.maxLength"),
    };

  return { isValid: true, message: t("validation.reason.valid") };
};

export const validateAddress = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.address.required") };

  // Letters, numbers, spaces, punctuation
  const addressRegex = new RegExp("^[\\p{L}\\p{N}\\p{M}'’.,\\-–/ ]+$", "u");

  if (value.length < 3)
    return {
      isValid: false,
      message: t("validation.address.minLength"),
    };

  if (!addressRegex.test(value))
    return {
      isValid: false,
      message: t("validation.address.invalid"),
    };

  return { isValid: true, message: t("validation.address.valid") };
};

export const validatePostalCode = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.postalCode.required") };

  const postalRegex = new RegExp("^[A-Za-z0-9\\s-]{3,12}$");

  if (!postalRegex.test(value))
    return {
      isValid: false,
      message: t("validation.postalCode.invalid"),
    };

  return { isValid: true, message: t("validation.postalCode.valid") };
};

export const validateCity = (value: string) => {
  if (!value) return { isValid: false, message: t("validation.city.required") };

  const cityRegex = new RegExp("^[\\p{L}\\p{M}'’\\-– ]+$", "u");

  if (!cityRegex.test(value))
    return {
      isValid: false,
      message: t("validation.city.invalid"),
    };

  return { isValid: true, message: t("validation.city.valid") };
};
*/
