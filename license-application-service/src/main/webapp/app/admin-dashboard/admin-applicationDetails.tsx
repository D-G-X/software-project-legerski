import React from "react";
import {useTranslation} from "react-i18next";
import {FormHeader} from "app/common/headingTitle";
import {
  ApplicationPaymentResource,
  ApplicationResource,
  GetApplicationDocuments200,
  LicenseResource
} from "../../types";
import {Minimize2} from "lucide-react";
import "./admin-applicationDetails.css";
import {useListPayments} from "../services/payments/payments";
import {useGetLicense, useListLicenses} from "../services/licenses/licenses";
import {useGetApplicationDocuments} from "../services/document-verification/document-verification";
import {formatAmount, formatBic, formatDateLong, formatIban} from "../common/format";
import {AnimatedDots} from "../common/AnimatedDots";

interface ApplicationDetailsProps {
  open: boolean;
  userId: string;
  applicationData: ApplicationResource | undefined;
  onClose: () => void;
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

function useGetLicenseData(
    userId: string,
    applicationId: number,
    applicationStatus?: string
): LicenseResource | undefined {
  const enabledByStatus = SHOW_LICENSE_STATUSES.includes(applicationStatus as any);

  const {data: listResponse} = useListLicenses(
      {user_id: userId},
      {query: {enabled: enabledByStatus}}
  );

  const licenses = listResponse?.data ?? [];
  const matching = licenses.find(lic => lic.application_id === applicationId);

  const {data: licenseResponse} = useGetLicense(
      matching?.id ?? 0,
      {query: {enabled: enabledByStatus && !!matching?.id}}
  );

  return licenseResponse?.data;
}

function useGetDocumentData(
    applicationId: number,
    applicationStatus?: string
): GetApplicationDocuments200 | undefined {
  const enabledByStatus = SHOW_DOCUMENT_STATUSES.includes(applicationStatus as any);

  const {data: response} = useGetApplicationDocuments(
      applicationId,
      {query: {enabled: enabledByStatus}}
  );

  return response?.data;
}

function useGetPaymentData(
    applicationId: number,
    applicationStatus?: string
): ApplicationPaymentResource | undefined {
  const enabledByStatus = SHOW_PAYMENT_STATUSES.includes(applicationStatus as any);

  const {data: response} = useListPayments(
      applicationId,
      {query: {enabled: enabledByStatus}}
  );

  return response?.data?.[-1]; // Get the latest payment
}

export default function AdminApplicationDetails({
                                                  open,
                                                  userId,
                                                  applicationData,
                                                  onClose,
                                                }: ApplicationDetailsProps) {
  if (!open || !applicationData) {
    console.log("ApplicationDetails: not open");
    return null;
  } else {
    console.log("ApplicationDetails: open");
  }
  const {t} = useTranslation();

  if (!applicationData || !applicationData.id) {
    alert("Missing application or user data");
    return null;
  }
  const licenseData = useGetLicenseData(
      userId,
      applicationData.id,
      applicationData.application_status
  );

  const documentData = useGetDocumentData(
      applicationData.id,
      applicationData.application_status
  );

  const paymentData = useGetPaymentData(
      applicationData.id,
      applicationData.application_status
  );

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

              {/* License fields */}
              {SHOW_LICENSE_STATUSES.includes(applicationData?.application_status as any) && (
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
              {SHOW_DOCUMENT_STATUSES.includes(applicationData?.application_status as any) && (
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
              {SHOW_PAYMENT_STATUSES.includes(applicationData?.application_status as any) && (
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
                {SHOW_PAYMENT_STATUSES.includes(applicationData?.application_status as any) && (
                    <span
                        className="text-xs font-light italic">{"*" + t("applicationDetails.index.payment.taxLabel")}
                </span>
                )}
              </div>
            </div>
          </div>
        </div>
        {/* Spacer to push content above the footer */
        }
        <div className="mb-12"></div>
      </div>
  )
      ;
}