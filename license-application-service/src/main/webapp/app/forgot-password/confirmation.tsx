import { FormHeader } from "app/common/headingTitle";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";

function RequestSent() {
  const { t } = useTranslation();
  return (
    <div className="font-inter min-w-96">
      <FormHeader
        heading={t("forgotPassword.confirmation.headline")}
        subHeading={t("forgotPassword.confirmation.subheadline")}
      />
      <div className="my-4">
        <Link
          to="/login"
          className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg block text-center"
        >
          {t("forgotPassword.confirmation.redirectToSignIn")}
        </Link>
      </div>
    </div>
  );
}

function ResetConfirmation() {
  const { t } = useTranslation();
  return (
    <div className="font-inter min-w-96">
      <FormHeader heading={t("forgotPassword.resetConfirmation.headline")} />
      <div className="my-4">
        <Link
          to="/login"
          className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg block text-center"
        >
          {t("forgotPassword.resetConfirmation.redirectToSignIn")}
        </Link>
      </div>
    </div>
  );
}

export { RequestSent, ResetConfirmation };
