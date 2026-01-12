import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Download } from "lucide-react";
import { useParams } from "react-router";

import Row from "app/common/ballot-details-ui/Row";
import Pill from "app/common/ballot-details-ui/Pill";
import ValuePill from "app/common/ballot-details-ui/ValuePill";

import { getBallotPeriodById, BallotPeriod } from "../api/ballotPeriods.api";
import {
  getBallotYear,
  getApplicationPeriod,
  getDrawingDate,
  getStatus,
} from "./ballot.utils";

type RouteParams = {
  id?: string;
};

const BallotDetails: React.FC = () => {
  const { t } = useTranslation(undefined, { keyPrefix: "ballotDetails" });
  const { id } = useParams<RouteParams>();

  const ballotPeriodId = id ? Number(id) : null;

  const [ballot, setBallot] = useState<BallotPeriod | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // ❌ Missing or invalid ID
    if (!ballotPeriodId || Number.isNaN(ballotPeriodId)) {
      setError(t("errors.invalidId"));
      setLoading(false);
      return;
    }

    getBallotPeriodById(ballotPeriodId)
      .then((data) => {
        setBallot(data);
        setError(null);
      })
      .catch((err) => {
        if (err?.response?.status === 404) {
          setError(t("errors.notFound"));
        } else {
          setError(t("errors.generic"));
        }
      })
      .finally(() => setLoading(false));
  }, [ballotPeriodId, t]);

  // 🔄 Loading state
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center text-gray-600">
        {t("loading")}
      </div>
    );
  }

  // ❌ Error state
  if (error) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center text-center">
        <h2 className="text-xl font-semibold mb-2">
          {t("errors.title")}
        </h2>
        <p className="text-gray-600">{error}</p>
      </div>
    );
  }

  if (!ballot) return null;

  const status = getStatus(ballot.end_date);

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
                {getBallotYear(ballot.start_date)}-MallorcaRentalLicenses
              </span>
            </Row>

            <Row label={t("overview.licenseType")}>
              <div className="flex gap-2">
                <Pill>ETV</Pill>
                <Pill>ETVPL</Pill>
                <Pill>ETV60</Pill>
              </div>
            </Row>

            <Row label={t("overview.status")}>
              <span className="bg-purple-300 px-4 py-1 rounded-md">
                {t(`status.${status}`)}
              </span>
            </Row>
          </div>

          <div className="h-3" />

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("overview.ballotYear")}>
              <ValuePill>{getBallotYear(ballot.start_date)}</ValuePill>
            </Row>

            <Row label={t("overview.approvedCap")}>
              <span className="font-medium">20000</span>
            </Row>
          </div>

          {/* TIMELINE */}
          <h2 className="text-lg font-semibold mt-8 mb-3">
            {t("timeline.title")}
          </h2>

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("timeline.applicationPeriod")}>
              <ValuePill>
                {getApplicationPeriod(ballot.start_date, ballot.end_date)}
              </ValuePill>
            </Row>

            <Row label={t("timeline.drawingDate")}>
              <ValuePill>{getDrawingDate(ballot.end_date)}</ValuePill>
            </Row>
          </div>

          {/* DOCUMENTS */}
          <h2 className="text-lg font-semibold mt-8 mb-3">
            {t("documents.title")}
          </h2>

          <div className="bg-gray-50 rounded-md flex justify-between px-4 py-3">
            <span>ballot_results.pdf</span>
            <Download size={18} />
          </div>

        </div>
      </main>
    </div>
  );
};

export default BallotDetails;
