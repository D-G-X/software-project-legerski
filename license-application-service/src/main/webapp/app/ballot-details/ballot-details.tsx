import React, { useContext, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Download } from "lucide-react";
import { useNavigate } from "react-router";

import Row from "app/common/ballot-details-ui/Row";
import Pill from "app/common/ballot-details-ui/Pill";
import ValuePill from "app/common/ballot-details-ui/ValuePill";

import { AuthContext } from "app/common/AuthContext";
import { useGlobalLoader } from "app/common/GlobalLoader";
import { useGetBallotPeriod } from "app/services/ballot-periods/ballot-periods";

const BallotDetails: React.FC = () => {
  const { t } = useTranslation(undefined, { keyPrefix: "ballotDetails" });
  const auth = useContext(AuthContext);
  const { show, hide } = useGlobalLoader();
  const navigate = useNavigate();


  const { data, isFetching, isError, error } = useGetBallotPeriod({
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
  });

  useEffect(() => {
    isFetching ? show() : hide();
  }, [isFetching, show, hide]);


  if (isError) {
    const errorMessage =
      (error as any)?.response?.data?.message ||
      (error as any)?.message ||
      "Unable to load ballot details.";

    return (
      <div className="min-h-screen bg-white font-inter flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600 text-lg font-medium">
            Failed to load ballot details
          </p>
          <p className="text-gray-500 mt-2 text-sm">
            {errorMessage}
          </p>
        </div>
      </div>
    );
  }


  if (!data?.data) {
    return (
      <div className="min-h-screen bg-white font-inter flex items-center justify-center">
        <div className="text-gray-500 text-lg">
          {t("loading")}
        </div>
      </div>
    );
  }


  const ballot = data.data;

  if (!ballot.start_date || !ballot.end_date) {
    throw new Error("Ballot start_date or end_date is missing");
  }
  const startDate = new Date(ballot.start_date);
  const endDate = new Date(ballot.end_date);

  if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
    throw new Error("Invalid ballot dates received from backend");
  }

  const year = startDate.getFullYear();

  const today = new Date();
  let status: "active" | "upcoming" | "completed" = "upcoming";

  if (today >= startDate && today <= endDate) status = "active";
  else if (today > endDate) status = "completed";

  return (
    <div className="min-h-screen bg-white font-inter flex flex-col">
      <main className="flex-1">
        <div className="max-w-5xl mx-auto px-6 py-6">

          {/* BALLOT OVERVIEW */}
          <h2 className="text-lg font-semibold mb-3">
            {t("overview.title")}
          </h2>

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("overview.ballotId")}>
              <span className="font-medium">
                {year}-MallorcaRentalLicenses
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
                {t(`status.${status}`)}
              </span>
            </Row>
          </div>

          <div className="h-3" />

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("overview.ballotYear")}>
              <ValuePill>{year}</ValuePill>
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
              <ValuePill>
                {startDate.toLocaleDateString()} – {endDate.toLocaleDateString()}
              </ValuePill>
            </Row>

            <Row label={t("timeline.drawingDate")}>
              <ValuePill>31.07.{year}</ValuePill>
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
              <button
                onClick={() => navigate("/")}
                className="w-48 bg-mallorca-purple text-white px-6 py-2 rounded-lg font-medium text-center"
              >
                {t("actions.viewApplications")}
              </button>

              <button
                onClick={() => navigate(-1)}
                className="w-48 border border-gray-400 px-6 py-2 rounded-lg font-medium text-center"
              >
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
