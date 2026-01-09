import React, { useContext, useEffect, useState } from "react";
import {
  isValidBic,
  isValidIban,
  isValidName,
} from "../../common/validationRules";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import { FormHeader } from "app/common/headingTitle";
import {
  useCreatePayment,
  useGetApplicationFee,
} from "app/services/payments/payments";
import { useUpdateApplication } from "app/services/applications/applications";
import { ApplicationStatusApiEnum } from "types/applicationStatusApiEnum";
import ModalDialog from "../../common/modal-dialog";
import { useNavigate, useParams } from "react-router";
import { useGetApplication } from "app/services/applications/applications";
import {
  formatAmount,
  formatBic,
  formatDateLong,
  formatIban,
} from "../../common/format";
import { ApplicationPaymentCreate } from "../../../types";
import { AuthContext } from "app/common/AuthContext";
import { useGlobalLoader } from "app/common/GlobalLoader";
import axios from "axios";

export type StateProps = {
  amount: number | null | undefined;
  payment_id: number;
  application_id: number;
  cadastral_reference: string | undefined;
  license_type: string | undefined;
  name: string;
  iban: string;
  bic: string;
  payment_date: string;
  payment_status: string;
};

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
  const updateApplication = useUpdateApplication({
    mutation: {
      onError: (error) => {
        console.error("Update application error:", error);
      },
    },
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
    data: applicationFeeData,
    isFetching: isFetchingAppFee,
    error: appFeeError,
  } = useGetApplicationFee(applicationId, {
    query: {
      enabled: !!applicationDetails?.id && !!auth?.accessToken,
      select: (response) => response,
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

  const { show, hide } = useGlobalLoader();

  useEffect(() => {
    if (isFetchingAppData || isFetchingAppFee) show();
    else hide();
  }, [isFetchingAppData, isFetchingAppFee, show, hide]);

  useEffect(() => {
    if (isFetchingAppData) return;
    if (
      !applicationDetails?.id ||
      applicationDetails.application_status !== "DOCUMENTS_SUBMITTED"
    ) {
      navigate("/");
      return;
    }
  }, [applicationDetails, navigate]);

  useEffect(() => {
    if (applicationFeeData?.data?.fee_amount !== undefined) {
      setAmount(applicationFeeData.data.fee_amount);
    }
  }, [applicationFeeData]);

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

    // validations
    const nameVal = isValidName(normalizedName);
    const ibanVal = isValidIban(normalizedIban);
    const bicVal = isValidBic(normalizedBic, normalizedIban);

    const newErrors = {
      name: nameVal.isValid ? "" : nameVal.message,
      iban: ibanVal.isValid ? "" : ibanVal.message,
      bic: bicVal.isValid ? "" : bicVal.message,
      sepaMandateCheck: form.sepaMandateChecked
        ? ""
        : t("license.paymentForm.sepaMandateError"),
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

    try {
      show();
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

      try {
        await updateApplication.mutateAsync({
          applicationId,
          data: {
            application_status: ApplicationStatusApiEnum.PAYMENT_RECEIVED,
          },
        });
      } catch (err) {
        console.error(
          "Failed to update application status after payment:",
          err
        );
      }

      const stateData: StateProps = {
        amount: amount,
        application_id: applicationId,
        cadastral_reference: applicationDetails?.cadastral_reference,
        license_type: applicationDetails?.license_type,
        payment_id: response.data.id,
        payment_date: response.data.payment_date ?? Date.now().toString(),
        payment_status: response.data.payment_status,
        name: normalizedName,
        iban: normalizedIban,
        bic: normalizedBic,
      };

      navigate(`/payment/${applicationId}/done`, {
        state: {
          ...stateData,
        },
      });
    } catch (err) {
      setErrors((prev) => ({ ...prev, pay: "Payment failed" }));
    } finally {
      setLoading(false);
      hide();
    }
  };

  const [bicFocused, setBicFocused] = useState(false);

  return (
    <div className="container mx-auto px-4 md:px-6">
      <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
        {!loading && (
          <div className="font-inter min-w-96">
            <FormHeader
              heading={t("license.paymentForm.index.headline")}
              subHeading={t("license.paymentForm.index.subHeadline")}
            />
            <div className="relative mt-12 bg-gray-50 text-mallorca-purple px-3 pt-5 pb-2 rounded-xl">
              <div className="flex justify-between items-baseline text-xl font-semibold">
                <span>{t("license.paymentForm.index.amountLabel") + ": "}</span>
                <div className="flex flex-col items-end">
                  <span className="leading-none">
                    {isFetchingAppData || isFetchingAppFee ? (
                      <div className="w-32 h-6 rounded-md overflow-hidden relative">
                        <div className="absolute inset-0 bg-linear-to-r from-gray-200 via-mallorca-purple/30 to-gray-200 animate-shimmer" />
                      </div>
                    ) : (
                      formatAmount(amount, t) || "N/A"
                    )}
                  </span>
                  <span className="text-xs font-light italic mt-1 leading-none">
                    {t("license.paymentForm.index.taxLabel")}
                  </span>
                </div>
              </div>

              <hr className="my-4 border-gray-300" />

              <div className="flex justify-between items-baseline text-md font-normal">
                <span>
                  {t("license.paymentForm.index.cadastralIdLabel") + ": "}
                </span>
                <div className="flex flex-col items-end">
                  <span className="leading-none">
                    {isFetchingAppData ? (
                      <div className="w-32 h-6 rounded-md overflow-hidden relative">
                        <div className="absolute inset-0 bg-linear-to-r from-gray-200 via-mallorca-purple/30 to-gray-200 animate-shimmer" />
                      </div>
                    ) : (
                      applicationDetails?.cadastral_reference || "N/A"
                    )}
                  </span>
                </div>
              </div>

              <div className="flex justify-between items-baseline text-md font-normal">
                <span>
                  {t("license.paymentForm.index.licenseTypeLabel") + ": "}
                </span>
                <div className="flex flex-col items-end">
                  <span className="leading-none">
                    {isFetchingAppData || isFetchingAppFee ? (
                      <div className="w-32 h-6 rounded-md overflow-hidden relative">
                        <div className="absolute inset-0 bg-linear-to-r from-gray-200 via-mallorca-purple/30 to-gray-200 animate-shimmer" />
                      </div>
                    ) : (
                      applicationDetails?.license_type || "N/A"
                    )}
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
                  {t("license.paymentForm.index.nameLabel")}
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
                  {t("license.paymentForm.index.ibanLabel")}
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
                  {t("license.paymentForm.index.bicLabel")}
                  {!form.bic &&
                    !bicFocused &&
                    t("license.paymentForm.index.bicLabelOptional")}
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
                    {t(
                      "license.paymentForm.index.acceptSepaMandateLabel.plain"
                    )}
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        setShowSepaDialog((prev) => !prev);
                      }}
                      className="ml-1 text-mallorca-purple underline hover:text-mallorca-purple/80"
                    >
                      {t(
                        "license.paymentForm.index.acceptSepaMandateLabel.button"
                      )}
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
                  "license.paymentForm.sepaMandateDialog.acceptButtonLabel"
                )}
                cancelButtonLabel={t(
                  "license.paymentForm.sepaMandateDialog.cancelButtonLabel"
                )}
                downloadButtonLabel={t(
                  "license.paymentForm.sepaMandateDialog.downloadButtonLabel"
                )}
                downloadFileName={t(
                  "license.paymentForm.sepaMandateDialog.downloadFileName",
                  { accountHolder: form.name ? " " + form.name : "" }
                )}
                title={
                  t("license.paymentForm.sepaMandateDialog.title") + "\n\n"
                }
                text={
                  t("license.paymentForm.sepaMandateDialog.text.line1") +
                  "\n\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line2") +
                  "\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line3") +
                  "\n\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line4") +
                  "\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line5", {
                    accountHolder: form.name ? form.name : "",
                  }) +
                  "\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line6", {
                    iban: form.iban ? form.iban : "",
                  }) +
                  (form.bic ? "\n" : "") +
                  (form.bic
                    ? t("license.paymentForm.sepaMandateDialog.text.line7", {
                        bic: form.bic,
                      })
                    : "") +
                  "\n\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line8") +
                  "\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line9", {
                    beneficiaryName: t("app.contact.legalName"),
                  }) +
                  "\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line10", {
                    creditorId: "ES98ZZZ09999999999",
                  }) +
                  "\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line11", {
                    mandateReference: `ESM-${new Date().getFullYear()}-${Math.floor(
                      Math.random() * 100_000
                    )
                      .toString()
                      .padStart(5, "0")}`,
                  }) +
                  "\n\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line12") +
                  "\n\n\n" +
                  t("license.paymentForm.sepaMandateDialog.text.line13", {
                    date: formatDateLong(Date.now(), t),
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
                {t("license.paymentForm.index.payButtonLabel")}
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
                {t("license.paymentForm.index.cancelButtonLabel")}
              </button>
              <p className="mt-3 font-inter text-center text-gray-600 text-sm">
                {t("license.paymentForm.index.cancelNote.label1")}
              </p>
              <p className="mt-1 font-inter text-center text-gray-600 text-sm">
                {t("license.paymentForm.index.cancelNote.label2")}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
