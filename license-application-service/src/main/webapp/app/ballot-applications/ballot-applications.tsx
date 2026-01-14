import React, { useContext, useEffect } from "react";

import { AuthContext } from "app/common/AuthContext";
import { useGlobalLoader } from "app/common/GlobalLoader";
import { useGetBallotPeriodEntries } from "app/services/ballot-periods/ballot-periods";
import { useNavigate, useParams } from "react-router";
import { FormHeader } from "app/common/headingTitle";
import { useTranslation } from "react-i18next";

function BallotApplications() {
  const params = useParams<{ id?: string }>();
  const paramsId = params.id;
  const parsed = paramsId ? parseInt(paramsId, 10) : NaN;
  const periodParam: number = Number.isFinite(parsed) ? parsed : -1;
  const periodEnabled = Number.isFinite(parsed);

  const { t } = useTranslation();
  const auth = useContext(AuthContext);
  const { show, hide } = useGlobalLoader();
  const navigate = useNavigate();

  const { data: applications, isFetching } = useGetBallotPeriodEntries(
    periodParam,
    {
      axios: {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      },
      query: {
        enabled: periodEnabled,
        select: (res) => res.data,
      },
    }
  );

  console.log(applications);

  useEffect(() => {
    isFetching ? show() : hide();
  }, [isFetching, show, hide]);

  // const startDate = new Date(ballot?.start_date as string);
  // const endDate = new Date(ballot?.end_date as string);

  // const status = getBallotStatus(startDate, endDate, new Date(), t);
  // const statusClass = getStatusClass(status);
  // const drawDate = getDrawDate(endDate, 7);
  return (
    <div className="min-h-[calc(100vh-8rem)] bg-white font-inter flex items-center justify-center">
      <main className="flex-1">
        <div className="max-w-5xl mx-auto px-6 py-6">
          <FormHeader
            className="mb-6"
            heading={t("ballotDetails.overview.title")}
          />

          {/* implement table for view applications */}

          {/* ACTION BUTTONS */}
          <div className=" bg-white mt-8">
            <div className="max-w-5xl mx-auto px-1 py-4 flex gap-4">
              <button
                onClick={() => navigate("/ballot-entries")}
                className="w-48 bg-mallorca-purple text-white px-6 py-2 rounded-lg font-medium text-center"
              >
                {t("ballotDetails.actions.viewApplications")}
              </button>

              <button
                onClick={() => navigate("/")}
                className="w-48 border border-gray-400 px-6 py-2 rounded-lg font-medium text-center"
              >
                {t("ballotDetails.actions.cancel")}
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default BallotApplications;
