import React, { useContext, useState } from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import Pagination from "../common/Pagination";

import { useNavigate } from "react-router";
import {
  getBallotStatus,
  getDrawDate,
  getStatusClass,
} from "app/common/ballotUtils";
import { FormHeader } from "app/common/headingTitle";
import { ExternalLink } from "lucide-react";
import { useListBallotPeriods } from "app/services/ballot-periods/ballot-periods";
import { AuthContext } from "app/common/AuthContext";

export default function BallotDashboard() {
  const { t } = useTranslation();
  const auth = useContext(AuthContext);
  const [currentPage, setCurrentPage] = useState(1); // This state is to hold number of the current page in pagination table
  const itemsPerPage = 10; // This state is to hold number of items per page in pagination table
  useDocumentTitle(t("home.index.headline"));
  const navigate = useNavigate();

  function handleNewApplicationClick() {
    navigate(`/ballot-config`);
  }

  const { data: ballotsRaw } = useListBallotPeriods({
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
    query: {
      select: (res) => res.data,
      enabled: true,
    },
  });

  const ballots = ballotsRaw ?? [];
  const sortedBallots = [...ballots].sort(
    (a, b) => Number(a.ballot_period_id) - Number(b.ballot_period_id)
  );

  const totalPage = Math.ceil(sortedBallots.length / itemsPerPage);

  const currentData = sortedBallots.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  return (
    // added border becoz the div is not taking proper content height if there is no border
    // weird af :)
    <div
      className={
        "min-h-[calc(100vh-8rem)] w-[90%] mx-[5%] border border-transparent"
      }
    >
      <div className="relative bg-white w-full">
        <div className={"w-full my-8 flex justify-between items-end"}>
          <FormHeader heading={t("ballot-dashboard.headline")} />
          <button
            onClick={handleNewApplicationClick}
            className={
              "bg-mallorca-purple text-white px-10 py-2 rounded-md font-medium mt-12 inline-block w-max"
            }
          >
            {t("ballot-dashboard.createNewBallot")}
          </button>
        </div>
        <div className={"overflow-x-auto"}>
          {currentData.length > 0 && (
            <table className="mt-8 text-lg dashboard-table text-mallorca-purple">
              <thead>
                <tr>
                  <th className="w-1/5 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                    {t("ballot-dashboard.table.ballotID")}
                  </th>
                  <th className="w-1/5 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                    {t("ballot-dashboard.table.status")}
                  </th>
                  <th className="w-1/5 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                    {t("ballot-dashboard.table.applicationPeriod")}
                  </th>
                  <th className="w-1/5 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                    {t("ballot-dashboard.table.drawingDate")}
                  </th>
                  <th className="w-1/5 border-b border-black/10 p-2.5 text-black/30 font-normal text-center">
                    {t("ballot-dashboard.table.totalApplication")}
                  </th>
                  <th className="w-1/5 border-b  border-black/10 p-2.5 text-black/30 font-normal text-center">
                    {t("ballot-dashboard.table.action")}
                  </th>
                </tr>
              </thead>

              <tbody>
                {currentData.map((item) => {
                  const now = new Date();

                  const startDate = item.start_date
                    ? new Date(item.start_date)
                    : undefined;
                  const endDate = item.end_date
                    ? new Date(item.end_date)
                    : undefined;

                  const rawStatus = getBallotStatus(
                    startDate!,
                    endDate!,
                    new Date(),
                    t
                  );

                  const statusClass = getStatusClass(rawStatus);

                  const localizedStatus =
                    startDate && endDate
                      ? getBallotStatus(startDate, endDate, now, t)
                      : t("status.upcoming");
                  const drawDate = endDate
                    ? getDrawDate(endDate, 7).toLocaleDateString()
                    : "";

                  return (
                    <tr className={"text-sm"} key={item.ballot_period_id}>
                      <td className="w-1/5 border-b border-black/10 p-2.5 text-center">
                        {item.ballot_period_id}
                      </td>

                      <td className="w-1/5 border-b border-black/10 p-2.5 text-center">
                        <span
                          className={`inline-flex items-center justify-center text-center p-1 px-4 min-w-56 rounded-md font-semibold ${statusClass}`}
                        >
                          {localizedStatus}
                        </span>
                      </td>

                      <td className="w-1/5 border-b border-black/10 p-2.5 text-center">
                        {startDate?.toLocaleDateString()} &nbsp;- &nbsp;
                        {endDate?.toLocaleDateString()}
                      </td>

                      <td className="w-1/5 border-b border-black/10 p-2.5 text-center">
                        {drawDate}
                      </td>

                      <td className="w-1/5 border-b border-black/10 p-2.5 text-center">
                        {item.totalApplications}
                      </td>

                      <td className="w-1/5 border-b border-black/10 p-2.5 px-15 text-center">
                        <button
                          className="text-mallorca-purple underline cursor-pointer hover:bg-mallorca-purple/25 p-1 rounded"
                          onClick={() =>
                            navigate(`/ballot-details/${item.ballot_period_id}`)
                          }
                        >
                          <ExternalLink />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}

          <Pagination
            currentPage={currentPage}
            totalPage={totalPage}
            onPageChange={setCurrentPage}
            start={(currentPage - 1) * itemsPerPage + 1}
            end={currentPage * itemsPerPage}
            numberOfItems={ballots.length}
          />
        </div>
      </div>
    </div>
  );
}
