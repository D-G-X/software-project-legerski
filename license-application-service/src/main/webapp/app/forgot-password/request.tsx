import {FormHeader} from "app/common/headingTitle";
import {validateResults} from "app/common/interfaces";
import {isValidEmail} from "app/common/validationRules";
import React, {useState} from "react";
import {useGlobalLoader} from "app/common/GlobalLoader";
import {useTranslation} from "react-i18next";
import {RequestSent} from "./confirmation";
import {requestPasswordReset} from "app/services/authentication/authentication";

export default function ForgotPasswordRequest() {
  const {t} = useTranslation();
  const [email, setEmail] = useState("");
  const [emailError, setEmailError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [showConfirmation, setShowConfirmation] = useState(false);
  const {show, hide, visible} = useGlobalLoader();

  const handleSubmit = async () => {
    const emailValidateResult: validateResults = isValidEmail(email);

    if (!emailValidateResult.isValid) {
      setEmailError(emailValidateResult.message);
      return;
    }

    setEmailError("");
    setSubmitError("");
    show();

    try {
      await requestPasswordReset({email});
      setShowConfirmation(true);
    } catch (error: any) {
      const status = error?.response?.status;
      if (status === 404) {
        setSubmitError(t("forgotPassword.request.emailNotRegistered"));
      } else {
        setSubmitError(t("forgotPassword.request.genericError"));
      }
    } finally {
      hide();
    }
  };

  return (
      <div className="container mx-auto px-4 md:px-6">
        <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
          {!showConfirmation ? (
              <>
                <div className="font-inter min-w-96">
                  <FormHeader
                      heading={t("forgotPassword.request.headline")}
                      subHeading={t("forgotPassword.request.subheadline")}
                  />

                  {/* Forgot Password Form */}
                  <div>
                    {/* Email Field */}
                    <div className="relative my-6">
                      <input
                          type="email"
                          id="email"
                          value={email}
                          placeholder=""
                          onChange={(event) => {
                            setEmail(event.target.value);
                          }}
                          className="peer border border-mallorca-purple rounded-xl h-12 w-full px-3 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
                      />
                      <label
                          htmlFor="email"
                          className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                              email
                                  ? "-top-2 text-xs text-mallorca-purple"
                                  : "top-3.5 text-base text-mallorca-purple/50"
                          } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
                      >
                        {t("login.index.emailLabel")}
                      </label>
                      {emailError && (
                          <div className="text-red-500 mt-2 pl-4">{emailError}</div>
                      )}
                    </div>
                    <div className="my-4">
                      {submitError && (
                          <div className="text-red-500 mt-2 pl-4">{submitError}</div>
                      )}
                      <button
                          type="submit"
                          id="resetPasswordBtn"
                          onClick={handleSubmit}
                          disabled={visible}
                          className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg"
                      >
                        {t("forgotPassword.request.resetPasswordBtn")}
                      </button>
                    </div>
                  </div>
                </div>
              </>
          ) : (
              <>
                <RequestSent/>
              </>
          )}
        </div>
      </div>
  );
}
