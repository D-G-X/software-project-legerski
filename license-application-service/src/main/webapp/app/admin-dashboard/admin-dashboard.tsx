import React, {useContext, useState} from "react";
import {useTranslation} from "react-i18next";
import {formatDateShort, formatStatusLabel, getApplicationStatusColor, useDocumentTitle} from "../common/utils";
import Pagination from "../common/Pagination";
import {Link, useNavigate} from "react-router";
import {ApplicationResource} from "../../types";
import AdminApplicationDetails from "./admin-applicationDetails";
import {AuthContext} from "../common/auth/AuthContext";
import {useGetBallotPeriodEntries} from "../services/ballot-periods/ballot-periods";

function getApplicationsForBallotPeriod(periodId: number | undefined) {
  if (!periodId) return;
  const auth = useContext(AuthContext);
  const {data: response} = useGetBallotPeriodEntries(periodId,
      {
        axios: {
          headers: {
            Authorization: `Bearer ${auth?.accessToken}`,
          },
        },
      }
  );
  return response;
}

export default function AdminDashboard() {

  const {t} = useTranslation();
  const navigate = useNavigate();
  const [selectedEntry, setSelectedEntry] = useState<ApplicationResource | undefined>(undefined);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;
  useDocumentTitle(t("home.index.headline"));

  const openDetails = (entry: ApplicationResource) => {
    setSelectedEntry(entry);
    setIsDetailsOpen(true);
  };

  const closeDetails = () => {
    setIsDetailsOpen(false);
    setSelectedEntry(undefined);
  };

  const response = getApplicationsForBallotPeriod(1) // TODO: replace with actual period ID
  const applications: ApplicationResource[] = Array.isArray(response?.data)
      ? response.data
      : [];

  function handleNewApplicationClick() {
    navigate(`/login`);
  }

  const totalPage = Math.ceil(applications.length / itemsPerPage);

  const currentData = applications.slice(
      (currentPage - 1) * itemsPerPage,
      currentPage * itemsPerPage
  );


  return (
      <div className="container mx-auto px-4 md:px-6">
        <div className="relative min-h-[calc(100vh-8rem)] bg-white flex justify-center">
          <div className="font-inter flex flex-col w-full mt-4">
            <h1 className="text-3xl font-bold text-mallorca-purple text-left">{t("admin-dashboard.headline")}</h1>
            <div className={"flex justify-between mt-2"}>
              <Link to={"/login"}
                    className={"text-green-500"}>{t("admin-dashboard.links.active_requests")}</Link>
              <button onClick={handleNewApplicationClick}
                      className={"text-blue-500 underline"}>{t("admin-dashboard.links.ballot_management")}</button>
            </div>

            {currentData.length > 0 && (
                <div className="mt-16 w-full">
                  <h1 className="font-bold text-xl text-mallorca-purple">
                    {t("dashboard.title")}
                  </h1>

                  <div className="overflow-x-auto">
                    <table className="mt-8 text-lg dashboard-table text-mallorca-purple">
                      <thead>
                      <tr>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">{t("admin-dashboard.table.requester_name")}</th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">{t("admin-dashboard.table.address")}</th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">{t("admin-dashboard.table.phone_number")}</th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">{t("admin-dashboard.table.email")}</th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">{t("admin-dashboard.table.country")}</th>
                        <th className="w-1/5 text-left border-b border-black/10 p-2.5 text-black/30 font-normal">{t("admin-dashboard.table.status")}</th>
                      </tr>
                      </thead>
                      <tbody>
                      {currentData.map((item) => (
                          <tr key={item.id}>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">{item.user_id}</td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">{item.license_type}</td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">{item.cadastral_reference}</td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">{formatDateShort(item.applied_at, t)}</td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">{formatDateShort(item.changed_at, t)}</td>
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">{item.application_status}</td>
                            {(item.remarks && (
                                <td className="w-1/5 text-left border-b border-black/10 p-2.5">{item.remarks}</td>
                            ))}
                            <td className="w-1/5 text-left border-b border-black/10 p-2.5">
                        <span
                            className={`inline-flex items-center justify-center text-center p-1 px-4 min-w-56 rounded-md 
                              text-${getApplicationStatusColor(item.application_status)}-800 
                              bg-${getApplicationStatusColor(item.application_status)}-200 font-semibold`}
                        >
                          {t(formatStatusLabel("dashboard.licenceStatus.", item.application_status))}
                        </span>
                            </td>
                            <td className="w-1/5 text-left border-b border-black/10 p-[10px]">
                              <button
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
            )}
          </div>
        </div>
        <div>
          {/* Application Details Modal */}
          {isDetailsOpen &&
              selectedEntry?.user_id &&
              (
                  <AdminApplicationDetails
                      open={isDetailsOpen}
                      applicationData={selectedEntry}
                      userId={selectedEntry?.user_id}
                      onClose={closeDetails}
                  />
              )}
        </div>
      </div>
  );
}