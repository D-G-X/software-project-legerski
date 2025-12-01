import React, {useEffect} from "react";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import {FormHeader} from "app/common/headingTitle";
import "./paymentConfirm.css";
import {CircleCheck, CircleX} from "lucide-react";
import {useLocation, useNavigate} from "react-router";

type Props = {
  payment_id: number;
  application_id: number;
  amount: number;
  name: string;
  iban: string;
  bic: string;
  payment_date: string;
  payment_status: string;
};

export default function PaymentConfirm() {
  const {t} = useTranslation();
  const navigate = useNavigate();
  const {state} = useLocation();
  const state_data: Props = state;

  useDocumentTitle(t("paymentConfirm.title"));

  useEffect(() => {
    if (!state || Object.keys(state).length === 0) {
      navigate("/error", {replace: true});
    }
  }, [state, navigate]);

  if (!state) return null;

  const success = state_data.payment_status === "UNPAID";

  const formatAmount = (amount: number): string =>
      new Intl.NumberFormat(t("locale"), {
        style: "currency",
        currency: "EUR",
        minimumFractionDigits: 2,
      }).format(amount);

  const formatIban = (raw: string): string => {
    if (!raw) return "";
    const visibleLength = 2;
    const clean = raw.replace(/\s+/g, "").toUpperCase();
    const visible = clean.slice(-visibleLength);
    const masked = "•".repeat(clean.length - visibleLength);
    return (masked + visible).replace(/(.{4})/g, "$1 ").trim();
  };

  const formatBic = (raw: string): string =>
      raw ? raw.replace(/\s+/g, "").toUpperCase() : "";

  const formattedDate = new Date(state_data.payment_date).toLocaleString(t("locale"), {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });

  const handleSubmit = () => navigate("/");

  return (
      <div className="container mx-auto px-4 md:px-6">

        <div
            className="relative min-h-[calc(100vh-8rem)] bg-white flex flex-col items-center justify-center">
          <div className="flex top-8 items-center justify-center">
            {success ?
                <CircleCheck className="text-green-500" size={72}/>
                :
                <CircleX className="text-red-500" size={72}/>
            }
          </div>
          <div className="font-inter mt-8 min-w-96">
            <FormHeader
                heading={
                  success ?
                      t("paymentConfirm.index.headline.success")
                      :
                      t("paymentConfirm.index.headline.failure")
                }
                subHeading={
                  success ?
                      t("paymentConfirm.index.subHeadline.success")
                      :
                      t("paymentConfirm.index.subHeadline.failure")
                }
            />
            {/* 3. Response fields */}
            <div className="bg-gray-50 rounded-lg p-6 mt-8 shadow">
              <div className="flex justify-between text-xl font-semibold">
                <span className="">{t("paymentConfirm.index.amountLabel") + ": "}</span>
                <div className="flex flex-col items-end leading-tight">
                  <span>{formatAmount(state_data.amount)}</span>
                  <span
                      className="text-xs font-light italic">{t("paymentConfirm.index.taxLabel")}</span>
                </div>
              </div>

              <hr className="my-4 border-gray-300"/>

              <div className="space-y-2.5 text-gray-700">

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.nameLabel") + ": "}</span>
                  <span>{state_data.name}</span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.ibanLabel") + ": "}</span>
                  <span>{formatIban(state_data.iban)}</span>
                </div>

                <div className="flex justify-between">
                  <span className="font-semibold">{t("paymentConfirm.index.bicLabel") + ": "}</span>
                  <span>
                    {state_data.bic
                        ? formatBic(state_data.bic)
                        : <em>{t("paymentConfirm.index.bicOptional")}</em>}
                  </span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.applicationIdLabel") + ": "}</span>
                  <span>{state_data.application_id}</span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.paymentIdLabel") + ": "}</span>
                  <span>{state_data.payment_id}</span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.dateLabel") + ": "}</span>
                  <span>{formattedDate}</span>
                </div>

              </div>
            </div>
          </div>
          { /* Leave Button */}
          <div className="mt-8">
            <button
                // type="submit"
                onClick={handleSubmit}
                className={`bg-mallorca-purple text-white px-10 py-2 rounded-md w-96 font-medium text-lg hover:bg-mallorca-purple/90`}
            >{
              (success ?
                      t("paymentConfirm.index.leaveButtonLabel.success")
                      :
                      t("paymentConfirm.index.leaveButtonLabel.failure")
              )
            }
            </button>
          </div>
        </div>
      </div>
  )
      ;
}
