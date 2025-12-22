import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import { FormHeader } from "app/common/headingTitle";
import "./paymentConfirm.css";
import { CircleCheck, CircleX } from "lucide-react";
import { useLocation, useNavigate } from "react-router";
import {
  formatAmount,
  formatBic,
  formatDateLong,
  formatIban,
} from "../../common/format";

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
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { state } = useLocation();

  useEffect(() => {
    if (!state) navigate("/error", { replace: true });
  }, [state, navigate]);

  if (!state) return null;

  const data: Props = state;
  const success = data.payment_status === "UNPAID";

  useDocumentTitle(t("paymentConfirm.title"));

  return (
    <div className="container mx-auto px-4 md:px-6">
      <div className="relative min-h-[calc(100vh-8rem)] bg-white flex flex-col items-center justify-center">
        <div className="flex items-center justify-center">
          {success ? (
            <CircleCheck className="text-green-500" size={72} />
          ) : (
            <CircleX className="text-red-500" size={72} />
          )}
        </div>

        <div className="font-inter mt-8 min-w-96">
          <FormHeader
            heading={
              success
                ? t("paymentConfirm.index.headline.success")
                : t("paymentConfirm.index.headline.failure")
            }
            subHeading={
              success
                ? t("paymentConfirm.index.subHeadline.success")
                : t("paymentConfirm.index.subHeadline.failure")
            }
          />
          {/* Response fields */}
          <div className="bg-gray-50 rounded-lg p-6 mt-8 shadow">
            <div className="flex justify-between text-xl font-semibold">
              <span>{t("paymentConfirm.index.amountLabel") + ": "}</span>
              <div className="flex flex-col items-end leading-tight">
                <span>{formatAmount(data.amount, t)}</span>
                <span className="text-xs font-light italic">
                  {t("paymentConfirm.index.taxLabel")}
                </span>
              </div>
            </div>

            <hr className="my-4 border-gray-300" />

            <div className="space-y-2.5 text-gray-700">
              <div className="flex justify-between">
                <span className="font-semibold">
                  {t("paymentConfirm.index.nameLabel") + ": "}
                </span>
                <span>{data.name}</span>
              </div>

              <div className="flex justify-between">
                <span className="font-semibold">
                  {t("paymentConfirm.index.ibanLabel") + ": "}
                </span>
                <span>{formatIban(data.iban)}</span>
              </div>

              <div className="flex justify-between">
                <span className="font-semibold">
                  {t("paymentConfirm.index.bicLabel") + ": "}
                </span>
                <span>
                  {data.bic ? (
                    formatBic(data.bic)
                  ) : (
                    <em>{t("paymentConfirm.index.bicOptional")}</em>
                  )}
                </span>
              </div>

              <div className="flex justify-between">
                <span className="font-semibold">
                  {t("paymentConfirm.index.applicationIdLabel") + ": "}
                </span>
                <span>{data.application_id}</span>
              </div>

              <div className="flex justify-between">
                <span className="font-semibold">
                  {t("paymentConfirm.index.paymentIdLabel") + ": "}
                </span>
                <span>{data.payment_id}</span>
              </div>

              <div className="flex justify-between">
                <span className="font-semibold">
                  {t("paymentConfirm.index.dateLabel") + ": "}
                </span>
                <span>{formatDateLong(Date.now().toString(), t)}</span>
              </div>
            </div>
          </div>
        </div>
        {/* Leave Button */}
        <div className="mt-8">
          <button
            onClick={() => navigate("/")}
            className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-96 font-medium text-lg hover:bg-mallorca-purple/90"
          >
            {success
              ? t("paymentConfirm.index.leaveButtonLabel.success")
              : t("paymentConfirm.index.leaveButtonLabel.failure")}
          </button>
        </div>
      </div>
    </div>
  );
}
