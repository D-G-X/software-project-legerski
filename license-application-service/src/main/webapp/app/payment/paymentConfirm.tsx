import React, {useState} from "react";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import {FormHeader} from "app/common/headingTitle";
import "./paymentConfirm.css";
import {CircleCheck, CircleX} from "lucide-react";


type Props = {
  data: {
    id: number;
    application_id: number;
    amount: number;
    name: string;
    iban: string;
    bic?: string;
    payment_date: string;
    payment_status: string;
  };
};


export default function PaymentConfirm({data}: Props) {
  const {t} = useTranslation();
  useDocumentTitle(t("paymentConfirm.title"));

  let email = "peter.heusch@hft-stuttgart.de";

  const [success] = useState(true); // TODO: This would be determined by actual payment status

  const formatAmount = (amount: number): string => {
    return new Intl.NumberFormat(t("locale"), {
      style: "currency",
      currency: "EUR",
      currencyDisplay: "symbol",
      minimumFractionDigits: 2,
    }).format(amount);
  }

  // IBAN format: max 34 characters in groups of 4 separated by spaces (no manual input of spaces)
  const formatIban = (raw: string): string => {
    const visibleLength = 4;
    const clean = raw.replace(/\s+/g, "").toUpperCase()
    const visible = clean.slice(-visibleLength);
    const masked = "•".repeat(clean.length - visibleLength);
    return (masked + visible)
    .replace(/(.{4})/g, "$1 ")
    .trim();
  };
  // BIC format: max 11 characters no manual input of spaces
  const formatBic = (raw: string): string => {
    const visibleLength = 4;
    const clean = raw.replace(/\s+/g, "").toUpperCase()
    const visible = clean.slice(0, visibleLength);
    const masked = "•".repeat(clean.length - visibleLength);
    return (visible + masked);
  };

  const formattedDate = new Date(data.payment_date).toLocaleString(t("locale"), {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });

  const handleSubmit = () => {
    return true;
  }

  return (
      <div className="container mx-auto px-4 md:px-6">

        <div
            className="relative min-h-[calc(100vh-4rem)] bg-white flex flex-col items-center justify-center">
          <div className="flex top-8 mb-6 items-center justify-center">
            {success ?
                <CircleCheck className="text-green-500" size={72}/>
                :
                <CircleX className="text-red-500" size={72}/>
            }
          </div>
          <div className="font-inter text-center min-w-96">
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
            <div className="bg-gray-100 rounded-lg p-6 mt-6 mb-2 shadow">
              <div className="flex justify-between text-xl font-semibold">
                <span className="">{t("paymentConfirm.index.amountLabel") + ": "}</span>
                <div className="flex flex-col items-end leading-tight">
                  <span>{formatAmount(data.amount)}</span>
                  <span
                      className="text-xs font-light italic">{t("paymentConfirm.index.taxLabel")}</span>
                </div>
              </div>

              <hr className="mt-2 mb-6 border-gray-300"/>

              <div className="space-y-2.5 text-gray-700">

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.nameLabel") + ": "}</span>
                  <span>{data.name}</span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.ibanLabel") + ": "}</span>
                  <span>{formatIban(data.iban)}</span>
                </div>

                <div className="flex justify-between">
                  <span className="font-semibold">{t("paymentConfirm.index.bicLabel") + ": "}</span>
                  <span>
                    {data.bic
                        ? formatBic(data.bic)
                        : <em>{t("paymentConfirm.index.bicOptional")}</em>}
                  </span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.applicationIdLabel") + ": "}</span>
                  <span>{data.application_id}</span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.paymentIdLabel") + ": "}</span>
                  <span>{data.id}</span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.dateLabel") + ": "}</span>
                  <span>{formattedDate}</span>
                </div>

                <div className="flex justify-between">
                  <span
                      className="font-semibold">{t("paymentConfirm.index.statusLabel.plain") + ": "}</span>
                  <span
                      className="font-bold">
                    <div className={`rounded-md px-3 py-1 text-white
                    ${success ? "bg-green-600" : "bg-red-600"}`}>
                    {
                      (success ?
                              t("paymentConfirm.index.statusLabel.success")
                              :
                              t("paymentConfirm.index.statusLabel.failure")
                      )
                    }
                    </div>
                  </span>
                </div>

              </div>
            </div>
          </div>
          {
            success &&
              <div className="my-2">
                <button
                    // type="submit"
                    onClick={handleSubmit}
                    className={`mt-0 border border-mallorca-purple text-mallorca-purple px-10 py-2 rounded-md w-96 font-medium text-lg bg-white hover:bg-gray-100`}
                >{t("paymentConfirm.index.sendReceiptLabel", {email: email})}
                </button>
              </div>
          }
          <div className="my-2">
            <button
                // type="submit"
                onClick={handleSubmit}
                className={`${success ? "mt-2" : "mt-4"}
                bg-mallorca-purple text-white px-10 py-2 rounded-md w-96 font-medium text-lg hover:bg-mallorca-purple/90`}
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
