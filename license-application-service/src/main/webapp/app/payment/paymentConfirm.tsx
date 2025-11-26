import React from "react";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import {FormHeader} from "app/common/headingTitle";
//import { createPayment } from "app/services/payments/payments"
import "./paymentConfirm.css";

export default function PaymentConfirm() {
  //const applicationId = 12345; // TODO: get the actual application ID from context or props
  const amount = 9999.99; // TODO: get the actual amount to be paid
  const {t} = useTranslation();
  useDocumentTitle(t("payment.title"));

  const formatAmount = (amount: number, locale: string, currency: string): string => {
    return new Intl.NumberFormat(locale, {
      style: "currency",
      currency: currency,
      currencyDisplay: "symbol",
      minimumFractionDigits: 2,
    }).format(amount);
  }

  // IBAN format: max 34 characters in groups of 4 separated by spaces (no manual input of spaces)
  /*const formatIban = (raw: string): string => {
    return raw
    .replace(/\s+/g, "")
    .toUpperCase()
    .replace(/(.{4})/g, "$1 ")
    .trim();
  };
  // BIC format: max 11 characters no manual input of spaces
  const formatBic = (raw: string): string => {
    return raw
    .replace(/\s+/g, "")
    .toUpperCase()
  };*/

  const handleSubmit = () => {
    return true;
  }

  return (
      <div className="container mx-auto px-4 md:px-6">
        <div
            className="relative min-h-[calc(100vh-4rem)] bg-white flex items-center justify-center">
          <div className="font-inter min-w-96">
            <FormHeader
                heading={t("payment.index.headline")}
                subHeading={t("payment.index.subheadline")}
            />

            <label>
            <span className="text-mallorca-purple text-lg">
                {t("payment.index.amountLabel", {amount: formatAmount(amount, t("locale"), "EUR")})}
            </span>
            </label>

            <button
                // type="submit"
                onClick={handleSubmit}
                className={"mt-4bg-mallorca-purple text-white px-10 py-2 rounded-md w-96 font-medium text-lg hover:bg-mallorca-purple/90"}
            >
              {t("payment.index.payButtonLabel")}
            </button>
          </div>
        </div>
      </div>
  );
}
