import React, {useContext, useEffect, useState} from "react";
import {validateResults} from "app/common/interfaces";
import {isValidEmail} from "../common/validationRules";
import {useTranslation} from "react-i18next";
import {useDocumentTitle} from "../common/utils";
import {FormHeader} from "app/common/headingTitle";
import {OrDivider} from "app/common/orDivider";
import {useLoginUser} from "app/services/authentication/authentication";
import {AuthContext} from "app/common/auth/AuthContext";
import {Link, useNavigate} from "react-router";
import {useGlobalLoader} from "app/common/GlobalLoader";
import {Eye, EyeClosed} from "lucide-react";

export default function Login() {
  const auth = useContext(AuthContext);
  const navigate = useNavigate();
  const {t} = useTranslation();
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

  const loginUser = useLoginUser({
    mutation: {
      onError: (error) => {
        console.error("Login failed:", error);
      },
    },
  });

  const {show, hide} = useGlobalLoader();

  const handleSubmit = async () => {
    const emailValidateResult: validateResults = isValidEmail(form.email);

    let newErrors = {email: "", password: "", login: ""};

    if (!emailValidateResult.isValid) {
      newErrors.email = emailValidateResult.message;
    }

    if (newErrors.email || newErrors.password) {
      setErrors(newErrors);
      return false;
    }
    setErrors({email: "", password: "", login: ""});

    try {
      show();

      const response = await loginUser.mutateAsync({
        data: {
          email: form.email,
          password: form.password,
        },
      });

      switch (response.status) {
        case 201:
        case 200: {
          const {
            access_token,
            refresh_token,
            expires_in,
            refresh_expires_in,
            token_type,
            is_admin,
          } = response.data;

          // Persist tokens
          localStorage.setItem("accessToken", access_token);
          localStorage.setItem("refreshToken", refresh_token);
          localStorage.setItem("accessTokenExpiry", expires_in.toString());
          localStorage.setItem(
              "refreshTokenExpiry",
              refresh_expires_in ? refresh_expires_in.toString() : ""
          );
          localStorage.setItem("tokenType", token_type);
          localStorage.setItem("role", is_admin ? "admin" : "user");

          // Update auth context
          auth?.setAccessToken(access_token);
          auth?.setRefreshToken(refresh_token);
          auth?.setAccessTokenExpiry(expires_in);
          auth?.setRefreshTokenExpiry(refresh_expires_in);
          auth?.setTokenType(token_type);
          auth?.setRole(is_admin ? "admin" : "user");

          navigate("/"); // redirect to dashboard
          break;
        }

        default:
          alert(t("login.loginUserAlerts.unexpectedResp"));
          break;
      }
    } catch (error: any) {
      const status = error?.response?.status;

      switch (status) {
        case 400:
          alert(t("login.loginUserAlerts.badRequest"));
          break;

        case 401:
          alert(t("login.loginUserAlerts.invalidEmail")); // invalid credentials
          break;

        case 403:
          alert(t("login.loginUserAlerts.forbidden"));
          break;

        case 500:
          alert(t("login.loginUserAlerts.serverError"));
          break;

        default:
          alert(t("login.loginUserAlerts.serverError"));
          break;
      }
    } finally {
      hide();
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
                      < Eye />
                  ) : (
                      < EyeClosed />
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
                  id="loginButton"
                  onClick={handleSubmit}
                  className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg"
              >
                {t("login.index.signInButton")}
              </button>
            </div>
            <OrDivider/>

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
