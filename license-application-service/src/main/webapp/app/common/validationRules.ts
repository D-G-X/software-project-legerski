// Email validation
export const validateEmail = (value: string) => {
  if (!value) return { isValid: true, message: "Email is required" };
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(value)
    ? { isValid: true, message: "Valid email address!" }
    : { isValid: false, message: "Invalid email address!" };
};

// Password validation
export const validatePassword = (value: string) => {
  if (!value) return { isValid: false, message: "Password is required" };
  if (value.length < 6)
    return {
      isValid: false,
      message: "Password must be at least 6 characters long",
    };
  if (!/[A-Z]/.test(value))
    return {
      isValid: false,
      message: "Password must include at least one uppercase letter",
    };
  if (!/[0-9]/.test(value))
    return {
      isValid: false,
      message: "Password must include at least one number",
    };
  if (!/[!@#$%^&*]/.test(value))
    return {
      isValid: false,
      message:
        "Password must include at least one special character (!@#$%^&*)",
    };
  return { isValid: true, message: "Password is valid!" };
};
