import React, {useContext, useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {formatDateShort, formatStatusLabel, getApplicationStatusColor, useDocumentTitle} from "../common/utils";
import Pagination from "../common/Pagination";
import {useNavigate} from "react-router";
import ApplicationDetails from "./applicationDetails";
import {useListApplications} from "app/services/applications/applications";
import {ApplicationResource, BallotPeriodResource} from "../../types";
import {AuthContext} from "app/common/auth/AuthContext";
import {getUserIdFromToken} from "app/common/auth/authTokenDecode";
import {useGetUser} from "../services/users/users";
import {InfoPopup} from "../common/infoPopup";
import {useGetBallotPeriod} from "../services/ballot-periods/ballot-periods";
import {useGlobalLoader} from "app/common/GlobalLoader";

enum BallotStatus {
  UPCOMING = "UPCOMING",
  RUNNING = "RUNNING",
  CLOSED = "CLOSED",
}

type BallotPeriodResult = {
  data: BallotPeriodResource | undefined;
  status: BallotStatus;
  start_date?: number;
  end_date?: number;
};

// data fetching is performed inside the component to access loading flags

export function Dashboard() {
  const auth = useContext(AuthContext);
  const {show, hide} = useGlobalLoader();
  const {t} = useTranslation();
  const navigate = useNavigate();
  const [selectedEntry, setSelectedEntry] = useState<
      ApplicationResource | undefined
  >(undefined);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  useDocumentTitle(t("home.index.headline"));
  const userID = getUserIdFromToken(auth?.accessToken);
  const {data: userData, isFetching: isFetchingUser} = useGetUser(userID, {
    axios: {headers: {Authorization: `Bearer ${auth?.accessToken}`}},
    query: {select: (res) => res.data, enabled: !!userID},
  });

  const {data: applicationsData, isFetching: isFetchingApps} =
      useListApplications(
          {user_id: userID},
          {
            axios: {headers: {Authorization: `Bearer ${auth?.accessToken}`}},
            query: {select: (res) => res.data, enabled: !!userID},
          }
      );

  const {data: ballotResp, isFetching: isFetchingBallot} = useGetBallotPeriod(
      {
        axios: {headers: {Authorization: `Bearer ${auth?.accessToken}`}},
      }
  );

  // derive ballotPeriodDetails
  const ballotPeriodDetails = React.useMemo(() => {
    const response = ballotResp;
    if (!(response?.data?.start_date || response?.data?.end_date)) {
      return {
        data: response?.data,
        status: BallotStatus.CLOSED,
      } as BallotPeriodResult;
    }
    const mostCurrentPeriod = {
      start_date: new Date(response?.data?.start_date || "").getTime(),
      end_date: new Date(response?.data?.end_date || "").getTime(),
    };
    if (mostCurrentPeriod.start_date > Date.now())
      return {
        data: response?.data,
        status: BallotStatus.UPCOMING,
        start_date: mostCurrentPeriod.start_date,
        end_date: mostCurrentPeriod.end_date,
      } as BallotPeriodResult;
    if (
        mostCurrentPeriod.start_date <= Date.now() &&
        mostCurrentPeriod.end_date >= Date.now()
    )
      return {
        data: response?.data,
        status: BallotStatus.RUNNING,
        start_date: mostCurrentPeriod.start_date,
        end_date: mostCurrentPeriod.end_date,
      } as BallotPeriodResult;
    return {
      data: response?.data,
      status: BallotStatus.CLOSED,
      start_date: mostCurrentPeriod.start_date,
      end_date: mostCurrentPeriod.end_date,
    } as BallotPeriodResult;
  }, [ballotResp]);

  let ballotDetailsProps = {
    bgColor: "bg-red-600",
    text: t("dashboard.ballotPeriodInfo.errorText"),
  };

  if (ballotPeriodDetails?.start_date && ballotPeriodDetails?.end_date) {
    if (ballotPeriodDetails.status === BallotStatus.UPCOMING) {
      ballotDetailsProps.bgColor = "bg-blue-600";
      ballotDetailsProps.text =
          t("dashboard.ballotPeriodInfo.upcomingText") +
          ": " +
          formatDateShort(ballotPeriodDetails.start_date, t) +
          " - " +
          formatDateShort(ballotPeriodDetails.end_date, t);
    } else if (ballotPeriodDetails.status === BallotStatus.RUNNING) {
      ballotDetailsProps.bgColor = "bg-green-600";
      ballotDetailsProps.text =
          t("dashboard.ballotPeriodInfo.ongoingText") +
          ": " +
          formatDateShort(ballotPeriodDetails.start_date, t) +
          " - " +
          formatDateShort(ballotPeriodDetails.end_date, t);
    } else if (ballotPeriodDetails.status === BallotStatus.CLOSED) {
      ballotDetailsProps.bgColor = "bg-gray-600";
      ballotDetailsProps.text =
          t("dashboard.ballotPeriodInfo.closedText") +
          ": " +
          formatDateShort(ballotPeriodDetails.start_date, t) +
          " - " +
          formatDateShort(ballotPeriodDetails.end_date, t);
    }
  }

  const openDetails = (entry: ApplicationResource) => {
    setSelectedEntry(entry);
    setIsDetailsOpen(true);
  };

  const closeDetails = () => {
    setIsDetailsOpen(false);
    setSelectedEntry(undefined);
  };
  const applications: ApplicationResource[] = Array.isArray(applicationsData)
      ? applicationsData
      : [];
  const totalPage = Math.ceil(applications.length / itemsPerPage);

  const currentData = applications.slice(
      (currentPage - 1) * itemsPerPage,
      currentPage * itemsPerPage
  );

  function handleNewApplicationClick() {
    navigate(`/license-application-request`);
  }

  useEffect(() => {
    if (isFetchingUser || isFetchingApps || isFetchingBallot) {
      show("Loading Dashboard");
    } else {
      hide();
    }
  }, [isFetchingUser, isFetchingApps, isFetchingBallot, show, hide]);

  return (
      <div className="container mx-auto px-4 md:px-6">
        <div className="relative flex flex-col min-h-[calc(100vh-8rem)] bg-white">
          <div className="font-inter flex flex-col items-center w-full">
            <div className="text-center mt-16 text-xl">
              <div className="flex flex-col items-center">
                <div className="text-3xl font-bold text-mallorca-purple">
                  {t("dashboard.headline.hello", {
                    firstName: userData?.firstName || "User",
                  })}
                </div>
                <div className="mt-2 text-2xl font-bold text-mallorca-purple">
                  {t("dashboard.headline.getStarted") +
                      (applications.length > 0
                          ? t("dashboard.headline.manageApplications")
                          : "")}
                </div>
              </div>
            </div>

            {ballotPeriodDetails.status === BallotStatus.RUNNING ? (
                <button
                    type="button"
                    id="createNewApplicationBtn"
                    className="bg-mallorca-purple text-white px-10 py-2 rounded-md font-medium text-lg mt-12 w-max"
                    onClick={handleNewApplicationClick}
                >
                  {t("dashboard.buttonLabel")}
                </button>
            ) : (
                <div className="mt-12 text-xl font-medium px-10 py-2 rounded-md bg-gray-100 text-gray-700">
                  {t("dashboard.noBallotRunning")}
                </div>
            )}

            {currentData.length > 0 ? (
                <div className="mt-16 w-full">
                  <h1 className="font-bold text-xl text-mallorca-purple">
                    {t("dashboard.title")}
                  </h1>

                  <div className="overflow-x-auto">
                    <table className="mt-8 text-lg dashboard-table text-mallorca-purple">
                      <thead>
                      <tr>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">
                          {t("dashboard.table.applicationId")}
                        </th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">
                          {t("dashboard.table.cadastalId")}
                        </th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">
                          {t("dashboard.table.requestDate")}
                        </th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">
                          {t("dashboard.table.licenceType")}
                        </th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">
                          {t("dashboard.table.status")}
                        </th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">
                          {t("dashboard.table.action.title")}
                        </th>
                      </tr>
                      </thead>

                      <tbody>
                      {currentData.map((item) => (
                          <tr key={item.id}>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">
                              {item.id}
                            </td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">
                              {item.cadastral_reference}
                            </td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">
                              {formatDateShort(item.applied_at, t)}
                            </td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">
                              {item.license_type}
                            </td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">
                          <span
                              className={`inline-flex items-center justify-center text-center p-1 px-4 min-w-56 rounded-md 
                              text-${getApplicationStatusColor(item.application_status)}-800 
                              bg-${getApplicationStatusColor(item.application_status)}-200 font-semibold`}
                          >
                            {t(formatStatusLabel("dashboard.licenceStatus.", item.application_status))}
                          </span>
                            </td>

                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">
                              <button
                                  id={`viewDetailsBtn-${item.id}`}
                                  key={item.id}
                                  onClick={() => openDetails(item)}
                                  className="text-mallorca-purple underline"
                              >
                                {t("dashboard.table.action.value")}
                              </button>
                            </td>
                          </tr>
                      ))}
                      </tbody>
                    </table>

                    <div className="my-8"/>

                    <Pagination
                        currentPage={currentPage}
                        totalPage={totalPage}
                        onPageChange={setCurrentPage}
                        start={(currentPage - 1) * itemsPerPage + 1}
                        end={currentPage * itemsPerPage}
                        numberOfItems={applications.length}
                    />
                  </div>
                </div>
            ) : (
                <div
                    className="mt-48 w-full text-mallorca-purple/25 text-5xl flex justify-center items-center h-full">
                  {t("dashboard.noApplications")}
                </div>
            )}
          </div>
          {/* Ballot Period Info Popup */}
          <div className="mt-auto flex justify-center w-full mb-4">
            <InfoPopup
                bgColor={ballotDetailsProps.bgColor}
                text={ballotDetailsProps.text}
            />
          </div>

          {/* Application Details Modal */}
          {isDetailsOpen && (
              <ApplicationDetails
                  open={isDetailsOpen}
                  applicationData={selectedEntry}
                  userData={userData}
                  onClose={closeDetails}
                  onRenew={handleNewApplicationClick}
              />
          )}
        </div>
      </div>
  );
}
