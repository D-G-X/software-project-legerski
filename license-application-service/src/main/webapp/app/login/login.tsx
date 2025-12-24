import React, { useContext, useEffect, useState } from "react";
import { validateResults } from "app/common/utils";
import { isValidEmail } from "../common/validationRules";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import { FormHeader } from "app/common/headingTitle";
import { OrDivider } from "app/common/orDivider";
import { useLoginUser } from "app/services/authentication/authentication";
import { AuthContext } from "app/common/AuthContext";
import { useNavigate } from "react-router";
import { Link } from "react-router";

export default function Login() {
  const auth = useContext(AuthContext);
  const navigate = useNavigate();
  const { t } = useTranslation();
  useDocumentTitle(t("login.title"));

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [errors, setErrors] = useState({
    email: "",
    password: "",
    login: "",
  });

  useEffect(() => {
    if (auth?.accessToken) {
      navigate("/");
    }
  }, [auth?.accessToken, navigate]);

  const [showPassword, setShowPassword] = useState(false);

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

  const loginUser = useLoginUser({
    mutation: {
      onError: (error) => {
        console.error("Login failed:", error);
      },
    },
  });

  const handleSubmit = async () => {
    const emailValidateResult: validateResults = isValidEmail(form.email);

    // redundant validation from register
    // const passwordValidateResult: validateResults = isValidPassword(
    //   form.password
    // );

    let newErrors = { email: "", password: "", login: "" };

    if (!emailValidateResult.isValid) {
      newErrors.email = emailValidateResult.message;
    }

    // redundant validation from register
    // if (!passwordValidateResult.isValid) {
    //   newErrors.password = passwordValidateResult.message;
    // }

    if (newErrors.email || newErrors.password) {
      setErrors(newErrors);
      return false;
    }
    setErrors({ email: "", password: "", login: "" });

    try {
      const response = await loginUser.mutateAsync({
        data: {
          email: form.email,
          password: form.password,
        },
      });

      // Check if login was successful
      if (response.status === 200) {
        const {
          access_token,
          refresh_token,
          expires_in,
          refresh_expires_in,
          token_type,
          is_admin,
        } = response.data;

        localStorage.setItem("accessToken", access_token);
        localStorage.setItem("refreshToken", refresh_token);
        localStorage.setItem("accessTokenExpiry", expires_in.toString());
        localStorage.setItem(
          "refreshTokenExpiry",
          refresh_expires_in ? refresh_expires_in?.toString() : ""
        );
        localStorage.setItem("tokenType", token_type);
        localStorage.setItem("role", is_admin ? "admin" : "user");

        auth?.setAccessToken(access_token);
        auth?.setRefreshToken(refresh_token);
        auth?.setAccessTokenExpiry(expires_in);
        auth?.setRefreshTokenExpiry(refresh_expires_in);
        auth?.setTokenType(refresh_token);
        auth?.setRole(is_admin ? "admin" : "user");

        navigate("/"); // redirect to dashboard
      } else {
        alert(t("login.loginUserAlerts.unexpectedResp"));
      }
    } catch (error: any) {
      const status = error?.response?.status;

      switch (status) {
        case 401:
          alert(t("login.loginUserAlerts.invalidEmail"));
          break;
        case 400:
          alert(t("login.loginUserAlerts.badRequest"));
          break;
        default:
          alert(t("login.loginUserAlerts.serverError"));
      }
    }
    return;
  };

  return (
    <div className="container mx-auto px-4 md:px-6">
      <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
        <div className="font-inter min-w-96">
          <FormHeader
            heading={t("login.index.headline")}
            subHeading={t("login.index.subheadline")}
          />
          {/* Login Form */}
          <div>
            {/* Email Field */}
            <div className="relative my-6">
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
                {t("login.index.emailLabel")}
              </label>
              {errors.email && (
                <div className="text-red-500 mt-2 pl-4">{errors.email}</div>
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
                {t("login.index.passwordLabel")}
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
                <div className="text-red-500 mt-2 pl-4">{errors.password}</div>
              )}
            </div>
          </div>
          <div className="flex justify-between my-6 px-1">
            <div className="text-mallorca-purple">
              <label className="flex items-center cursor-pointer">
                <input
                  id="keepMeLoggedIn"
                  type="checkbox"
                  className="accent-mallorca-purple w-5 h-5 rounded-sm border-2 border-mallorca-purple checked:bg-mallorca-purple mr-3"
                />
                <span>{t("login.index.keepMeLoggedIn")}</span>
              </label>
            </div>
            <div>
              <Link to="/forgot-password" className="text-mallorca-purple/50">
                {t("login.index.forgotPassword")}
              </Link>
            </div>
          </div>
          <div className="my-4">
            {errors.login && (
              <div className="text-red-500 mt-2 pl-4">{errors.login}</div>
            )}
            <button
              // type="submit"
              onClick={handleSubmit}
              className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg"
            >
              {t("login.index.signInButton")}
            </button>
          </div>
          <OrDivider />

          {/* Register Redirect */}
          <div className="text-center mt-5">
            <span className="text-mallorca-purple/50 pr-2">
              {t("login.index.createAccountText")}
            </span>
            <a
              className="text-mallorca-purple font-medium underline underline-offset-3"
              href="/register"
            >
              {t("login.index.createAccountLinkLabel")}
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
