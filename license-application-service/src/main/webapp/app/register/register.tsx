import React, { useContext, useEffect, useState } from "react";
import { validateResults } from "app/common/utils";
import {
  validateConfirmPassword,
  validateEmail,
  validateName,
  validatePassword,
} from "../common/validationRules";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import { FormHeader } from "app/common/headingTitle";
import "./register.css";
import { OrDivider } from "app/common/orDivider";
import { useRegisterUser } from "../services/authentication/authentication";
import { useNavigate } from "react-router";
import { AuthContext } from "app/common/AuthContext";

export default function Register() {
  const auth = useContext(AuthContext);
  const navigate = useNavigate();
  const { t } = useTranslation();
  useDocumentTitle(t("register.title"));

  useEffect(() => {
    if (auth?.accessToken) {
      navigate("/");
    }
  }, [navigate, auth?.accessToken]);

  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirm_password: "",
  });

  const [errors, setErrors] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirm_password: "",
    register: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [id]: value,
    }));
    setErrors((prev) => ({
      ...prev,
      [id]: "",
    }));
  };

  const registerUser = useRegisterUser({
    mutation: {
      onSuccess: (data) => {
        console.log("Registered successfully:", data.data);
        alert("Account created successfully!");
      },
      onError: (error) => {
        console.error("Registration error:", error);
        alert("Registration failed");
      },
    },
  });

  const handleSubmit = async () => {
    const fristNameValidateResult: validateResults = validateName(
      form.firstName
    );
    const lastNameValidateResult: validateResults = validateName(form.lastName);
    const emailValidateResult: validateResults = validateEmail(form.email);
    const passwordValidateResult: validateResults = validatePassword(
      form.password
    );
    const confirmPasswordValidateResult: validateResults =
      validateConfirmPassword(form.password, form.confirm_password);
    let newErrors = {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      confirm_password: "",
      register: "",
    };

    if (!fristNameValidateResult.isValid) {
      newErrors.firstName = fristNameValidateResult.message;
    }
    if (!lastNameValidateResult.isValid) {
      newErrors.lastName = lastNameValidateResult.message;
    }
    if (!emailValidateResult.isValid) {
      newErrors.email = emailValidateResult.message;
    }
    if (!passwordValidateResult.isValid) {
      newErrors.password = passwordValidateResult.message;
    }
    if (!confirmPasswordValidateResult.isValid) {
      newErrors.confirm_password = confirmPasswordValidateResult.message;
    }

    if (
      newErrors.firstName ||
      newErrors.lastName ||
      newErrors.email ||
      newErrors.password ||
      newErrors.confirm_password
    ) {
      setErrors(newErrors);
      console.log(newErrors);
      return false;
    }
    setErrors({
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      confirm_password: "",
      register: "",
    });

    // API call for register;
    try {
      const response = await registerUser.mutateAsync({
        data: {
          firstname: form.firstName,
          lastname: form.lastName,
          email: form.email,
          password: form.password,
        },
      });

      switch (response.status) {
        case 201:
          navigate("/login");
          break;

        default:
          alert(t("register.registerUserAlerts.unexpectedErr"));
      }
      return true;
    } catch (error: any) {
      const status = error?.response?.status;

      switch (status) {
        case 400:
          alert(t("register.registerUserAlerts.invalidReq"));
          break;

        case 401:
          alert(t("register.registerUserAlerts.unAuthReq"));
          break;

        case 409:
          alert(t("register.registerUserAlerts.userExists"));
          break;

        default:
          alert(t("register.registerUserAlerts.serverError"));
          break;
      }
      return false;
    }
  };

  return (
    <div className="container mx-auto px-4 md:px-6">
      <div className="relative min-h-[calc(100vh-4rem)] bg-white flex items-center justify-center">
        <div className="font-inter min-w-96">
          <FormHeader
            heading={t("register.index.headline")}
            subHeading={t("register.index.subheadline")}
          />
          {/* Register Form */}
          <div>
            {/* First Name Field */}
            <div className="relative mb-4 mt-8">
              <input
                type="text"
                id="firstName"
                value={form.firstName}
                placeholder=""
                onChange={handleChange}
                className="peer border border-mallorca-purple rounded-xl h-12 w-full px-3 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
              />
              <label
                htmlFor="name"
                className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                  form.firstName
                    ? "-top-2 text-xs text-mallorca-purple"
                    : "top-3.5 text-base text-mallorca-purple/50"
                } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
              >
                {t("register.index.firstNameLabel")}
              </label>
              {errors.firstName && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.firstName}
                </div>
              )}
            </div>

            {/* Last Name Field */}
            <div className="relative mb-4">
              <input
                type="text"
                id="lastName"
                value={form.lastName}
                placeholder=""
                onChange={handleChange}
                className="peer border border-mallorca-purple rounded-xl h-12 w-full px-3 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
              />
              <label
                htmlFor="name"
                className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                  form.lastName
                    ? "-top-2 text-xs text-mallorca-purple"
                    : "top-3.5 text-base text-mallorca-purple/50"
                } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
              >
                {t("register.index.lastNameLabel")}
              </label>
              {errors.lastName && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.lastName}
                </div>
              )}
            </div>

            {/* Email Field */}
            <div className="relative my-4">
              <input
                type="email"
                id="email"
                value={form.email}
                placeholder=""
                onChange={handleChange}
                className="peer border border-mallorca-purple rounded-xl h-12 w-full px-3 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
              />
              <label
                htmlFor="email"
                className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                  form.email
                    ? "-top-2 text-xs text-mallorca-purple"
                    : "top-3.5 text-base text-mallorca-purple/50"
                } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
              >
                {t("register.index.emailLabel")}
              </label>
              {errors.email && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.email}
                </div>
              )}
            </div>

            {/* Password Field */}
            <div className="relative my-4">
              <input
                type={showPassword ? "text" : "password"}
                id="password"
                value={form.password}
                placeholder=" "
                onChange={handleChange}
                className="peer border border-mallorca-purple rounded-xl h-12 w-full px-3 pr-10 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
              />
              <label
                htmlFor="password"
                className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                  form.password
                    ? "-top-2 text-xs text-mallorca-purple"
                    : "top-3.5 text-base text-mallorca-purple/50"
                } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
              >
                {t("register.index.passwordLabel")}
              </label>

              {/* Eye Icon */}
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-3.5 text-mallorca-purple/60 hover:text-mallorca-purple focus:outline-none"
              >
                {showPassword ? (
                  // Eye open icon (showing password)
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    strokeWidth={1.5}
                    stroke="currentColor"
                    className="w-5 h-5"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.26 4.5 12 4.5c4.74 0 8.577 3.01 9.964 7.183.07.207.07.431 0 .639C20.577 16.49 16.74 19.5 12 19.5c-4.74 0-8.577-3.01-9.964-7.178z"
                    />
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                  </svg>
                ) : (
                  // Eye off icon (hidden password)
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    strokeWidth={1.5}
                    stroke="currentColor"
                    className="w-5 h-5"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.86-.395M6.228 6.228A10.451 10.451 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.893 7.893L21 21m-3.229-3.229l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.243 4.243L9.88 9.88"
                    />
                  </svg>
                )}
              </button>

              {errors.password && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.password}
                </div>
              )}
            </div>

            {/* Confirm Password Field */}
            <div className="relative mb-8">
              <input
                type={showConfirmPassword ? "text" : "password"}
                id="confirm_password"
                value={form.confirm_password}
                placeholder=" "
                onChange={handleChange}
                className="peer border border-mallorca-purple rounded-xl h-12 w-full px-3 pr-10 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
              />
              <label
                htmlFor="confirm_password"
                className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                  form.confirm_password
                    ? "-top-2 text-xs text-mallorca-purple"
                    : "top-3.5 text-base text-mallorca-purple/50"
                } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
              >
                {t("register.index.confirmPasswordLabel")}
              </label>

              {/* Eye Icon */}
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-3.5 text-mallorca-purple/60 hover:text-mallorca-purple focus:outline-none"
              >
                {showConfirmPassword ? (
                  // Eye open icon (showing password)
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    strokeWidth={1.5}
                    stroke="currentColor"
                    className="w-5 h-5"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.26 4.5 12 4.5c4.74 0 8.577 3.01 9.964 7.183.07.207.07.431 0 .639C20.577 16.49 16.74 19.5 12 19.5c-4.74 0-8.577-3.01-9.964-7.178z"
                    />
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                  </svg>
                ) : (
                  // Eye off icon (hidden password)
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    strokeWidth={1.5}
                    stroke="currentColor"
                    className="w-5 h-5"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.86-.395M6.228 6.228A10.451 10.451 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.893 7.893L21 21m-3.229-3.229l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.243 4.243L9.88 9.88"
                    />
                  </svg>
                )}
              </button>

              {errors.confirm_password && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.confirm_password}
                </div>
              )}
            </div>
          </div>

          <div className="my-4">
            {errors.register && (
              <div className="text-red-500 mt-2 pl-4">{errors.register}</div>
            )}
            <button
              // type="submit"
              onClick={handleSubmit}
              className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg"
            >
              {t("register.index.registerButton")}
            </button>
          </div>

          <OrDivider />

          {/* Login Redirect */}
          <div className="text-center mt-5">
            <span className="text-mallorca-purple/50 pr-2">
              {t("register.index.alreadyHaveAccountText")}
            </span>
            <a
              className="text-mallorca-purple font-medium underline underline-offset-3"
              href="/login"
            >
              {t("register.index.signInLinkLabel")}
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
