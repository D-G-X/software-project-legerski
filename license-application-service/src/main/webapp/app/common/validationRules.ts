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
        "\"(?:[\\x21-\\x7E]|\\\\[\\x21-\\x7E])+\"" +
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
