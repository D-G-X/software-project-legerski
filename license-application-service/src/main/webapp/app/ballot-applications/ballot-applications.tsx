import React, { useContext, useEffect, useState } from "react";

import { AuthContext } from "app/common/AuthContext";
import { useGlobalLoader } from "app/common/GlobalLoader";
import { useGetBallotPeriodEntries } from "app/services/ballot-periods/ballot-periods";
import { useNavigate, useParams } from "react-router";
import { FormHeader } from "app/common/headingTitle";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import Pagination from "../common/Pagination";
import { useGetBallotPeriodDetails } from "app/services/ballot-periods/ballot-periods";
import { formatDateShort } from "app/common/format";
import { getApplicationStatusColor } from "../common/format";
import { getBallotStatus, getStatusClass } from "app/common/ballotUtils";
import { ChevronLeft, FileText } from "lucide-react";
import AdminApplicationDetails from "app/admin-dashboard/admin-applicationDetails";

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

  useDocumentTitle(t("ballotDetails.overview.title"));

  const { data: ballotDetails, isFetching: isBallotFetching } =
    useGetBallotPeriodDetails(periodParam, {
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

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const { data: applicationsRaw, isFetching } = useGetBallotPeriodEntries(
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

  const displayApplications = applicationsRaw ?? [];

  useEffect(() => {
    isFetching || isBallotFetching ? show() : hide();
  }, [isFetching, isBallotFetching, show, hide]);

  const sorted = [...displayApplications].sort(
    (a, b) => Number(a.id) - Number(b.id)
  );
  const totalPage = Math.max(1, Math.ceil(sorted.length / itemsPerPage));

  const currentData = sorted.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  // State for Application Details Modal
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [selectedEntry, setSelectedEntry] = useState<any>(null);

  const closeDetails = () => {
    setIsDetailsOpen(false);
    setSelectedEntry(null);
  };

  const ballotStart = ballotDetails?.start_date
    ? new Date(ballotDetails.start_date)
    : undefined;
  const ballotEnd = ballotDetails?.end_date
    ? new Date(ballotDetails.end_date)
    : undefined;
  const ballotStatus =
    ballotStart && ballotEnd
      ? getBallotStatus(ballotStart, ballotEnd, new Date(), t)
      : t("ballotStatus.upcoming");
  const ballotStatusClass = getStatusClass(ballotStatus);

  return (
    <div className="min-h-[calc(100vh-8rem)] w-[90%] mx-[5%] border border-transparent">
      <div className="relative bg-white w-full">
        <div className={"w-full my-8 flex justify-between items-end"}>
          <FormHeader heading={t("ballotApplications.title")} />

          <div>
            <button
              onClick={() => navigate(`/ballot-details/${periodParam}`)}
              className="cursor-pointer bg-mallorca-purple pl-3 pr-4 py-2 border-2 border-mallorca-purple text-white rounded-xl hover:bg-mallorca-purple/75 flex gap-5"
            >
              <ChevronLeft />
              Back
            </button>
          </div>
        </div>

        <div className="bg-gray-50 rounded-md p-4 pt-2 mb-4">
          <div className="flex flex-wrap gap-6 items-center justify-between">
            <div className="text-center w-[15%]">
              <div className="text-black/60">
                {t("ballotDetails.overview.ballotId")}
              </div>
              <div className="font-medium">
                {ballotDetails?.ballot_period_id ?? periodParam}
              </div>
            </div>

            <div className="text-center w-[15%]">
              <div className="text-black/60">
                {t("ballotDetails.overview.applicationPeriod")}
              </div>
              <div className="font-medium">
                {formatDateShort(ballotDetails?.start_date, t)} —{" "}
                {formatDateShort(ballotDetails?.end_date, t)}
              </div>
            </div>

            <div className="text-center w-[15%]">
              <div className="text-black/60">
                {t("ballot-dashboard.table.totalApplication")}
              </div>
              <div className="font-medium">{displayApplications.length}</div>
            </div>

            <div className="text-center w-[15%]">
              <div className="text-black/60">
                {t("ballotDetails.overview.status")}
              </div>
              <div>
                <div
                  className={`font-medium px-3 py-1 rounded-md w-full ${ballotStatusClass}`}
                >
                  {ballotStatus}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className={"overflow-x-auto"}>
          <table className="mt-8 text-lg dashboard-table text-mallorca-purple w-full">
            <thead>
              <tr>
                <th className="w-1/6 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                  {t("ballotApplications.table.applicationId")}
                </th>
                <th className="w-1/6 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                  {t("ballotApplications.table.licenseType")}
                </th>
                <th className="w-1/6 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                  {t("ballotApplications.table.cadastralNumber")}
                </th>
                <th className="w-1/6 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                  {t("ballotApplications.table.appliedAt")}
                </th>
                <th className="w-1/6 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                  {t("ballotApplications.table.status")}
                </th>
                <th className="w-1/6 border-b  border-black/10 p-2.5 text-black/30 font-normal text-center">
                  {t("ballotApplications.table.action")}
                </th>
              </tr>
            </thead>

            <tbody>
              {currentData.length > 0
                ? currentData.map((item) => {
                    const appliedAt = item.applied_at
                      ? new Date(item.applied_at)
                      : undefined;

                    return (
                      <tr className={"text-sm"} key={item.id}>
                        <td className="w-1/6 border-b border-black/10 p-2.5 text-center">
                          {item.id}
                        </td>

                        <td className="w-1/6 border-b border-black/10 p-2.5 text-center">
                          {item.license_type}
                        </td>

                        <td className="w-1/6 border-b border-black/10 p-2.5 text-center">
                          {item.cadastral_reference ?? "-"}
                        </td>

                        <td className="w-1/6 border-b border-black/10 p-2.5 text-center">
                          {appliedAt ? appliedAt.toLocaleDateString() : "-"}
                        </td>

                        <td className="w-1/6 border-b border-black/10 p-2.5 text-center">
                          <span
                            className={`inline-flex items-center justify-center text-center p-1 px-4 rounded-md font-semibold min-w-[140px] truncate w-full text-${getApplicationStatusColor(
                              item.application_status
                            )}-800 bg-${getApplicationStatusColor(
                              item.application_status
                            )}-200`}
                          >
                            {item.application_status}
                          </span>
                        </td>

                        <td className="w-1/6 border-b border-black/10 p-2.5 px-15 text-center">
                          <button
                            className="text-mallorca-purple underline cursor-pointer hover:bg-mallorca-purple/25 p-1 rounded"
                            onClick={() => {
                              setSelectedEntry(item);
                              setIsDetailsOpen(true);
                            }}
                          >
                            <FileText />
                          </button>
                        </td>
                      </tr>
                    );
                  })
                : !isFetching && (
                    <tr>
                      <td
                        colSpan={6}
                        className="py-8 text-center text-gray-600 text-lg"
                      >
                        {t("ballotApplications.noEligible")}
                      </td>
                    </tr>
                  )}
            </tbody>
          </table>

          <Pagination
            currentPage={currentPage}
            totalPage={totalPage}
            onPageChange={setCurrentPage}
            start={(currentPage - 1) * itemsPerPage + 1}
            end={currentPage * itemsPerPage}
            numberOfItems={displayApplications.length}
          />
        </div>
      </div>
      <div>
        {/* Application Details Modal */}
        {isDetailsOpen && (
          <AdminApplicationDetails
            open={isDetailsOpen}
            userId={selectedEntry?.user_id}
            applicationData={selectedEntry}
            onClose={closeDetails}
          />
        )}
      </div>
    </div>
  );
}

export default BallotApplications;
