import React, { useContext } from "react";
import { useNavigate, useParams } from "react-router";
import { useTranslation } from "react-i18next";
import { FormHeader } from "../common/headingTitle";
import { useGetLicense } from "app/services/licenses/licenses";
import { AuthContext } from "app/common/AuthContext";

export default function IssuedLicensePage() {
  const navigate = useNavigate();
  const { t } = useTranslation("translation", { keyPrefix: "issued-license" });
  const auth = useContext(AuthContext);
  const params = useParams();
  const licenseId = params.id ? Number(params.id) : -1;

  const { data: licenseData } = useGetLicense(licenseId, {
    axios: {
      headers: {
        Authorization: `${auth?.tokenType} ${auth?.accessToken}`,
      },
    },
    query: {
      select: (response) => response.data,
      enabled: typeof licenseId === "number" && !isNaN(licenseId),
    },
  });

  const formatDateOnly = (iso?: string | null) => {
    if (!iso) return "";
    const d = new Date(iso);
    if (isNaN(d.getTime())) return "";
    return d.toLocaleDateString();
  };

  return (
    <div className="container mx-auto px-4 md:px-6 min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center font-inter">
      <div className="w-full max-w-xl">
        {/* Page Header */}
        <FormHeader
          className="mb-6"
          heading={t("title")}
          subHeading={t("subtitle")}
        />

        {/* License Details */}
        <div className="bg-gray-50 rounded-lg p-6 mb-6">
          <h3 className="font-semibold mb-5 text-xl">{t("licenseDetails")}</h3>

          <div className="grid grid-cols-2 gap-y-5">
            <span className="text-gray-500">{t("licenseId")}</span>
            <span className="text-center">{licenseData?.id}</span>

            <span className="text-gray-500">{t("licenseType")}</span>
            <span className="text-center">{licenseData?.license_type}</span>

            <span className="text-gray-500">{t("status")}</span>
            <div className="flex justify-center">
              <span className="text-center px-3 py-1 rounded-full bg-green-100 text-green-700">
                {licenseData?.license_status}
              </span>
            </div>

            <span className="text-gray-500">{t("issuedOn")}</span>
            <span className="text-center">
              {formatDateOnly(licenseData?.issued_at)}
            </span>

            <span className="text-gray-500">{t("validity")}</span>
            <span className="text-center">
              {formatDateOnly(licenseData?.issued_at)}
              &nbsp;&nbsp;–&nbsp;&nbsp;
              {formatDateOnly(licenseData?.expires_at)}
            </span>
          </div>
        </div>

        {/* Property Details */}
        <div className="bg-gray-50 rounded-lg p-6 mb-8">
          <h3 className="font-semibold mb-3 text-xl">{t("propertyDetails")}</h3>

          <div className="grid grid-cols-2 gap-y-3">
            <span className="text-gray-500">{t("cadastralNumber")}</span>
            <span className="text-center">
              {licenseData?.cadastral_reference}
            </span>
          </div>
        </div>

        {/* GDPR Notice */}
        <p className="text-xs text-gray-500 text-center mb-6">
          {t("gdprNotice")}
        </p>

        {/* Back Button */}
        <button
          onClick={() => navigate("/")}
          className="bg-mallorca-purple text-white px-6 py-2 rounded-lg shadow hover:bg-[#4B1A58] mx-auto block"
        >
          {t("back")}
        </button>
      </div>
    </div>
  );
}
