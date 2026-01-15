import React, { useContext, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router";

import Row from "app/common/ballot-details-ui/Row";
import Pill from "app/common/ballot-details-ui/Pill";
import ValuePill from "app/common/ballot-details-ui/ValuePill";

import { AuthContext } from "app/common/AuthContext";
import { useGlobalLoader } from "app/common/GlobalLoader";
import {
  useGetBallotPeriodDetails,
  useRunLotteryForBallotPeriod,
  getGetBallotPeriodDetailsQueryKey,
} from "app/services/ballot-periods/ballot-periods";
import { useQueryClient } from "@tanstack/react-query";
import { FormHeader } from "app/common/headingTitle";
import { getBallotStatus, getStatusClass } from "app/common/ballotUtils";

const BallotDetails: React.FC = () => {
  const params = useParams<{ id?: string }>();
  const paramsId = params.id;
  const parsed = paramsId ? parseInt(paramsId, 10) : NaN;
  const periodParam: number = Number.isFinite(parsed) ? parsed : -1;
  const periodEnabled = Number.isFinite(parsed);

  const { t } = useTranslation();
  const auth = useContext(AuthContext);
  const { show, hide } = useGlobalLoader();
  const navigate = useNavigate();

  const {
    data: ballot,
    isFetching,
    isError,
    error,
  } = useGetBallotPeriodDetails(periodParam, {
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
    query: {
      enabled: periodEnabled,
      select: (res) => res.data,
    },
  });

  useEffect(() => {
    isFetching ? show() : hide();
  }, [isFetching, show, hide]);

  if (isError) {
    const errorMessage =
      (error as any)?.response?.data?.message ||
      (error as any)?.message ||
      t("ballotDetails.errors.unableToLoad");
    return (
      <div className="min-h-screen bg-white font-inter flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600 text-lg font-medium">
            {t("ballotDetails.errors.failedToLoad")}
          </p>
          <p className="text-gray-500 mt-2 text-sm">{errorMessage}</p>
        </div>
      </div>
    );
  }

  const startDate = new Date(ballot?.start_date as string);
  const endDate = new Date(ballot?.end_date as string);

  const status = getBallotStatus(startDate, endDate, new Date(), t);
  const statusClass = getStatusClass(status);
  // const drawDate = getDrawDate(endDate, 7);

  const queryClient = useQueryClient();

  const { mutate: runLottery, isPending: isLotteryRunning } =
    useRunLotteryForBallotPeriod({
      mutation: {
        onSuccess: async () => {
          try {
            await queryClient.invalidateQueries({
              queryKey: getGetBallotPeriodDetailsQueryKey(periodParam),
            });
          } catch (e) {
            // ignore
          }
          alert(
            t("ballotDetails.messages.lotterySuccess") ||
              "Lottery run successfully"
          );
        },
        onError: (err: any) => {
          console.error(err);
          alert(
            t("ballotDetails.errors.lotteryFailed") || "Running lottery failed"
          );
        },
      },
      axios: {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      },
    });

  const handleBallotLottery = () => {
    // Allow API call only when status is "Completed"
    if (status !== "Completed") {
      alert(
        t("ballotDetails.errors.cannotTriggerLottery") ||
          "Lottery can only be run after the ballot period has completed"
      );
      return;
    }

    if (!periodEnabled) return;

    runLottery({ periodId: periodParam, data: {} });
  };

  return (
    <div className="min-h-[calc(100vh-8rem)] bg-white font-inter flex items-center justify-center">
      <main className="flex-1">
        <div className="max-w-5xl mx-auto px-6 py-6">
          <FormHeader
            className="mb-6"
            heading={t("ballotDetails.overview.title")}
          />

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("ballotDetails.overview.ballotId")}>
              <span className="font-medium">{ballot?.ballot_period_id}</span>
            </Row>

            <Row label={t("ballotDetails.overview.licenseType")}>
              <div className="flex gap-2">
                <Pill>{t("ballotDetails.license.etv")}</Pill>
                <Pill>{t("ballotDetails.license.etvpl")}</Pill>
                <Pill>{t("ballotDetails.license.etv60")}</Pill>
              </div>
            </Row>

            <Row label={t("ballotDetails.overview.status")}>
              {/* implement status class */}
              <span
                className={`font-medium px-4 py-1 rounded-md ${statusClass}`}
              >
                {status}
              </span>
            </Row>
          </div>

          <div className="h-3" />

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("ballotDetails.overview.ballotYear")}>
              <ValuePill>{startDate.getFullYear()}</ValuePill>
            </Row>

            <Row label={t("ballotDetails.overview.approvedCap")}>
              <span className="font-medium">
                {t("ballotDetails.values.approvedCap")}
              </span>
            </Row>
          </div>

          {/* BALLOT TIMELINE */}
          <h2 className="text-lg font-semibold mt-8 mb-3">
            {t("ballotDetails.timeline.title")}
          </h2>

          <div className="bg-gray-50 rounded-md divide-y divide-gray-200">
            <Row label={t("ballotDetails.timeline.applicationPeriod")}>
              <ValuePill>
                {startDate.toLocaleDateString()} –{" "}
                {endDate.toLocaleDateString()}
              </ValuePill>
            </Row>

            {/* <Row label={t("ballotDetails.timeline.drawingDate")}>
              <ValuePill>{drawDate.toLocaleDateString()}</ValuePill>
            </Row> */}
          </div>

          {/* ACTION BUTTONS */}
          <div className="border-t bg-white mt-8">
            <div className="max-w-5xl mx-auto px-1 py-4 flex gap-4">
              <button
                onClick={() =>
                  navigate(`/ballot-applications/${ballot?.ballot_period_id}`)
                }
                className="w-48 border-2 border-mallorca-purple hover:cursor-pointer bg-mallorca-purple hover:bg-mallorca-purple/75 text-white px-6 py-2 rounded-lg font-medium text-center"
              >
                {t("ballotDetails.actions.viewApplications")}
              </button>

              <button
                onClick={() => navigate("/")}
                className="w-48 border-2 border-mallorca-purple hover:cursor-pointer hover:text-white hover:bg-mallorca-purple/75 px-6 py-2 rounded-lg font-medium text-center"
              >
                {t("ballotDetails.actions.cancel")}
              </button>

              {status === "Completed" && (
                <button
                  onClick={handleBallotLottery}
                  disabled={isLotteryRunning}
                  className={`w-48 border-2 px-6 py-2 rounded-lg hover:cursor-pointer font-medium text-center bg-mallorca-red/75 text-white border-mallorca-red hover:bg-mallorca-red ${
                    isLotteryRunning ? "opacity-50 cursor-not-allowed" : ""
                  }`}
                >
                  {t("ballotDetails.actions.triggerBallot")}
                </button>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default BallotDetails;
