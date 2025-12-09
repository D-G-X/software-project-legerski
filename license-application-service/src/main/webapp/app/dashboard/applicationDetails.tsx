import React from "react";
import {useTranslation} from "react-i18next";
import {FormHeader} from "app/common/headingTitle";
import {
  GetApplicationDocuments200,
  ApplicationPaymentResource,
  ApplicationResource,
  LicenseResource,
  UserResource
} from "../../types";
import {Minimize2} from "lucide-react";
import "./applicationDetails.css";
import {useGetApplication} from "../services/applications/applications";
import {useListPayments} from "../services/payments/payments";
import {useGetUser} from "../services/users/users";
import {useGetLicense} from "../services/licenses/licenses";
import {useGetApplicationDocuments} from "../services/document-verification/document-verification";

interface ApplicationDetailsProps {
  open: boolean;
  entry: ApplicationResource | null;
  onClose: () => void;
}

function getUserData(userId: string): UserResource | null {
  const {data} = useGetUser(userId);
  return data?.data ?? null;
}

function getApplicationData(applicationId: number): ApplicationResource | null {
  const {data} = useGetApplication(applicationId);
  return data?.data ?? null;
}

function getLicenseData(applicationId: number): LicenseResource | null {
  const {data} = useGetLicense(applicationId);
  return data?.data ?? null;
}

function getDocumentData(applicationId: number): GetApplicationDocuments200 | null {
  const {data} = useGetApplicationDocuments(applicationId);
  return data?.data ?? null;
}

function getPaymentData(applicationId: number): ApplicationPaymentResource | null {
  const {data} = useListPayments(applicationId);
  return data?.data?.[0] ?? null;
}

export default function ApplicationDetails({open, entry, onClose}: ApplicationDetailsProps) {
  if (!open || !entry) return null;

  const {t} = useTranslation();

  const userData = getUserData("f28d1d3b-9bcb-4a74-a65a-2fede2b0a6c3");//entry.user_id);

  const applicationData = getApplicationData(201);//entry.id);

  const licenseData = getLicenseData(101);//entry.id);

  const documentData = getDocumentData(entry.id);

  const paymentData = getPaymentData(201);//entry.id);

  const formatAmount = (raw: number | undefined): string => {
    if (raw === undefined) return "";
    return new Intl.NumberFormat(t("locale"), {
      style: "currency",
      currency: "EUR",
      minimumFractionDigits: 2,
    }).format(raw);
  }

  const formatIban = (raw: string | undefined): string => {
    if (raw === undefined || raw === "") return "";
    const visibleLength = 2;
    const clean = raw.replace(/\s+/g, "").toUpperCase();
    const visible = clean.slice(-visibleLength);
    const masked = "•".repeat(clean.length - visibleLength);
    return (masked + visible).replace(/(.{4})/g, "$1 ").trim();
  };

  const formatBic = (raw: string | undefined): string => {
    if (raw === undefined || raw === "") return "";
    return raw.replace(/\s+/g, "").toUpperCase();
  }

  const formatDate = (raw: string | undefined): string => {
    if (raw === undefined || raw === "") return "";
    return new Date(raw).toLocaleString(t("locale"), {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });
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
            <div className="font-inter max-w-[calc(100vb-8rem)]">

              <FormHeader
                  heading={t("applicationDetails.index.headline")}
                  subHeading={t("")}
                  className="my-8"
              />

              {/* Application fields */}
              <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                <div className="flex justify-between min-w-128 font-semibold">
                  <span>{t("applicationDetails.index.applicationIdLabel") + ": "}</span>
                  <span>{applicationData?.id}</span>
                </div>

                <hr className="my-2 border-gray-300"/>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.licenseTypeLabel") + ": "}</span>
                  <span>{applicationData?.license_type}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.cadastralIdLabel") + ": "}</span>
                  <span>{applicationData?.cadastral_reference}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.statusLabel") + ": "}</span>
                  <span>{applicationData?.application_status}</span>
                </div>

                {applicationData?.remarks && (
                    <div className="flex justify-between min-w-128">
                      <span>{t("applicationDetails.index.remarksLabel") + ": "}</span>
                      <span>{applicationData?.remarks}</span>
                    </div>
                )}

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.appliedOnLabel") + ": "}</span>
                  <span>{formatDate(applicationData?.applied_at)}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.lastUpdatedLabel") + ": "}</span>
                  <span>{formatDate(applicationData?.changed_at)}</span>
                </div>

              </div>

              {/* User fields */}
              <div className="text-lg mt-4 text-mallorca-purple/70">
                {t("applicationDetails.index.user.label")}
              </div>

              <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.user.nameLabel") + ": "}</span>
                  <span>{userData?.firstName + " " + userData?.lastName}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.user.emailLabel") + ": "}</span>
                  <span>{userData?.email}</span>
                </div>

              </div>

              {/* License fields */}
              <div className="text-lg mt-4 text-mallorca-purple/70">
                {t("applicationDetails.index.license.label")}
              </div>

              <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.license.idLabel") + ": "}</span>
                  <span>{licenseData?.id}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.license.statusLabel") + ": "}</span>
                  <span>{licenseData?.license_status}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.license.issuedOnLabel") + ": "}</span>
                  <span>{formatDate(licenseData?.issued_at)}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.license.expiresOnLabel") + ": "}</span>
                  <span>{formatDate(licenseData?.expires_at)}</span>
                </div>

              </div>

              {/* Document fields */}
              <div className="text-lg mt-4 text-mallorca-purple/70">
                {t("applicationDetails.index.documents.label")}
              </div>

              <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.documents.statusLabel") + ": "}</span>
                  <span>{documentData?.status}</span>
                </div>

                {documentData?.rejection_reason && (
                    <div className="flex justify-between min-w-128">
                      <span>{t("applicationDetails.index.documents.rejectionReasonLabel") + ": "}</span>
                      <span>{documentData?.rejection_reason}</span>
                    </div>
                )}
              </div>

              {/* Payment fields */}
              <div className="text-lg mt-4 text-mallorca-purple/70">
                {t("applicationDetails.index.payment.label")}
              </div>

              <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.payment.idLabel") + ": "}</span>
                  <span>{paymentData?.id}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.payment.amountLabel") + ": "}</span>
                  <span>{formatAmount(paymentData?.amount) + "*"}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.payment.nameLabel") + ": "}</span>
                  <span>{paymentData?.name}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.payment.ibanLabel") + ": "}</span>
                  <span>{formatIban(paymentData?.iban)}</span>
                </div>

                {paymentData?.bic && (
                    <div className="flex justify-between min-w-128">
                      <span>{t("applicationDetails.index.payment.bicLabel") + ": "}</span>
                      <span>{formatBic(paymentData?.bic)}</span>
                    </div>
                )}

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.payment.dateLabel") + ": "}</span>
                  <span>{formatDate(paymentData?.payment_date)}</span>
                </div>

                <div className="flex justify-between min-w-128">
                  <span>{t("applicationDetails.index.payment.statusLabel") + ": "}</span>
                  <span>{paymentData?.payment_status}</span>
                </div>

              </div>
              <div className="px-6 mt-1">
                <span
                    className="text-xs font-light italic">{"*" + t("applicationDetails.index.payment.taxLabel")}
                </span>
              </div>

            </div>
          </div>
        </div>
        {/* Spacer to push content above the footer */}
        <div className="mb-12"></div>
      </div>
  );
}
