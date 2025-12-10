import React, {useState} from "react";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import "./dashboard.css";
import Pagination from "../common/Pagination";
import {useNavigate} from "react-router";
import {useListApplications} from "app/services/applications/applications";
import {ApplicationResource} from "../../types";
import ApplicationDetails from "./applicationDetails";

export default function Dashboard() {
  const {t} = useTranslation();
  const navigate = useNavigate();
  const [selectedEntry, setSelectedEntry] = useState<ApplicationResource | null>(null);
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
    setSelectedEntry(null);
  };

  const {data: response} = useListApplications({
    user_id: "f28d1d3b-9bcb-4a74-a65a-2fede2b0a6c3",
  });

  // response = AxiosResponse
  const applications: ApplicationResource[] = Array.isArray(response?.data)
      ? response.data
      : [];
  const totalPage = Math.ceil(applications.length / itemsPerPage);

  const currentData = applications.slice(
      (currentPage - 1) * itemsPerPage,
      currentPage * itemsPerPage
  );

  const formatDate = (rawDate: string | undefined) => {
    if (!rawDate) return "";
    return new Date(rawDate).toLocaleString(t("locale"), {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  function handleNewApplicationClick() {
    navigate(`/license-application-request`);
  }

  return (
      <div className="container mx-auto px-4 md:px-6">
        <div className="relative min-h-[calc(100vh-8rem)] bg-white flex justify-center">
          <div className="font-inter flex flex-col items-center w-full">
            <div className="text-center mt-16 text-xl">
              <div className="text-3xl font-bold text-mallorca-purple">
                {t("dashboard.headline.getStarted") +
                    (applications.length > 0
                        ? t("dashboard.headline.manageApplications")
                        : "")}
              </div>
            </div>

            <button
                type="button"
                className="bg-mallorca-purple text-white px-10 py-2 rounded-md font-medium text-lg mt-12 w-max"
                onClick={handleNewApplicationClick}
            >
              {t("dashboard.buttonLabel")}
            </button>

            {currentData.length > 0 && (
                <div className="mt-16 w-full">
                  <h1 className="font-bold text-xl text-mallorca-purple">
                    {t("dashboard.title")}
                  </h1>

                  <div className="overflow-x-auto">
                    <table className="mt-8 text-lg dashboard-table text-mallorca-purple">
                      <thead>
                      <tr>
                        <th>{t("dashboard.table.applicationId")}</th>
                        <th>{t("dashboard.table.cadastalId")}</th>
                        <th>{t("dashboard.table.requestDate")}</th>
                        <th>{t("dashboard.table.licenceType")}</th>
                        <th>{t("dashboard.table.status")}</th>
                        <th>{t("dashboard.table.action.title")}</th>
                      </tr>
                      </thead>

                      <tbody>
                      {currentData.map((item) => (
                          <tr key={item.id}>
                            <td>{item.id}</td>
                            <td>{item.cadastral_reference}</td>
                            <td>{formatDate(item.applied_at)}</td>
                            <td>{item.license_type}</td>
                            <td>
                          <span
                              className={`inline-flex items-center justify-center text-center p-1 px-4 min-w-56 rounded-md ${
                                  // green
                                  ["SELECTED", "PAYMENT_RECEIVED"].includes(
                                      item.application_status
                                  )
                                      ? "text-green-800 bg-green-200"
                                      : // blue
                                      ["SUBMITTED",
                                        "UNDER_REVIEW",
                                        "AWAITING_PAYMENT",
                                        "APPROVED",
                                        "IN_BALLOT",]
                                      .includes(item.application_status)
                                          ? "text-blue-800 bg-blue-200"
                                          :
                                          ["CANCELLED", "REJECTED", "NOT_SELECTED",].includes(
                                              item.application_status
                                          )
                                              ? "text-red-800 bg-red-200"
                                              : // grey
                                              ["DRAFT", "EXPIRED",].includes(item.application_status)
                                                  ? "text-gray-800 bg-gray-200"
                                                  : // orange (all in-process)
                                                  ["DOCUMENTS_SUBMITTED", "VERIFICATION_PENDING",].includes(item.application_status)
                                                      ? "text-orange-800 bg-orange-200"
                                                      : // fallback
                                                      "text-mallorca-purple bg-mallorca-purple/10"
                              }`}
                          >
                            {t(
                                "dashboard.licenceStatus." +
                                item.application_status
                                .toLowerCase()
                                .replace(/_([a-z])/g, (_, c) => c.toUpperCase()) // Camel case
                            )}
                          </span>
                            </td>

                            <td>
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
          {/* Application Details Modal */}
          {isDetailsOpen && (
            <ApplicationDetails
                open={isDetailsOpen}
                applicationData={selectedEntry}
                onClose={closeDetails}
            />
          )}
        </div>
      </div>
  );
}
