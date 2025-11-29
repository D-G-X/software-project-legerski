import React, {useState} from "react";
import {validateResults} from "app/common/utils";
import {
  validateBic,
  validateIban,
  validateName,
  validateSepaMandateCheck
} from "../common/validationRules";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import {FormHeader} from "app/common/headingTitle";
import {useCreatePayment} from "app/services/payments/payments"
import SepaMandateDialog from "../common/modal-dialog/modal-dialog";
import {useNavigate, useParams} from "react-router";
import "./paymentForm.css";
import {AnimatedDots} from "../common/AnimatedDots";

type Props = {
  payment_id?: number;
  application_id: number;
  amount?: number;
  name: string;
  iban: string;
  bic: string;
  payment_date?: string;
  payment_status?: string;
};

export default function PaymentConfirm() {
  const {applicationId} = useParams();  // TODO: get the actual application ID from submit (application form) or GET request
  const amount = 9999.99; // TODO: get the actual amount to be paid from GET request
  console.log("Application ID:", applicationId);

  const {t} = useTranslation();
  const navigate = useNavigate()
  const mutation = useCreatePayment();
  useDocumentTitle(t("payment.title"));

  const getFormattedDate = () => {
    const now = new Date();
    return now.toLocaleDateString(t("locale"), {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };
  // modal dialog state
  const [showSepaDialog, setShowSepaDialog] = useState(false);
  // loading screen state
  const [loading, setLoading] = useState(false);

  // Format amount as currency according to locale
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
  };

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
    e.preventDefault()
    if(!applicationId){
      console.error("No application ID found in URL parameters.");
      return;
    }
    const {id, value, type} = e.target;
    let finalValue;

    if (id === "iban" && type === "text") {
      finalValue = formatIban(value);
    } else if (id === "bic" && type === "text") {
      finalValue = formatBic(value);
    } else {
      finalValue = value;
    }

    setForm((prev) => ({...prev, [id]: finalValue}));
    setErrors((prev) => ({...prev, [id]: ""}));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!applicationId) {
      return;
    }

    const nameValidateResult: validateResults = validateName(form.name.trim());
    const ibanValidateResult: validateResults = validateIban(form.iban.replace(/\s+/g, ''));
    const bicValidateResult: validateResults = validateBic(form.bic.replace(/\s+/g, ''), form.iban.replace(/\s+/g, ''));
    const sepaMandateCheckValidateResult: validateResults = validateSepaMandateCheck(form.sepaMandateChecked);
    let newErrors = {
      name: "",
      iban: "",
      bic: "",
      sepaMandateCheck: "",
      pay: "",
    };

    if (!nameValidateResult.isValid) {
      newErrors.name = nameValidateResult.message;
    }
    if (!ibanValidateResult.isValid) {
      newErrors.iban = ibanValidateResult.message;
    }
    if (!bicValidateResult.isValid) {
      newErrors.bic = bicValidateResult.message;
    }
    if (!sepaMandateCheckValidateResult.isValid) {
      newErrors.sepaMandateCheck = sepaMandateCheckValidateResult.message;
    }

    if (
        newErrors.name ||
        newErrors.iban ||
        newErrors.bic ||
        newErrors.sepaMandateCheck
    ) {
      setErrors(newErrors);
      console.log(newErrors);
      return;
    }
    setErrors({
      name: "",
      iban: "",
      bic: "",
      sepaMandateCheck: "",
      pay: "",
    });
    // If the IBAN is from Spain and BIC is invalid, clear the BIC field before submission
    if (form.iban.slice(0, 2) === 'ES' && !bicValidateResult.isValid) {
      form.bic = "";
    }

    const postData = {
      application_id: Number(applicationId),
      name: form.name.trim(),
      iban: form.iban.replace(/\s+/g, ''),
      bic: form.bic.replace(/\s+/g, ''),
    }

    try{
      console.log("Submitting payment data:", postData);
      setLoading(true);
      const response = await mutation.mutateAsync({
        applicationId: Number(applicationId),
        data: {...postData}
      });

      const last = response.data[];

      if /*(!Array.isArray(response) || response.length === 0)*/( last === null ) { // TODO: fix response is an array in orval config
        console.error("Payment API returned empty or invalid response:", response);
        throw new Error("Empty response");
      }

      console.log("Payment API response:", response);

      const fieldsToMatch = ["application_id", "name", "iban", "bic"]
      // TODO: fix response is an array in orval config
      const responseMatchesPost = postData===last/*fieldsToMatch.every(
          (key) => postData[key] === last[key]
      )*/;

      if(!responseMatchesPost){
        console.error("Payment API response does not match submitted data:", {
          submitted: postData,
          received: last
        });
        throw new Error("Response data mismatch");
      }

      const stateData: Props = {
        ...postData,
        payment_id: last.id,
        amount: amount, // TODO: get the actual amount from response if available
        payment_date: last.payment_date,
        payment_status: last.payment_status,
      }

      navigate(`/payment/${applicationId}/done`, {
        state: stateData, // pass API result to the next page
      });
    } catch (error: any) {
      console.error("Payment submission error:", error);
      setErrors(prev => ({ ...prev, pay: "Payment failed" }));
    } finally {
      setLoading(false);
    }
  };

  const [bicFocused, setBicFocused] = useState(false);

  return (
      <div className="container mx-auto px-4 md:px-6">
        <div
            className="relative min-h-[calc(100vh-4rem)] bg-white flex items-center justify-center">
          {!loading ?
              <div className="font-inter min-w-96">
                <FormHeader
                    heading={t("paymentForm.index.headline")}
                    subHeading={t("paymentForm.index.subHeadline")}
                />
                {/* Amount Field */}
                <div className="relative mt-12 bg-gray-50 px-3 pt-5 pb-2 rounded-xl">
                  <div className="flex justify-between items-baseline text-xl font-semibold">
                    {/* Left: label */}
                    <span>{t("paymentForm.index.amountLabel") + ": "}</span>

                    {/* Right: amount */}
                    <div className="flex flex-col items-end">
                      <span className="text-xl font-semibold leading-none">
                        {formatAmount(amount)}
                      </span>
                      <span className="text-xs font-light italic mt-1 leading-none">
                        {t("paymentConfirm.index.taxLabel")}
                      </span>
                    </div>
                  </div>
                </div>

                {/* PaymentForm Form */}
                <div>
                  {/* Name Field */}
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

                  {/* IBAN Field */}
                  <div className="relative my-4">
                    <input
                        type="text"
                        id="iban"
                        value={form.iban}
                        placeholder=""
                        onChange={handleChange}
                        className="peer border border-mallorca-purple rounded-xl h-12 w-96 px-3 pt-5 pb-2 text-mallorca-purple placeholder-transparent focus:outline-none focus:ring-1 focus:ring-mallorca-purple"
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

                  {/* BIC Field */}
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
                      {(!form.bic && !bicFocused) && (
                          t("paymentForm.index.bicLabelOptional")
                      )}
                    </label>
                    {errors.bic && (
                        <div className="text-red-500 mt-1 pl-4 text-xs">
                          {errors.bic}
                        </div>
                    )}
                  </div>

                  {/* SEPA Mandate Check Field */}
                  <div className="relative my-4 pt-2">
                    <div className="flex items-center gap-4">
                      {/* Label incl. Button */}
                      <label
                          htmlFor="sepaMandateChecked"
                          className="text-mallorca-purple/70 text-lg"
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
                  <SepaMandateDialog
                      open={showSepaDialog}
                      showDownloadButton={true}
                      acceptButtonLabel={t("paymentForm.sepaMandateDialog.acceptButtonLabel")}
                      cancelButtonLabel={t("paymentForm.sepaMandateDialog.cancelButtonLabel")}
                      downloadButtonLabel={t("paymentForm.sepaMandateDialog.downloadButtonLabel")}
                      downloadFileName={t("paymentForm.sepaMandateDialog.downloadFileName", {accountHolder: form.name ? (" " + form.name) : ""})}
                      title={t("paymentForm.sepaMandateDialog.title") + "\n\n"}
                      text={(
                          t("paymentForm.sepaMandateDialog.text.line1") + "\n\n" +
                          t("paymentForm.sepaMandateDialog.text.line2") + "\n" +
                          t("paymentForm.sepaMandateDialog.text.line3") + "\n\n" +
                          t("paymentForm.sepaMandateDialog.text.line4") + "\n" +
                          t("paymentForm.sepaMandateDialog.text.line5", {accountHolder: form.name ? form.name : ""}) + "\n" +
                          t("paymentForm.sepaMandateDialog.text.line6", {iban: form.iban ? form.iban : ""}) + (form.bic ? "\n" : "") +
                          (form.bic ? (t("paymentForm.sepaMandateDialog.text.line7", {bic: form.bic})) : "") + "\n\n" +
                          t("paymentForm.sepaMandateDialog.text.line8") + "\n" +
                          t("paymentForm.sepaMandateDialog.text.line9", {beneficiaryName: t("app.contact.legalName")}) + "\n" +
                          t("paymentForm.sepaMandateDialog.text.line10", {creditorId: "ES98ZZZ09999999999"}) + "\n" +
                          t("paymentForm.sepaMandateDialog.text.line11", {mandateReference: "ESM-2025-00001"}) + "\n\n" +
                          t("paymentForm.sepaMandateDialog.text.line12") + "\n\n\n" +
                          t("paymentForm.sepaMandateDialog.text.line13", {date: getFormattedDate() ? getFormattedDate() : ""})
                      )}
                      t={t}
                      onAccept={() => {
                        setShowSepaDialog(false);
                        setForm(prev => ({...prev, sepaMandateChecked: true}));
                      }}
                      onCancel={() => setShowSepaDialog(false)}
                  />
                </div>

                <div className="my-2">
                  {errors.pay && (
                      <div className="text-red-500 mt-2 pl-4">{errors.pay}</div>
                  )}
                  <button
                      type="submit"
                      onClick={handleSubmit}
                      className={`mt-4
                ${errors.pay
                          ? "bg-mallorca-purple/75"
                          : "bg-mallorca-purple"
                      } text-white px-10 py-2 rounded-md w-96 font-medium text-lg hover:bg-mallorca-purple/90`}
                  >
                    {t("paymentForm.index.payButtonLabel")}
                  </button>
                </div>
              </div>
              :
              <div className="font-inter text-center">
                <div
                    className="w-16 h-16 border-6 border-gray-200 border-t-mallorca-purple rounded-full animate-spin mx-auto"/>
                <p className="mt-6 font-light text-xl text-gray-600 relative inline-block">{t("paymentForm.processing")}
                  <span className="absolute left-full">
                <AnimatedDots speed={400}/>
              </span>
                </p>
              </div>
          }
        </div>
      </div>
  );
}
