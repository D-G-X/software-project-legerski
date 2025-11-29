import { t } from "i18next";

// Regular expressions for validation rules (positive checks only, test with !regexName.test(value))
const nameRegex = new RegExp(
    "^[\\p{L}\\p{M}'’\\-–]+(?: [\\p{L}\\p{M}'’\\-–]+)*$",
    "u"
);

const emailRegex = new RegExp(
    "^(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+" +
    "(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
    "|" +
    "\"(?:[\\x21-\\x7E]|\\\\[\\x21-\\x7E])+\"" +
    ")" +
    "@" +
    "(?:(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)\\.)+" +
    "(?:[A-Za-z]{2,})$",
);

const ibanRegex = new RegExp("^[A-Z]{2}[0-9A-Z]{13,32}$", "i");

const bicRegex = new RegExp("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$", "i");

const passwordRegex = new RegExp("[\\p{P}\\p{S}]", "u");

// Name Validation
export const validateName = (name: string) => {
  if (!name) return { isValid: false, message: t("validation.name.required") };

  if (name.length < 2)
    return {
      isValid: false,
      message: t("validation.name.minLength"),
    };

  if (!nameRegex.test(name))
    return {
      isValid: false,
      message: t("validation.name.invalid"),
    };

  return { isValid: true, message: t("validation.name.valid") };
};

// Email validation
export const validateEmail = (email: string) => {
  if (!email)
    return { isValid: false, message: t("validation.email.required") };

  if (!emailRegex.test(email))
    return{
        isValid: false,
        message: t("validation.email.invalid")
    };

  return { isValid: true, message: t("validation.email.valid") };
};

// Password validation: min. 8 and max. 255 characters long and at least one special character
export const validatePassword = (password: string) => {
  if (!password)
    return { isValid: false, message: t("validation.password.required") };

  if (password.length < 8)
    return {
      isValid: false,
      message: t("validation.password.minLength"),
    };
  // avoid cutting off passwords that are too long for database
  if (password.length > 255)
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

  return { isValid: true, message: t("validation.password.valid") };
};

// Confirm Password validation
export const validateConfirmPassword = (
    password: string,
    confirmPassword: string
) => {
    if (!confirmPassword)
        return {
            isValid: false,
            message: t("validation.confirmPassword.required"),
        };

    if (password !== confirmPassword)
        return {
            isValid: false,
            message: t("validation.confirmPassword.mismatch"),
        };

    return { isValid: true, message: t("validation.confirmPassword.valid") };
};

export const validateIban = (iban: string) => {
    if (!iban)
        return { isValid: false, message: t("validation.iban.required") };

    if (iban.length < 15)
        return {
            isValid: false,
            message: t("validation.iban.minLength"),
        };

    if (iban.length > 35)
        return {
            isValid: false,
            message: t("validation.iban.maxLength"),
        };

    if (!ibanRegex.test(iban))
        return {
            isValid: false,
            message: t("validation.iban.specialChar"),
        };

    return { isValid: true, message: t("validation.iban.valid") };
};

export const validateBic = (bic: string, iban: string) => {
    if(iban.slice(0,2) === 'ES' && !bic)
        return { isValid: true, message: t("validation.bic.valid") };

    if (!bic)
        return { isValid: false, message: t("validation.bic.required") };

    if (bic.length != 8 && bic.length != 11)
        return {
            isValid: false,
            message: t("validation.bic.length"),
        };

    if (!bicRegex.test(bic))
        return {
            isValid: false,
            message: t("validation.bic.specialChar.general"),
        };

    return { isValid: true, message: t("validation.iban.valid") };
};

export const validateSepaMandateCheck = (value: boolean) => {
    if (!value)
        return {
            isValid: false,
            message: t("validation.sepaMandate.required"),
        };

    return { isValid: true, message: t("validation.sepaMandate.valid") };
};
