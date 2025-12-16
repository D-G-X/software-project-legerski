import React from "react";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import { FormHeader } from "../common/headingTitle";

export default function IssuedLicensePage() {
  const navigate = useNavigate();
  const { t } = useTranslation("issued_license");

  // Mock data – later this will come from backend
  const licenseData = {
    licenseId: "0124219871241298",
    licenseType: "ETV",
    status: "Active",
    issuedOn: "25.12.2025",
    validFrom: "01.01.2026",
    validTo: "31.12.2030",
    cadastralNumber: "1234567CD8113S0001AB",
  };

  return (
    <div className="container mx-auto px-4 md:px-6 min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center font-inter">
      <div className="w-full max-w-xl">

        {/* Page Header */}
        <FormHeader
          heading={t("title")}
          subHeading={t("subtitle")}
        />

        {/* License Details */}
        <div className="bg-gray-50 rounded-lg p-6 mb-6">
          <h3 className="font-semibold mb-4 text-lg">
            {t("licenseDetails")}
          </h3>

          <div className="grid grid-cols-2 gap-y-3 text-sm">
            <span className="text-gray-500">{t("licenseId")}</span>
            <span>{licenseData.licenseId}</span>

            <span className="text-gray-500">{t("licenseType")}</span>
            <span>{licenseData.licenseType}</span>

            <span className="text-gray-500">{t("status")}</span>
            <span className="inline-block px-3 py-1 rounded-full text-xs bg-green-100 text-green-700">
              {licenseData.status}
            </span>

            <span className="text-gray-500">{t("issuedOn")}</span>
            <span>{licenseData.issuedOn}</span>

            <span className="text-gray-500">{t("validity")}</span>
            <span>
              {licenseData.validFrom} – {licenseData.validTo}
            </span>
          </div>
        </div>

        {/* Property Details */}
        <div className="bg-gray-50 rounded-lg p-6 mb-8">
          <h3 className="font-semibold mb-3 text-lg">
            {t("propertyDetails")}
          </h3>

          <div className="grid grid-cols-2 gap-y-3 text-sm">
            <span className="text-gray-500">
              {t("cadastralNumber")}
            </span>
            <span>{licenseData.cadastralNumber}</span>
          </div>
        </div>

        {/* GDPR Notice */}
        <p className="text-xs text-gray-500 text-center mb-6">
          {t("gdprNotice")}
        </p>

        {/* Back Button */}
        <button
          onClick={() => navigate(-1)}
          className="bg-mallorca-purple text-white px-6 py-2 rounded-lg shadow hover:bg-[#4B1A58] mx-auto block"
        >
          {t("back")}
        </button>
      </div>
    </div>
  );
}