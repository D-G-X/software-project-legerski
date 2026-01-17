import React, {useContext, useEffect, useState} from "react";
import {validateResults} from "app/common/interfaces";
import {isValidConfirmPassword, isValidEmail, isValidName, isValidPassword,} from "../common/validationRules";
import {useTranslation} from "react-i18next";
import {useDocumentTitle} from "../common/utils";
import {FormHeader} from "app/common/headingTitle";
import {OrDivider} from "app/common/orDivider";
import {useRegisterUser} from "../services/authentication/authentication";
import {useNavigate} from "react-router";
import {AuthContext} from "app/common/auth/AuthContext";
import {useGlobalLoader} from "app/common/GlobalLoader";
import {Eye, EyeClosed} from "lucide-react";

export default function Register() {
  const auth = useContext(AuthContext);
  const navigate = useNavigate();
  const {t} = useTranslation();
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
    confirmPassword: "",
  });

  const [errors, setErrors] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    register: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const {id, value} = e.target;
    setForm((prev) => ({
      ...prev,
      [id]: value,
    }));
    setErrors((prev) => ({
      ...prev,
      [id]: "",
    }));
  };

  const registerUser = useRegisterUser();
  const {show, hide} = useGlobalLoader();

  const handleSubmit = async () => {
    const fristNameValidateResult: validateResults = isValidName(
        form.firstName
    );
    const lastNameValidateResult: validateResults = isValidName(form.lastName);
    const emailValidateResult: validateResults = isValidEmail(form.email);
    const passwordValidateResult: validateResults = isValidPassword(
        form.password
    );
    const confirmPasswordValidateResult: validateResults =
        isValidConfirmPassword(form.password, form.confirmPassword);
    let newErrors = {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      confirmPassword: "",
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
      newErrors.confirmPassword = confirmPasswordValidateResult.message;
    }

    if (
        newErrors.firstName ||
        newErrors.lastName ||
        newErrors.email ||
        newErrors.password ||
        newErrors.confirmPassword
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
      confirmPassword: "",
      register: "",
    });

    // API call for register;
    try {
      show();
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
    } finally {
      hide();
    }
  };

  return (
      <div className="container mx-auto px-4 md:px-6">
        <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
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
                      < Eye />
                  ) : (
                      < EyeClosed />
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
                    id="confirmPassword"
                    value={form.confirmPassword}
                    placeholder=" "
                    onChange={handleChange}
                    className="peer border border-mallorca-purple rounded-xl h-12 w-full px-3 pr-10 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
                />
                <label
                    htmlFor="confirmPassword"
                    className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                        form.confirmPassword
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
                      < Eye />
                  ) : (
                      < EyeClosed />
                  )}
                </button>

                {errors.confirmPassword && (
                    <div className="text-red-500 mt-1 pl-4 text-xs">
                      {errors.confirmPassword}
                    </div>
                )}
              </div>
            </div>

            <div className="my-4">
              {errors.register && (
                  <div className="text-red-500 mt-2 pl-4">{errors.register}</div>
              )}
              <button
                  id="registerButton"
                  onClick={handleSubmit}
                  className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg"
              >
                {t("register.index.registerButton")}
              </button>
            </div>

            <OrDivider/>

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
