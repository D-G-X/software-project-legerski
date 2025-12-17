import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import Pagination from "../common/Pagination";

import { useNavigate } from "react-router";

export default function BallotDashboard() {
  const { t } = useTranslation();
  const [currentPage, setCurrentPage] = useState(1); // This state is to hold number of the current page in pagination table
  const itemsPerPage = 2; // This state is to hold number of items per page in pagination table
  useDocumentTitle(t("home.index.headline"));
  const navigate = useNavigate();

  function handleNewApplicationClick() {
    navigate(`/ballot-config`);
  }

  const ballots = [
    {
      id: "001",
      ballotName: "Spring Marathon 2026",
      status: "Completed",
      applicationPeriod: "01/10/25 - 31/10/25",
      drawingDate: "15/11/25",
      totalApplications: 75240,
    },
    {
      id: "002",
      ballotName: "Autumn Half-Marathon Lottery",
      status: "Upcoming",
      applicationPeriod: "15/03/26 - 30/04/26",
      drawingDate: "15/05/26",
      totalApplications: 0,
    },
    {
      id: "003",
      ballotName: "Charity 5K Run",
      status: "On Going",
      applicationPeriod: "01/01/25 - 20/01/25",
      drawingDate: "25/01/25",
      totalApplications: 12895,
    },
  ];

  const totalPage = Math.ceil(ballots.length / itemsPerPage);

  const currentData = ballots.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  return (
    <div className={"container mx-auto px-4 md:px-6 min-h-[calc(100vh-8rem)] "}>
      <div className="relative bg-white ">
        <div className={"w-full my-8 flex justify-between items-end"}>
          <h1 className={"inline-block text-xl w-6/12"}>
            {t("ballot-dashboard.headline")}
          </h1>
          <button
            onClick={handleNewApplicationClick}
            className={
              "bg-mallorca-purple text-white px-10 py-2 rounded-md font-medium text-lg mt-12 inline-block w-max text-sm"
            }
          >
            {t("ballot-dashboard.createNewBallot")}
          </button>
        </div>
        <div className={"overflow-x-auto"}>
          {currentData.length > 0 && (
            <table className="mt-8 text-lg dashboard-table text-mallorca-purple">
              <tr className={"text-sm"}>
                <th>{t("ballot-dashboard.table.ballotName")}</th>
                <th>{t("ballot-dashboard.table.status")}</th>
                <th>{t("ballot-dashboard.table.applicationPeriod")}</th>
                <th>{t("ballot-dashboard.table.drawingDate")}</th>
                <th>{t("ballot-dashboard.table.totalApplication")}</th>
                <th>{t("ballot-dashboard.table.action")}</th>
              </tr>
              {currentData.map((item) => (
                <tr className={"text-sm"} key={item.id}>
                  <td>{item.ballotName}</td>
                  <td>
                    <span
                      className={`inline-flex items-center justify-center text-center p-1 px-4 min-w-[14rem] rounded-md text-black border-2 border-black ${
                        item.status === "Completed"
                          ? "bg-purple-500"
                          : item.status === "On Going"
                          ? "bg-green-300"
                          : "bg-gray-200"
                      }
                                        }`}
                    >
                      {t(item.status)}
                    </span>
                  </td>
                  <td>{item.applicationPeriod}</td>
                  <td>{item.drawingDate}</td>
                  <td>{item.totalApplications}</td>
                  <td className={"text-blue-700 underline"}>
                    {item.status === "Completed"
                      ? "See Details"
                      : "Edit Configuration"}
                  </td>
                </tr>
              ))}
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
