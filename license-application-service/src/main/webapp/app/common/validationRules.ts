import { t } from "i18next";

// Name Validation
export const validateName = (value: string) => {
  if (!value) return { isValid: false, message: t("validation.name.required") };

  const nameRegex = new RegExp(
    "^[\\p{L}\\p{M}'’\\-–]+(?: [\\p{L}\\p{M}'’\\-–]+)*$",
    "u"
  );

  if (value.length < 2)
    return {
      isValid: false,
      message: t("validation.name.minLength"),
    };

  if (!nameRegex.test(value))
    return {
      isValid: false,
      message: t("validation.name.invalid"),
    };

  return { isValid: true, message: t("validation.name.valid") };
};

// Email validation
export const validateEmail = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.email.required") };

  const emailRegex = new RegExp(
    "^(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+" +
      "(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
      "|" +
      '"(?:[\\x21-\\x7E]|\\\\[\\x21-\\x7E])+"' +
      ")" +
      "@" +
      "(?:(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)\\.)+" +
      "(?:[A-Za-z]{2,})$"
  );
  return emailRegex.test(value)
    ? { isValid: true, message: t("validation.email.valid") }
    : { isValid: false, message: t("validation.email.invalid") };
};

// Password validation: min. 8 and max. 255 characters long and at least one special character
export const validatePassword = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.password.required") };

  if (value.length < 8)
    return {
      isValid: false,
      message: t("validation.password.minLength"),
    };
  // avoid cutting off passwords that are too long for database
  if (value.length > 255)
    return {
      isValid: false,
      message: t("validation.password.maxLength"),
    };

  // At least one special character (non letter or number)
  if (!/[\p{P}\p{S}]/u.test(value))
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
