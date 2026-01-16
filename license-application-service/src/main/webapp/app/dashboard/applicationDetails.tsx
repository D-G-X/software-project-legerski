import React, {useContext, useState} from "react";
import {useTranslation} from "react-i18next";
import {FormHeader} from "app/common/headingTitle";
import {
  ApplicationPaymentResource,
  ApplicationResource,
  GetApplicationDocuments200,
  LicenseResource,
  UserResource,
} from "../../types";
import {CircleX} from "lucide-react";
import {useListPayments} from "../services/payments/payments";
import {deleteLicense, useGetLicense, useListLicenses,} from "../services/licenses/licenses";
import {useGetApplicationDocuments} from "../services/document-verification/document-verification";
import {
  formatAmount,
  formatBic,
  formatDateLong,
  formatIban,
  formatStatusLabel,
  getApplicationStatusColor,
  getDocumentStatusColor,
  getLicenseStatusColor,
  getPaymentStatusColor,
} from "../common/utils";
import ConfirmPopup from "../common/confirmPopup";
import {AuthContext} from "../common/auth/AuthContext";
import {useNavigate} from "react-router";
import {downloadPdfOfficialDocument} from "../common/downloadPdf";
import {useQueryClient} from "@tanstack/react-query";


const SHOW_LICENSE_STATUSES = ["APPROVED"] as const;

const SHOW_DOCUMENT_STATUSES = [
  "DOCUMENTS_SUBMITTED",
  "VERIFICATION_PENDING",
  "AWAITING_PAYMENT",
  "PAYMENT_RECEIVED",
  "SUBMITTED",
  "IN_BALLOT",
  "SELECTED",
  "NOT_SELECTED",
  "UNDER_REVIEW",
  "APPROVED",
  "REJECTED",
  "EXPIRED",
  "CANCELLED",
] as const;

const SHOW_PAYMENT_STATUSES = [
  "PAYMENT_RECEIVED",
  "SUBMITTED",
  "IN_BALLOT",
  "SELECTED",
  "NOT_SELECTED",
  "UNDER_REVIEW",
  "APPROVED",
  "REJECTED",
  "EXPIRED",
  "CANCELLED",
] as const;

function useGetLicenseData(
    userId?: string,
    applicationId?: number
): LicenseResource | undefined {
  const auth = useContext(AuthContext);

  const {data: listResponse} = useListLicenses(
      {user_id: userId ?? ""},
      {
        axios: {
          headers: {
            Authorization: `Bearer ${auth?.accessToken}`,
          },
        },
        query: {
          enabled: Boolean(userId),
        },
      }
  );

  const licenses = listResponse?.data ?? [];
  const matching = licenses.find(
      (lic) => lic.application_id === applicationId
  );

  const {data: licenseResponse} = useGetLicense(
      matching?.id as number,
      {
        axios: {
          headers: {
            Authorization: `Bearer ${auth?.accessToken}`,
          },
        },
        query: {
          enabled: Boolean(matching?.id),
          retry: false,
        },
      }
  );

  return licenseResponse?.data;
}

function useGetDocumentData(
    applicationId?: number
): GetApplicationDocuments200 | undefined {
  const auth = useContext(AuthContext);

  const {data} = useGetApplicationDocuments(applicationId as number, {
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
    query: {
      enabled: Boolean(applicationId),
      retry: false,
    },
  });

  return data?.data;
}

function useGetPaymentData(
    applicationId?: number
): ApplicationPaymentResource | undefined {
  const auth = useContext(AuthContext);

  const {data} = useListPayments(applicationId as number, {
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
    query: {
      enabled: Boolean(applicationId),
      retry: false,
    },
  });

  return data?.data?.at(-1);
}

interface ApplicationDetailsProps {
  open: boolean;
  applicationData?: ApplicationResource;
  userData?: UserResource;
  onClose: () => void;
  onRenew: () => void;
}

