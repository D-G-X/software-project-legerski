import React, {useState} from "react";
import {useTranslation} from "react-i18next";
import {FormHeader} from "app/common/headingTitle";
import {
  ApplicationPaymentResource,
  ApplicationResource,
  GetApplicationDocuments200,
  LicenseResource,
  UserResource
} from "../../types";
import {Minimize2} from "lucide-react";
import "./applicationDetails.css";
import {useListPayments} from "../services/payments/payments";
import {useGetUser} from "../services/users/users";
import {useDeleteLicense, useGetLicense, useListLicenses} from "../services/licenses/licenses";
import {useGetApplicationDocuments} from "../services/document-verification/document-verification";
import {formatAmount, formatBic, formatDateLong, formatIban} from "../common/format";
import {AxiosError} from "axios";
import {AnimatedDots} from "../common/AnimatedDots";
import ConfirmPopup from "../common/confirmPopup";
import {useQueryClient} from "@tanstack/react-query";

interface ApplicationDetailsProps {
  open: boolean;
  applicationData: ApplicationResource | null;
  onClose: () => void;
  onRenew: () => void;
}

type UserDataResult = {
  data: UserResource | null;
  isFound: boolean;
  isLoading: boolean;
};

type LicenseDataResult = {
  data: LicenseResource | null;
  isFound: boolean;
  isLoading: boolean;
};

type DocumentDataResult = {
  data: GetApplicationDocuments200 | null;
  isFound: boolean;
  isLoading: boolean;
};

type PaymentDataResult = {
  data: ApplicationPaymentResource | null;
  isFound: boolean;
  isLoading: boolean;
}

const SHOW_LICENSE_STATUSES = ["SELECTED", "PAYMENT_RECEIVED"] as const;

const SHOW_DOCUMENT_STATUSES = [
  "SELECTED",
  "PAYMENT_RECEIVED",
  "SUBMITTED",
  "UNDER_REVIEW",
  "AWAITING_PAYMENT",
  "APPROVED",
  "IN_BALLOT",
  "CANCELLED",
  "REJECTED",
  "NOT_SELECTED",
  "DOCUMENTS_SUBMITTED",
  "VERIFICATION_PENDING",
] as const;

const SHOW_PAYMENT_STATUSES = [
  "SELECTED",
  "PAYMENT_RECEIVED",
  "SUBMITTED",
  "UNDER_REVIEW",
  "APPROVED",
  "IN_BALLOT",
  "CANCELLED",
  "REJECTED",
  "NOT_SELECTED",
] as const;

function useGetUserData(userId: string): UserDataResult {
  const {
    data: response,
    error,
    isLoading
  } = useGetUser(userId);

  const isFound =
      error === undefined ||
      (error as AxiosError | undefined)?.response?.status !== 404;

  return {
    data: response?.data ?? null,
    isFound,
    isLoading
  };
}

function useGetLicenseData(
    userId: string,
    applicationId: number,
    applicationStatus?: string
): LicenseDataResult {
  const enabledByStatus = SHOW_LICENSE_STATUSES.includes(applicationStatus as any);

  const { data: listResponse } = useListLicenses(
      { user_id: userId },
      { query: { enabled: enabledByStatus } }
  );

  const licenses = listResponse?.data ?? [];
  const matching = licenses.find(lic => lic.application_id === applicationId);

  const {
    data: licenseResponse,
    error,
    isLoading
  } = useGetLicense(
      matching?.id ?? 0,
      { query: { enabled: enabledByStatus && !!matching?.id } }
  );

  const isFound =
      error === undefined ||
      (error as AxiosError | undefined)?.response?.status !== 404;

  return {
    data: licenseResponse?.data ?? null,
    isFound,
    isLoading
  };
}

function useGetDocumentData(
    applicationId: number,
    applicationStatus?: string
): DocumentDataResult {
  const enabledByStatus = SHOW_DOCUMENT_STATUSES.includes(applicationStatus as any);

  const {
    data: response,
    error,
    isLoading
  } = useGetApplicationDocuments(
      applicationId,
      { query: { enabled: enabledByStatus } }
  );

  const isFound =
      error === undefined ||
      (error as AxiosError | undefined)?.response?.status !== 404;

  return {
    data: response?.data ?? null,
    isFound,
    isLoading
  };
}

