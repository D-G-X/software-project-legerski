import React from "react";
import { useTranslation } from "react-i18next";
import { Download } from "lucide-react";


const BallotDetails: React.FC = () => {
  const { t } = useTranslation("ballotDetails");

  return (
    <div className="min-h-screen bg-white font-inter flex flex-col">


      {/* PAGE CONTENT */}
      <main className="flex-1">
        <div className="max-w-5xl mx-auto px-6 py-6">


          {/* BALLOT OVERVIEW */}
          <h2 className="text-lg font-semibold mb-3">
            {t("overview.title")}
          </h2>

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("overview.ballotId")}>
              <span className="font-medium">
                2025-MallorcaRentalLicenses
              </span>
            </Row>

            <Row label={t("overview.licenseType")}>
              <div className="flex gap-2">
                <Pill>{t("license.etv")}</Pill>
                <Pill>{t("license.etvpl")}</Pill>
                <Pill>{t("license.etv60")}</Pill>
              </div>
            </Row>

            <Row label={t("overview.status")}>
              <span className="bg-purple-300 text-black font-medium px-4 py-1 rounded-md border border-purple-500">
                {t("status.completed")}
              </span>
            </Row>
          </div>

          <div className="h-3" />

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("overview.ballotYear")}>
              <ValuePill>2025</ValuePill>
            </Row>

            <Row label={t("overview.approvedCap")}>
              <span className="font-medium">20000</span>
            </Row>
          </div>

          {/* BALLOT TIMELINE */}
          <h2 className="text-lg font-semibold mt-8 mb-3">
            {t("timeline.title")}
          </h2>

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("timeline.applicationPeriod")}>
              <ValuePill>01/01/25 - 30/06/25</ValuePill>
            </Row>

            <Row label={t("timeline.drawingDate")}>
              <ValuePill>31.07.2025</ValuePill>
            </Row>
          </div>

          {/* BALLOT DOCUMENTS */}
          <h2 className="text-lg font-semibold mt-8 mb-3">
            {t("documents.title")}
          </h2>

          <div className="bg-gray-50 rounded-md">
            <div className="flex items-center justify-between px-4 py-3">
              <span className="text-gray-700">
                ballot_results.pdf
              </span>
              <button
                aria-label={t("documents.download")}
                className="text-gray-600 hover:text-black"
              >
                <Download size={18} />
              </button>
            </div>
          </div>

          {/* ACTION BUTTONS */}
          <div className="border-t bg-white mt-8">
            <div className="max-w-5xl mx-auto px-1 py-4 flex gap-4">
              <button className="w-48 bg-mallorca-purple text-white px-6 py-2 rounded-lg font-medium text-center">
                {t("actions.viewApplications")}
              </button>

              <button className="w-48 border border-gray-400 px-6 py-2 rounded-lg font-medium text-center">
                {t("actions.cancel")}
              </button>
            </div>
          </div>

        </div>
      </main>

    </div>
  );
};

export default BallotDetails;

/* ---------- Reusable UI Pieces ---------- */

const Row: React.FC<{ label: string; children: React.ReactNode }> = ({
  label,
  children,
}) => (
  <div className="flex items-center justify-between px-4 py-3">
    <span className="text-gray-600">{label}</span>
    <div className="flex items-center gap-2">{children}</div>
  </div>
);

const Pill: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <span className="border border-gray-300 rounded-full px-3 py-1 text-sm bg-white">
    {children}
  </span>
);

const ValuePill: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <span className="border border-gray-300 rounded-md px-3 py-1 bg-white font-medium">
    {children}
  </span>
);