export default function ApplicationDetails({
                                             open,
                                             applicationData,
                                             userData,
                                             onClose,
                                             onRenew,
                                           }: ApplicationDetailsProps) {
  const {t} = useTranslation();
  const navigate = useNavigate();
  const auth = useContext(AuthContext);
  const queryClient = useQueryClient();

  const [isPopupOpen, setIsPopupOpen] = useState(false);

  if (!open || !applicationData || !userData) {
    return null;
  }

  const licenseData = useGetLicenseData(
      userData.id,
      applicationData.id
  );
  const documentData = useGetDocumentData(applicationData.id);
  const paymentData = useGetPaymentData(applicationData.id);

  async function handleReleaseLicense() {
    if (!licenseData?.id) return;

    await deleteLicense(licenseData.id, {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    });

    await queryClient.invalidateQueries();
    setIsPopupOpen(false);
  }

  function handleCompleteApplication(
      status: string,
      applicationId: number
  ) {
    if (status === "DRAFT") {
      navigate(`/license-application-request/edit/${applicationId}`);
    }
    if (status === "DOCUMENTS_SUBMITTED") {
      navigate(`/payment/${applicationId}`);
    }
  }

  function timeToRenew(expiresAt?: string): boolean {
    if (!expiresAt) return false;

    const now = new Date();
    const expires = new Date(expiresAt);

    if (expires <= now) return true;

    const sixMonthsFromNow = new Date(now);
    sixMonthsFromNow.setMonth(now.getMonth() + 6);

    return expires <= sixMonthsFromNow;
  }

  const onDownload = async () => {
    await downloadPdfOfficialDocument({
      downloadFileName: t(
          "applicationDetails.licenseCertificate.fileName",
          {
            firstName: userData.firstName,
            lastName: userData.lastName,
          }
      ),
      title: t("applicationDetails.licenseCertificate.title", {
        licenseType: applicationData.license_type,
      }),
      text: "...", // unverändert aus deinem Original
      t,
    });
  };

  return (
      <div
          className="fixed inset-0 z-50 flex flex-col bg-black/50 overflow-auto"
          aria-modal="true"
          role="dialog"
      >
        {/* Spacer to push content below the header */}
        <div className="mt-20"></div>
        {/* Overlay to close the modal when clicking outside */}
        <div className="absolute inset-0" onClick={onClose}/>

        <div className="container mx-auto px-4 md:px-6">
          <div
              className="relative min-h-[calc(50vh-8rem)] bg-white flex items-center justify-center rounded-xl overflow-y-auto">
            {/* Close Button */}
            <button
                onClick={onClose}
                className="absolute flex top-4 left-4 items-center justify-center cursor-pointer px-2 py-2 rounded-full
             text-mallorca-purple/75 hover:bg-mallorca-purple/10"
            >
              <CircleX size={36}/>
            </button>
            <div className="font-inter text-center">
              <div className="font-inter min-w-80vb max-w-200vb">
                <FormHeader
                    heading={t("applicationDetails.index.headline")}
                    subHeading={t("")}
                    className="mt-16"
                />

                {/* Application fields */}
                <div className="bg-gray-50 rounded-lg p-6 mt-12 shadow space-y-2.5 text-gray-700">
                  <div className="flex justify-between min-w-lg font-semibold">
                  <span>
                    {t("applicationDetails.index.applicationIdLabel") + ": "}
                  </span>
                    <span>{applicationData?.id}</span>
                  </div>

                  <hr className="my-2 border-gray-300"/>

                  <div className="flex justify-between min-w-lg">
                  <span>
                    {t("applicationDetails.index.licenseTypeLabel") + ": "}
                  </span>
                    <span>{applicationData?.license_type}</span>
                  </div>

                  <div className="flex justify-between min-w-lg">
                  <span>
                    {t("applicationDetails.index.cadastralIdLabel") + ": "}
                  </span>
                    <span>{applicationData?.cadastral_reference}</span>
                  </div>

                  <div className="flex justify-between min-w-lg">
                  <span>
                    {t("applicationDetails.index.statusLabel") + ": "}
                  </span>
                    <span
                        className={`text-${getApplicationStatusColor(applicationData?.application_status)}-600 font-semibold`}
                    >
                    {t(formatStatusLabel("applicationDetails.applicationStatus.", applicationData?.application_status))}
                  </span>
                  </div>

                  {applicationData?.remarks && (
                      <div className="flex justify-between min-w-lg">
                    <span>
                      {t("applicationDetails.index.remarksLabel") + ": "}
                    </span>
                        <span>{applicationData?.remarks}</span>
                      </div>
                  )}

                  <div className="flex justify-between min-w-lg">
                  <span>
                    {t("applicationDetails.index.appliedOnLabel") + ": "}
                  </span>
                    <span>{formatDateLong(applicationData?.applied_at, t)}</span>
                  </div>

                  <div className="flex justify-between min-w-lg">
                  <span>
                    {t("applicationDetails.index.lastUpdatedLabel") + ": "}
                  </span>
                    <span>{formatDateLong(applicationData?.changed_at, t)}</span>
                  </div>
                </div>

                {/* User fields */}
                <div className="text-lg text-left mt-6 text-mallorca-purple/70 font-semibold">
                  {t("applicationDetails.index.user.label")}
                </div>
                <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">
                  <div className="flex justify-between min-w-lg">
                  <span>
                    {t("applicationDetails.index.user.nameLabel") + ": "}
                  </span>
                    <span>{userData?.firstName + " " + userData?.lastName}</span>
                  </div>

                  <div className="flex justify-between min-w-lg">
                  <span>
                    {t("applicationDetails.index.user.emailLabel") + ": "}
                  </span>
                    <span>{userData?.email}</span>
                  </div>
                </div>

                {/* License fields */}
                {SHOW_LICENSE_STATUSES.includes(
                    applicationData?.application_status as any
                ) && (
                    <>
                      <div className="text-lg text-left mt-6 text-mallorca-purple/70 font-semibold">
                        {t("applicationDetails.index.license.label")}
                      </div>

                      <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">
                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.license.idLabel") + ": "}
                      </span>
                          <span>{licenseData?.id}</span>
                        </div>

                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.license.statusLabel") +
                            ": "}
                      </span>
                          <span
                              className={`text-${getLicenseStatusColor(licenseData?.license_status)}-600 font-semibold`}
                          >{t(formatStatusLabel("applicationDetails.licenseStatus.", licenseData?.license_status))}
                      </span>
                        </div>

                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.license.issuedOnLabel") +
                            ": "}
                      </span>
                          <span>{formatDateLong(licenseData?.issued_at, t)}</span>
                        </div>

                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.license.expiresOnLabel") +
                            ": "}
                      </span>
                          <span>{formatDateLong(licenseData?.expires_at, t)}</span>
                        </div>
                      </div>
                    </>
                )}

                {/* Document fields */}
                {SHOW_DOCUMENT_STATUSES.includes(
                    applicationData?.application_status as any
                ) && (
                    <>
                      <div className="text-lg text-left mt-6 text-mallorca-purple/70 font-semibold">
                        {t("applicationDetails.index.documents.label")}
                      </div>

                      <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">
                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.documents.statusLabel") +
                            ": "}
                      </span>
                          <span
                              className={`text-${getDocumentStatusColor(documentData?.status)}-600 font-semibold`}
                          >{t(formatStatusLabel("applicationDetails.documentStatus.", documentData?.status))}</span>
                        </div>

                        {documentData?.rejection_reason && (
                            <div className="flex justify-between min-w-lg">
                        <span>
                          {t(
                              "applicationDetails.index.documents.rejectionReasonLabel"
                          ) + ": "}
                        </span>
                              <span>{documentData?.rejection_reason}</span>
                            </div>
                        )}
                      </div>
                    </>
                )}

                {/* Payment fields */}
                {SHOW_PAYMENT_STATUSES.includes(
                    applicationData?.application_status as any
                ) && (
                    <>
                      <div className="text-lg text-left mt-6 text-mallorca-purple/70 font-semibold">
                        {t("applicationDetails.index.payment.label")}
                      </div>

                      <div className="bg-gray-50 rounded-lg p-6 mt-2 shadow space-y-2.5 text-gray-700">
                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.payment.idLabel") + ": "}
                      </span>
                          <span>{paymentData?.id}</span>
                        </div>

                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.payment.amountLabel") +
                            ": "}
                      </span>
                          <span>{formatAmount(paymentData?.amount, t) + "*"}</span>
                        </div>

                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.payment.nameLabel") + ": "}
                      </span>
                          <span>{paymentData?.name}</span>
                        </div>

                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.payment.ibanLabel") + ": "}
                      </span>
                          <span>{formatIban(paymentData?.iban)}</span>
                        </div>

                        {paymentData?.bic && (
                            <div className="flex justify-between min-w-lg">
                        <span>
                          {t("applicationDetails.index.payment.bicLabel") +
                              ": "}
                        </span>
                              <span>{formatBic(paymentData?.bic)}</span>
                            </div>
                        )}

                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.payment.dateLabel") + ": "}
                      </span>
                          <span>
                        {formatDateLong(paymentData?.payment_date, t)}
                      </span>
                        </div>

                        <div className="flex justify-between min-w-lg">
                      <span>
                        {t("applicationDetails.index.payment.statusLabel") +
                            ": "}
                      </span>
                          <span
                              className={`text-${getPaymentStatusColor(paymentData?.payment_status)}-600 font-semibold`}
                          >{t(formatStatusLabel("applicationDetails.paymentStatus.", paymentData?.payment_status))}</span>
                        </div>
                      </div>
                    </>
                )}

                <div className="px-6 mt-1">
                  {SHOW_PAYMENT_STATUSES.includes(
                      applicationData?.application_status as any
                  ) && (
                      <span className="text-xs font-light italic">
                    {"*" + t("applicationDetails.index.payment.taxLabel")}
                  </span>
                  )}
                </div>

                <div className="my-8 flex flex-line items-center justify-center gap-4">
                  <>
                    {/* Renew License Button */}
                    {["ACTIVE", "EXPIRED"].includes(
                        licenseData?.license_status ?? ""
                    ) && timeToRenew(licenseData?.expires_at) && (
                        <button
                            type="submit"
                            onClick={onRenew}
                            className={"bg-mallorca-purple text-white px-10 py-2 rounded-md min-w-48 max-w-96 font-medium text-lg hover:bg-mallorca-purple/90"}
                        >
                          {t("applicationDetails.buttons.renewLicenseLabel")}
                        </button>
                    )}

                    {/* Release License Button */}
                    {["ACTIVE"].includes(
                        licenseData?.license_status ?? ""
                    ) && (

                        <button
                            type="submit"
                            onClick={() => setIsPopupOpen(true)}
                            className={"bg-red-500 text-white  px-10 py-2 rounded-md min-w-48 max-w-96 font-medium text-lg hover:bg-red-700"}
                        >
                          {t("applicationDetails.buttons.releaseLicenseLabel")}
                        </button>
                    )}

                    {/* Download License Button */}
                    {["ACTIVE"].includes(
                        licenseData?.license_status ?? ""
                    ) && (
                        <button
                            type="submit"
                            onClick={onDownload}
                            className={"bg-mallorca-purple text-white  px-10 py-2 rounded-md min-w-48 max-w-96 font-medium text-lg hover:bg-mallorca-purple/90"}
                        >
                          {t("applicationDetails.buttons.downloadLabel")}
                        </button>
                    )}
                  </>

                  {/* Edit Application Button */}
                  {["DRAFT", "DOCUMENTS_SUBMITTED"].includes(
                      applicationData?.application_status
                  ) && (
                      <button
                          onClick={() =>
                              handleCompleteApplication(
                                  applicationData?.application_status,
                                  applicationData?.id
                              )
                          }
                          className="bg-mallorca-purple text-white px-10 py-2 rounded-md font-medium text-lg mt-8 w-max"
                      >
                        {applicationData?.application_status === "DRAFT"
                            ? "Edit"
                            : "Complete Payment"}
                      </button>
                  )}
                </div>

                {isPopupOpen && (
                    <ConfirmPopup
                        open={isPopupOpen}
                        onCancel={() => setIsPopupOpen(false)}
                        onConfirm={handleReleaseLicense}
                        headingLabel={t(
                            "applicationDetails.confirmPopup.headingLabel"
                        )}
                        subHeadingLabel={t(
                            "applicationDetails.confirmPopup.subHeadingLabel"
                        )}
                        quoteTitle={t("applicationDetails.confirmPopup.quoteTitle")}
                        quoteText={[
                          t("applicationDetails.confirmPopup.quoteText1"),
                          t("applicationDetails.confirmPopup.quoteText2"),
                        ]}
                        cancelLabel={t(
                            "applicationDetails.confirmPopup.cancelButtonLabel"
                        )}
                        confirmLabel={t(
                            "applicationDetails.confirmPopup.confirmButtonLabel"
                        )}
                    />
                )}
              </div>
            </div>
          </div>
          {/* Spacer to push content above the footer */}
          <div className="mb-12"></div>
        </div>
      </div>
  );
}
