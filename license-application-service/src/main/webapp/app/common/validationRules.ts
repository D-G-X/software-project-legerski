import { t } from "i18next";

// Email validation
export const validateEmail = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.email.required") };

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  return emailRegex.test(value)
    ? { isValid: true, message: t("validation.email.valid") }
    : { isValid: false, message: t("validation.email.invalid") };
};

// Password validation
export const validatePassword = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.password.required") };

  if (value.length < 6)
    return {
      isValid: false,
      message: t("validation.password.minLength"),
    };

  if (!/[A-Z]/.test(value))
    return {
      isValid: false,
      message: t("validation.password.uppercase"),
    };

  if (!/[0-9]/.test(value))
    return {
      isValid: false,
      message: t("validation.password.number"),
    };

  if (!/[!@#$%^&*]/.test(value))
    return {
      isValid: false,
      message: t("validation.password.specialChar"),
    };

  return { isValid: true, message: t("validation.password.valid") };
};