function useGetPaymentData(
    applicationId: number,
    applicationStatus?: string
): PaymentDataResult {
  const enabledByStatus = SHOW_PAYMENT_STATUSES.includes(applicationStatus as any);

  const {
    data: response,
    error,
    isLoading
  } = useListPayments(
      applicationId,
      { query: { enabled: enabledByStatus } }
  );

  const isFound =
      error === undefined ||
      (error as AxiosError | undefined)?.response?.status !== 404;

  return {
    data: response?.data?.[0] ?? null,
    isFound,
    isLoading
  };
}

export default function ApplicationDetails({
                                             open,
                                             applicationData,
                                             onClose,
                                             onRenew,
                                           }: ApplicationDetailsProps) {
  if (!open || !applicationData) {
    console.log("ApplicationDetails: not open");
    return null;
  } else {
    console.log("ApplicationDetails: open");
  }
  const {t} = useTranslation();
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const openPopup = () => setIsPopupOpen(true);
  const closePopup = () => setIsPopupOpen(false);

  const {
    data: userData,
    isFound: foundUser,
    isLoading: isLoadingUser
  } = useGetUserData(applicationData?.user_id);

  const {
    data: licenseData,
    isFound: foundLicense,
    isLoading: isLoadingLicense
  } = useGetLicenseData(
      userData?.id ?? "",
      applicationData.id,
      applicationData.application_status
  );

  const {
    data: documentData,
    isFound: foundDocument,
    isLoading: isLoadingDocument
  } = useGetDocumentData(
      applicationData.id,
      applicationData.application_status
  );

  const {
    data: paymentData,
    isFound: foundPayment,
    isLoading: isLoadingPayment
  } = useGetPaymentData(
      applicationData.id,
      applicationData.application_status
  );

  const queryClient = useQueryClient();

  const { mutate: deleteLicense } = useDeleteLicense({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: ["listLicenses"] });
        await queryClient.invalidateQueries({ queryKey: ["getLicense"] });

        closePopup();
      },
      onError: (err) => {
        console.error(err);
        alert("Error while releasing license");
      },
    },
  });

  function handleReleaseLicense() {
    if (!licenseData?.id) return;
    deleteLicense({ licenseId: licenseData.id });
    closePopup();
  }

  return (
      <div
          className="fixed inset-0 z-50 flex flex-col bg-black/50 overflow-auto"
          aria-modal="true"
          role="dialog"
      >
        {/* Spacer to push content below the header */}
        <div className="mt-20"></div>
        {/* Overlay to close the modal when clicking outside */}
        <div
            className="absolute inset-0"
            onClick={onClose}
        />

        <div className="container mx-auto px-4 md:px-6">

          <div
              className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center rounded-xl overflow-y-auto">
            {/* Close Button */}
            <button
                onClick={onClose}
                className="absolute flex top-4 right-4 items-center justify-center cursor-pointer px-3 py-1 rounded-md border
             text-mallorca-purple/75 hover:bg-mallorca-purple/10"
            ><Minimize2/>
            </button>
            {(isLoadingUser || isLoadingLicense || isLoadingDocument || isLoadingPayment) ?
                <div className="font-inter text-center">
                  {/* Loading Screen */}
                  <div
                      className="w-16 h-16 border-6 border-gray-200 border-t-mallorca-purple rounded-full animate-spin mx-auto"/>
                  <p className="mt-6 font-light text-xl text-gray-600 relative inline-block">
                    {t("applicationDetails.loading")}
                    <span className="absolute left-full">
                      <AnimatedDots speed={400}/>
                    </span>
                  </p>
                </div>
                :
                <div className="font-inter max-w-[calc(100vb-8rem)]">

                  <FormHeader
                      heading={t("applicationDetails.index.headline")}
                      subHeading={t("")}
                      className="mt-16"
                  />

                  {/* Application fields */}
                  <div className="bg-gray-50 rounded-lg p-6 mt-12 shadow space-y-2.5 text-gray-700">

                    <div className="flex justify-between min-w-lg font-semibold">
                      <span>{t("applicationDetails.index.applicationIdLabel") + ": "}</span>
                      <span>{applicationData?.id}</span>
                    </div>

                    <hr className="my-2 border-gray-300"/>

                    <div className="flex justify-between min-w-lg">
                      <span>{t("applicationDetails.index.licenseTypeLabel") + ": "}</span>
                      <span>{applicationData?.license_type}</span>
                    </div>

                    <div className="flex justify-between min-w-lg">
                      <span>{t("applicationDetails.index.cadastralIdLabel") + ": "}</span>
                      <span>{applicationData?.cadastral_reference}</span>
                    </div>

                    <div className="flex justify-between min-w-lg">
                      <span>{t("applicationDetails.index.statusLabel") + ": "}</span>
                      <span
                          className={`${
                              // green
                              ["SELECTED", "PAYMENT_RECEIVED"].includes(
                                  applicationData?.application_status
                              )
                                  ? "text-green-800"
                                  : // blue
                                  ["SUBMITTED",
                                    "UNDER_REVIEW",
                                    "AWAITING_PAYMENT",
                                    "APPROVED",
                                    "IN_BALLOT",]
                                  .includes(applicationData?.application_status)
                                      ? "text-blue-800"
                                      :
                                      ["CANCELLED", "REJECTED", "NOT_SELECTED",].includes(
                                          applicationData?.application_status
                                      )
                                          ? "text-red-800"
                                          : // grey
                                          ["DRAFT", "EXPIRED",].includes(applicationData?.application_status)
                                              ? "text-gray-800 "
                                              : // orange (all in-process)
                                              ["DOCUMENTS_SUBMITTED", "VERIFICATION_PENDING",].includes(applicationData?.application_status)
                                                  ? "text-orange-800"
                                                  : // fallback
                                                  "text-mallorca-purple"
                          }`}
                      >{applicationData?.application_status}</span>
                    </div>

                    {applicationData?.remarks && (
                        <div className="flex justify-between min-w-lg">
                          <span>{t("applicationDetails.index.remarksLabel") + ": "}</span>
                          <span>{applicationData?.remarks}</span>
                        </div>
                    )}

                    <div className="flex justify-between min-w-lg">
                      <span>{t("applicationDetails.index.appliedOnLabel") + ": "}</span>
                      <span>{formatDateLong(applicationData?.applied_at, t)}</span>
                    </div>

                    <div className="flex justify-between min-w-lg">
                      <span>{t("applicationDetails.index.lastUpdatedLabel") + ": "}</span>
                      <span>{formatDateLong(applicationData?.changed_at, t)}</span>
                    </div>

                  </div>

                  {/* User fields */}
                  {foundUser && (
                      <>
                        <div className="text-lg mt-4 text-mallorca-purple/70">
                          {t("applicationDetails.index.user.label")}
                        </div>
                        <div
                            className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.user.nameLabel") + ": "}</span>
                            <span>{userData?.firstName + " " + userData?.lastName}</span>
                          </div>

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.user.emailLabel") + ": "}</span>
                            <span>{userData?.email}</span>
                          </div>

                        </div>
                      </>
                  )}

                  {/* License fields */}
                  {SHOW_LICENSE_STATUSES.includes(applicationData?.application_status as any) && foundLicense && (
                      <>
                        <div className="text-lg mt-4 text-mallorca-purple/70">
                          {t("applicationDetails.index.license.label")}
                        </div>

                        <div
                            className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.license.idLabel") + ": "}</span>
                            <span>{licenseData?.id}</span>
                          </div>

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.license.statusLabel") + ": "}</span>
                            <span>{licenseData?.license_status}</span>
                          </div>

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.license.issuedOnLabel") + ": "}</span>
                            <span>{formatDateLong(licenseData?.issued_at, t)}</span>
                          </div>

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.license.expiresOnLabel") + ": "}</span>
                            <span>{formatDateLong(licenseData?.expires_at, t)}</span>
                          </div>

                        </div>
                      </>
                  )}

                  {/* Document fields */}
                  {SHOW_DOCUMENT_STATUSES.includes(applicationData?.application_status as any) && foundDocument && (
                      <>
                        <div className="text-lg mt-4 text-mallorca-purple/70">
                          {t("applicationDetails.index.documents.label")}
                        </div>

                        <div
                            className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.documents.statusLabel") + ": "}</span>
                            <span>{documentData?.status}</span>
                          </div>

                          {documentData?.rejection_reason && (
                              <div className="flex justify-between min-w-lg">
                                <span>{t("applicationDetails.index.documents.rejectionReasonLabel") + ": "}</span>
                                <span>{documentData?.rejection_reason}</span>
                              </div>
                          )}
                        </div>
                      </>
                  )}

                  {/* Payment fields */}
                  {SHOW_PAYMENT_STATUSES.includes(applicationData?.application_status as any) && foundPayment && (
                      <>
                        <div className="text-lg mt-4 text-mallorca-purple/70">
                          {t("applicationDetails.index.payment.label")}
                        </div>

                        <div
                            className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.payment.idLabel") + ": "}</span>
                            <span>{paymentData?.id}</span>
                          </div>

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.payment.amountLabel") + ": "}</span>
                            <span>{formatAmount(paymentData?.amount, t) + "*"}</span>
                          </div>

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.payment.nameLabel") + ": "}</span>
                            <span>{paymentData?.name}</span>
                          </div>

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.payment.ibanLabel") + ": "}</span>
                            <span>{formatIban(paymentData?.iban)}</span>
                          </div>

                          {paymentData?.bic && (
                              <div className="flex justify-between min-w-lg">
                                <span>{t("applicationDetails.index.payment.bicLabel") + ": "}</span>
                                <span>{formatBic(paymentData?.bic)}</span>
                              </div>
                          )}

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.payment.dateLabel") + ": "}</span>
                            <span>{formatDateLong(paymentData?.payment_date, t)}</span>
                          </div>

                          <div className="flex justify-between min-w-lg">
                            <span>{t("applicationDetails.index.payment.statusLabel") + ": "}</span>
                            <span>{paymentData?.payment_status}</span>
                          </div>

                        </div>
                      </>
                  )}
                  <div className="px-6 mt-1">
                    {foundPayment && (
                        <span
                            className="text-xs font-light italic">{"*" + t("applicationDetails.index.payment.taxLabel")}
                </span>
                    )}
                  </div>

                  <div className="my-12 flex flex-line items-center jusify-center gap-4">
                    {licenseData?.id && (
                        <>
                          {/* Renew License Button */}
                          <button
                              type="submit"
                              onClick={onRenew}
                              className={`bg-mallorca-purple text-white px-10 py-2 rounded-md ${licenseData?.id ? "w-64" : "w-96" } font-medium text-lg`}
                          >
                            {t("applicationDetails.buttons.renewLicenseLabel")}
                          </button>
                          {/* Release License Button */}
                          <button
                              type="submit"
                              onClick={openPopup}
                              className={`bg-red-500 text-white  px-10 py-2 rounded-md ${licenseData?.id ? "w-64" : "w-96" } font-medium text-lg hover:bg-red-700  border-red-950`}
                          >
                            {t("applicationDetails.buttons.releaseLicenseLabel")}
                          </button>
                        </>
                    )}
                  </div>

                  {isPopupOpen && (
                      <ConfirmPopup
                          open={isPopupOpen}
                          onCancel={() => setIsPopupOpen(false)}
                          onConfirm={handleReleaseLicense}
                          headingLabel={t("applicationDetails.confirmPopup.headingLabel")}
                          subHeadingLabel={t("applicationDetails.confirmPopup.subHeadingLabel")}
                          quoteTitle={t("applicationDetails.confirmPopup.quoteTitle")}
                          quoteText={[t("applicationDetails.confirmPopup.quoteText1"), t("applicationDetails.confirmPopup.quoteText2")]}
                          cancelLabel={t("applicationDetails.confirmPopup.cancelButtonLabel")}
                          confirmLabel={t("applicationDetails.confirmPopup.confirmButtonLabel")}
                      />
                  )}

                </div>

            }

          </div>
        </div>
        {/* Spacer to push content above the footer */
        }
        <div className="mb-12"></div>
      </div>
  )
      ;
}
