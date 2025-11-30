import React, {useState} from "react";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import "./dashboard.css";
import Pagination from "../common/Pagination";
import {Link, useNavigate} from "react-router";
import {FormHeader} from "../common/headingTitle";
import {useListApplications} from "app/services/applications/applications";
import {ApplicationResource} from "../../types";

export default function Dashboard() {
  const {t} = useTranslation();
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  useDocumentTitle(t("home.index.headline"));

  const { data: response } = useListApplications({
    user_id: "f28d1d3b-9bcb-4a74-a65a-2fede2b0a6c3",
  });

// response = AxiosResponse
  const applications: ApplicationResource[] =
      Array.isArray(response?.data) ? response.data : [];

  console.log("Applications:", applications);

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
        <div className="relative min-h-[calc(100vh-4rem)] bg-white flex justify-center">
          <div className="font-inter flex flex-col items-center w-full">

            <FormHeader
                heading={
                  applications.length > 0 ? t("dashboard.headline.getStarted") : ""
                }
                className="text-center mt-16"
            />

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
                    <table className="mt-8 text-lg dashboard-table">
                      <tr>
                        <th>{t("dashboard.table.applicationId")}</th>
                        <th>{t("dashboard.table.cadastalId")}</th>
                        <th>{t("dashboard.table.requestDate")}</th>
                        <th>{t("dashboard.table.licenceType")}</th>
                        <th>{t("dashboard.table.status")}</th>
                        <th>{t("dashboard.table.action.title")}</th>
                      </tr>

                      {currentData.map((item) => (
                          <tr key={item.id}>
                            <td>{item.id}</td>
                            <td>{item.cadastral_reference}</td>
                            <td>{formatDate(item.applied_at)}</td>
                            <td>{item.license_type}</td>
                            <td>
                              <span
                                  className={`inline-flex items-center justify-center text-center p-1 px-4 min-w-[10rem] rounded-md ${
                                      item.application_status === "APPROVED"
                                          ? "text-green-600 bg-green-300"
                                          : item.application_status === "EXPIRED"
                                              ? "text-red-800 bg-red-200"
                                              : item.application_status === "CANCELLED"
                                                  ? "text-red-800 bg-red-200"
                                                  : item.application_status === "REJECTED"
                                                      ? "text-red-800 bg-red-200"
                                                      : item.application_status === "DRAFT"
                                                          ? "text-gray-800 bg-gray-200"
                                                          : [
                                                            "DOCUMENTS_SUBMITTED",
                                                            "VERIFICATION_PENDING",
                                                            "AWAITING_PAYMENT",
                                                            "PAYMENT_RECEIVED",
                                                            "SUBMITTED",
                                                            "IN_BALLOT",
                                                            "SELECTED",
                                                            "NOT_SELECTED",
                                                            "UNDER_REVIEW",
                                                          ].includes(item.application_status)
                                                              ? "text-orange-800 bg-orange-200"
                                                              : "text-gray-800 bg-gray-200"
                                  }`}
                              >
                                {
                                  item.application_status === "APPROVED"
                                      ? t("dashboard.licenceStatus.accepted")
                                      : item.application_status === "EXPIRED"
                                          ? t("dashboard.licenceStatus.expired")
                                          : item.application_status === "CANCELLED"
                                              ? t("dashboard.licenceStatus.cancelled")
                                              : item.application_status === "REJECTED"
                                                  ? t("dashboard.licenceStatus.declined")
                                                  : item.application_status === "DRAFT"
                                                      ? t("dashboard.licenceStatus.draft")
                                                      : [
                                                        "DOCUMENTS_SUBMITTED",
                                                        "VERIFICATION_PENDING",
                                                        "AWAITING_PAYMENT",
                                                        "PAYMENT_RECEIVED",
                                                        "SUBMITTED",
                                                        "IN_BALLOT",
                                                        "SELECTED",
                                                        "NOT_SELECTED",
                                                        "UNDER_REVIEW",
                                                      ].includes(item.application_status)
                                                          ? t("dashboard.licenceStatus.inProcess")
                                                          : ""
                                }
                              </span>
                            </td>

                            <td>
                              <Link
                                  to="register"
                                  className="text-mallorca-purple underline"
                              >
                                {t("dashboard.table.action.value")}
                              </Link>
                            </td>
                          </tr>
                      ))}
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
      </div>
  );
}