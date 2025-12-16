import React, { useContext, useEffect, useState } from "react";
import {
  validateBic,
  validateIban,
  validateName,
  validateSepaMandateCheck,
} from "../common/validationRules";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import { FormHeader } from "app/common/headingTitle";
import {
  useCreatePayment,
  useGetApplicationFee,
} from "app/services/payments/payments";
import ModalDialog from "../common/modal-dialog";
import { useNavigate, useParams } from "react-router";
import "./paymentForm.css";
import { AnimatedDots } from "../common/AnimatedDots";
import { useGetApplication } from "app/services/applications/applications";
import {
  formatAmount,
  formatBic,
  formatDate,
  formatIban,
} from "../common/format";
import { ApplicationPaymentCreate } from "../../types";
import { AuthContext } from "app/common/AuthContext";
import axios from "axios";

export default function PaymentConfirm() {
  const { t } = useTranslation();
  const auth = useContext(AuthContext);
  const navigate = useNavigate();
  const mutation = useCreatePayment({
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
  });
  const { id } = useParams<{ id: string }>();
  const applicationId = parseInt(id!, 10);
  const [ibanFocused, setIbanFocused] = useState(false);
  const [amount, setAmount] = useState<number | null | undefined>(null);
  useDocumentTitle(t("payment.title"));

  // Page initializer useEffect will work only once after the page loads
  useEffect(() => {
    if (!id) {
      alert("Invalid application data received. Redirecting to dashboard.");
      navigate("/");
      return;
    }
  }, []);

  const {
    data: applicationDetails,
    isFetching: isFetchingAppData,
    error: appError,
  } = useGetApplication(applicationId, {
    query: {
      select: (response) => response.data,
    },
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
  });

  const {
    refetch: refetchAppFee,
    isFetching: isFetchingAppFee,
    error: appFeeError,
  } = useGetApplicationFee(applicationId, {
    query: {
      enabled: false,
    },
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
  });

  useEffect(() => {
    if (appError && axios.isAxiosError(appError)) {
      if (appError.response?.status === 401) {
        alert("Session expired. Redirecting to login.");
        auth?.signOut();
        navigate("/login");
        console.error(
          "API error:",
          appError.response?.status,
          appError.message
        );
      }
    }

    if (appFeeError && axios.isAxiosError(appFeeError)) {
      if (appFeeError.response?.status === 401) {
        alert("Session expired. Redirecting to login.");
        auth?.signOut();
        navigate("/login");
      } else {
        console.error(
          "API error:",
          appFeeError.response?.status,
          appFeeError.message
        );
      }
    }
  }, [appError, appFeeError]);

  useEffect(() => {
    const fetchFee = async () => {
      if (isFetchingAppData) {
        return;
      }
      if (!applicationDetails?.id) {
        navigate("/");
        return;
      }

      const result = await refetchAppFee();
      if (isFetchingAppFee && !result) {
        alert("Payment Gateway is not responding, please try again later!");
        navigate("/");
        return;
      }
      setAmount(result?.data?.data.fee_amount);
    };

    fetchFee();
  }, [applicationDetails]);

  const [showSepaDialog, setShowSepaDialog] = useState(false);
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    name: "",
    iban: "",
    bic: "",
    sepaMandateChecked: false,
  });

  const [errors, setErrors] = useState({
    name: "",
    iban: "",
    bic: "",
    sepaMandateCheck: "",
    pay: "",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;

    console.log(form);

    if (id === "iban") {
      const rawIban = value.replace(/\s+/g, "").toUpperCase();

      setForm((prev) => ({
        ...prev,
        iban: rawIban,
      }));

      setErrors((prev) => ({ ...prev, iban: "" }));
      return;
    }

    const formatted = id === "bic" ? formatBic(value) : value;

    setForm((prev) => ({ ...prev, [id]: formatted }));
    setErrors((prev) => ({ ...prev, [id]: "" }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // normalize inputs
    const normalizedName = form.name.trim();
    const normalizedIban = form.iban.replace(/\s+/g, "");
    const normalizedBic = form.bic.replace(/\s+/g, "");

    console.log(normalizedBic, normalizedName, normalizedIban, form);

    // validations
    const nameVal = validateName(normalizedName);
    const ibanVal = validateIban(normalizedIban);
    const bicVal = validateBic(normalizedBic, normalizedIban);
    const sepaVal = validateSepaMandateCheck(form.sepaMandateChecked);

    const newErrors = {
      name: nameVal.isValid ? "" : nameVal.message,
      iban: ibanVal.isValid ? "" : ibanVal.message,
      bic: bicVal.isValid ? "" : bicVal.message,
      sepaMandateCheck: sepaVal.isValid ? "" : sepaVal.message,
      pay: "",
    };

    if (Object.values(newErrors).some(Boolean)) {
      setErrors(newErrors);
      return;
    }

    setErrors({ name: "", iban: "", bic: "", sepaMandateCheck: "", pay: "" });

    // ES IBAN → clear BIC if invalid
    if (normalizedIban.startsWith("ES") && !bicVal.isValid) {
      setForm((prev) => ({ ...prev, bic: "" }));
    }

    const postData: ApplicationPaymentCreate = {
      application_id: applicationId,
      name: normalizedName,
      iban: normalizedIban,
      bic: normalizedBic,
    };

    console.log("postdata", postData);

    try {
      setLoading(true);

      const response = await mutation.mutateAsync({
        applicationId,
        data: postData,
      });

      if (response.status !== 201) {
        alert("Unexpected response from server. Redirecting to dashboard.");
        navigate("/");
        return;
      }

      // validate response consistency
      // const isEqual =
      //   response.data.application_id === applicationId &&
      //   response.data.amount === amount &&
      //   response.data.name === normalizedName &&
      //   response.data.iban === normalizedIban &&
      //   response.data.bic === normalizedBic;

      // if (!isEqual) {
      //   throw new Error("Response data mismatch");
      // }

      console.log("post isequal", {
        application_id: applicationId,
        payment_id: response.data.id,
        payment_date: response.data.payment_date ?? Date.now().toString(),
        payment_status: response.data.payment_status,
        name: normalizedName,
        iban: normalizedIban,
        bic: normalizedBic,
        amount,
      });

      navigate(`/payment/${applicationId}/done`, {
        state: {
          application_id: applicationId,
          payment_id: response.data.id,
          payment_date: response.data.payment_date ?? Date.now().toString(),
          payment_status: response.data.payment_status,
          name: normalizedName,
          iban: normalizedIban,
          bic: normalizedBic,
          amount,
        },
      });
    } catch (err) {
      setErrors((prev) => ({ ...prev, pay: "Payment failed" }));
    } finally {
      setLoading(false);
    }
  };

  const [bicFocused, setBicFocused] = useState(false);

  return (
    <div className="container mx-auto px-4 md:px-6">
      <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
        {!loading ? (
          <div className="font-inter min-w-96">
            <FormHeader
              heading={t("paymentForm.index.headline")}
              subHeading={t("paymentForm.index.subHeadline")}
            />
            <div className="relative mt-12 bg-gray-50 px-3 pt-5 pb-2 rounded-xl">
              <div className="flex justify-between items-baseline text-xl font-semibold">
                <span>{t("paymentForm.index.amountLabel") + ": "}</span>
                <div className="flex flex-col items-end">
                  <span className="text-xl font-semibold leading-none">
                    {isFetchingAppData || isFetchingAppFee ? (
                      <div className="w-32 h-6 rounded-md overflow-hidden relative">
                        <div className="absolute inset-0 bg-linear-to-r from-gray-200 via-mallorca-purple/30 to-gray-200 animate-shimmer" />
                      </div>
                    ) : (
                      formatAmount(amount!, t)
                    )}
                  </span>
                  <span className="text-xs font-light italic mt-1 leading-none">
                    {t("paymentConfirm.index.taxLabel")}
                  </span>
                </div>
              </div>
            </div>

            <div>
              <div className="relative mt-8 mb-4">
                <input
                  type="text"
                  id="name"
                  value={form.name}
                  placeholder=""
                  onChange={handleChange}
                  className="peer border border-mallorca-purple rounded-xl h-12 w-96 px-3 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
                />
                <label
                  htmlFor="name"
                  className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                    form.name
                      ? "-top-2 text-xs text-mallorca-purple"
                      : "top-3.5 text-base text-mallorca-purple/50"
                  } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
                >
                  {t("paymentForm.index.nameLabel")}
                </label>
                {errors.name && (
                  <div className="text-red-500 mt-1 pl-4 text-xs">
                    {errors.name}
                  </div>
                )}
              </div>

              <div className="relative my-4">
                <input
                  type="text"
                  id="iban"
                  value={ibanFocused ? form.iban : formatIban(form.iban)}
                  onChange={handleChange}
                  onFocus={() => setIbanFocused(true)}
                  onBlur={() => setIbanFocused(false)}
                  className="peer border border-mallorca-purple rounded-xl h-12 w-96 px-3 pt-5 pb-2 text-mallorca-purple"
                />
                <label
                  htmlFor="iban"
                  className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                    form.iban
                      ? "-top-2 text-xs text-mallorca-purple"
                      : "top-3.5 text-base text-mallorca-purple/50"
                  } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
                >
                  {t("paymentForm.index.ibanLabel")}
                </label>
                {errors.iban && (
                  <div className="text-red-500 mt-1 pl-4 text-xs">
                    {errors.iban}
                  </div>
                )}
              </div>

              <div className="relative my-4">
                <input
                  type="text"
                  id="bic"
                  value={form.bic}
                  placeholder=""
                  onChange={handleChange}
                  onFocus={() => setBicFocused(true)}
                  onBlur={() => setBicFocused(false)}
                  className="peer border border-mallorca-purple rounded-xl h-12 w-96 px-3 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
                />
                <label
                  htmlFor="bic"
                  className={`absolute left-3 text-mallorca-purple/70 text-sm transition-all duration-200 bg-white z-10 px-1 ${
                    form.bic
                      ? "-top-2 text-xs text-mallorca-purple"
                      : "top-3.5 text-base text-mallorca-purple/50"
                  } peer-focus:-top-2 peer-focus:text-xs peer-focus:text-mallorca-purple`}
                >
                  {t("paymentForm.index.bicLabel")}
                  {!form.bic &&
                    !bicFocused &&
                    t("paymentForm.index.bicLabelOptional")}
                </label>
                {errors.bic && (
                  <div className="text-red-500 my-1 pl-4 text-xs">
                    {errors.bic}
                  </div>
                )}
              </div>

              <div className="relative mt-4 pt-2">
                <div className="flex items-center gap-4">
                  <label
                    htmlFor="sepaMandateChecked"
                    className="px-3 text-mallorca-purple/70 text-lg"
                  >
                    {t("paymentForm.index.acceptSepaMandateLabel.plain")}
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        setShowSepaDialog((prev) => !prev);
                      }}
                      className="ml-1 text-mallorca-purple underline hover:text-mallorca-purple/80"
                    >
                      {t("paymentForm.index.acceptSepaMandateLabel.button")}
                    </button>
                  </label>
                </div>

                {errors.sepaMandateCheck && (
                  <div className="text-red-500 mt-1 text-xs pl-1">
                    {errors.sepaMandateCheck}
                  </div>
                )}
              </div>

              {/* SEPA Mandate Dialog */}
              <ModalDialog
                open={showSepaDialog}
                showDownloadButton={true}
                acceptButtonLabel={t(
                  "paymentForm.sepaMandateDialog.acceptButtonLabel"
                )}
                cancelButtonLabel={t(
                  "paymentForm.sepaMandateDialog.cancelButtonLabel"
                )}
                downloadButtonLabel={t(
                  "paymentForm.sepaMandateDialog.downloadButtonLabel"
                )}
                downloadFileName={t(
                  "paymentForm.sepaMandateDialog.downloadFileName",
                  { accountHolder: form.name ? " " + form.name : "" }
                )}
                title={t("paymentForm.sepaMandateDialog.title") + "\n\n"}
                text={
                  t("paymentForm.sepaMandateDialog.text.line1") +
                  "\n\n" +
                  t("paymentForm.sepaMandateDialog.text.line2") +
                  "\n" +
                  t("paymentForm.sepaMandateDialog.text.line3") +
                  "\n\n" +
                  t("paymentForm.sepaMandateDialog.text.line4") +
                  "\n" +
                  t("paymentForm.sepaMandateDialog.text.line5", {
                    accountHolder: form.name ? form.name : "",
                  }) +
                  "\n" +
                  t("paymentForm.sepaMandateDialog.text.line6", {
                    iban: form.iban ? form.iban : "",
                  }) +
                  (form.bic ? "\n" : "") +
                  (form.bic
                    ? t("paymentForm.sepaMandateDialog.text.line7", {
                        bic: form.bic,
                      })
                    : "") +
                  "\n\n" +
                  t("paymentForm.sepaMandateDialog.text.line8") +
                  "\n" +
                  t("paymentForm.sepaMandateDialog.text.line9", {
                    beneficiaryName: t("app.contact.legalName"),
                  }) +
                  "\n" +
                  t("paymentForm.sepaMandateDialog.text.line10", {
                    creditorId: "ES98ZZZ09999999999",
                  }) +
                  "\n" +
                  t("paymentForm.sepaMandateDialog.text.line11", {
                    mandateReference: "ESM-2025-00001",
                  }) +
                  "\n\n" +
                  t("paymentForm.sepaMandateDialog.text.line12") +
                  "\n\n\n" +
                  t("paymentForm.sepaMandateDialog.text.line13", {
                    date: formatDate(Date.now().toString(), t),
                  }) +
                  "\n\n"
                }
                t={t}
                onAccept={() => {
                  setShowSepaDialog(false);
                  setForm((prev) => ({ ...prev, sepaMandateChecked: true }));
                }}
                onCancel={() => setShowSepaDialog(false)}
              />
            </div>
            {/* Pay Button */}
            <div>
              {errors.pay && (
                <div className="text-red-500 my-2 pl-4">{errors.pay}</div>
              )}
              <button
                type="submit"
                onClick={handleSubmit}
                className={`mt-8 ${
                  errors.pay ? "bg-mallorca-purple/75" : "bg-mallorca-purple"
                } text-white px-10 py-2 rounded-md w-96 font-medium text-lg`}
              >
                {t("paymentForm.index.payButtonLabel")}
              </button>
            </div>
            {/* Finish Later Button */}
            <div>
              <button
                type="submit"
                onClick={() => navigate(`/`)}
                className={
                  "mt-4 bg-white text-mallorca-purple  px-10 py-2 rounded-md w-96 font-medium text-lg hover:bg-mallorca-purple/50 hover:text-white border border-mallorca-purple"
                }
              >
                {t("paymentForm.index.cancelButtonLabel")}
              </button>
              <p className="mt-1 font-inter text-center text-gray-600 text-sm">
                {t("paymentForm.index.cancelNote.label1")}
              </p>
              <p className="mt-1 font-inter text-center text-gray-600 text-sm">
                {t("paymentForm.index.cancelNote.label2")}
              </p>
            </div>
          </div>
        ) : (
          <div className="font-inter text-center">
            {/* Loading Screen */}
            <div className="w-16 h-16 border-6 border-gray-200 border-t-mallorca-purple rounded-full animate-spin mx-auto" />
            <p className="mt-6 font-light text-xl text-gray-600 relative inline-block">
              {t("paymentForm.processing")}
              <span className="absolute left-full">
                <AnimatedDots speed={400} />
              </span>
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
