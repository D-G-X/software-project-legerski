import { t } from "i18next";

// Name Validation
export const validateName = (value: string) => {
  if (!value) return { isValid: false, message: t("validation.name.required") };

  const nameRegex = new RegExp(
    "^[\\p{L}\\p{M}'’-]+(?: [\\p{L}\\p{M}'’-]+)*$",
    "u"
  );

  if (!nameRegex.test(value))
    return {
      isValid: false,
      message: t("validation.name.invalid"),
    };

  if (value.length < 2)
    return {
      isValid: false,
      message: t("validation.name.minLength"),
    };

  return { isValid: true, message: t("validation.name.valid") };
};

// Email validation
export const validateEmail = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.email.required") };

  const emailRegex = new RegExp("([!#-'*+/-9=?A-Z^-~-]+(\.[!#-'*+/-9=?A-Z^-~-]+)*|\"\(\[\]!#-[^-~ \t]|(\\[\t -~]))+\")@([!#-'*+/-9=?A-Z^-~-]+(\.[!#-'*+/-9=?A-Z^-~-]+)*|\[[\t -Z^-~]*])");
  return emailRegex.test(value)
    ? { isValid: true, message: t("validation.email.valid") }
    : { isValid: false, message: t("validation.email.invalid") };
};

// Password validation: at least 8 characters long and at least one special character
export const validatePassword = (value: string) => {
  if (!value)
    return { isValid: false, message: t("validation.password.required") };

  if (value.length < 8)
    return {
      isValid: false,
      message: t("validation.password.minLength"),
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
