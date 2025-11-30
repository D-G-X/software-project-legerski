import React, {useState} from "react";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import "./dashboard.css";
import Pagination from "../common/Pagination";
import {Link, useNavigate} from "react-router";
import {FormHeader} from "../common/headingTitle";
// import useDocumentTitle from "app/common/use-document-title";

type LicenseRecord = {
  application_id: number;
  cadastralId: string;
  requested_date: string;
  license_type: string;
  license_status: string;
};

export default function Dashboard() {

  const {t} = useTranslation();
  useDocumentTitle(t("home.index.headline"));

  const licenseRecords: LicenseRecord[] = [
    {
      "application_id": 201,
      "cadastralId": "07123A04500012",
      "requested_date": "2025-07-01T10:00:00Z",
      "license_type": "ETV",
      "license_status": "ACCEPTED"
    },
    {
      "application_id": 202,
      "cadastralId": "07123A04600098",
      "requested_date": "2025-07-01T14:30:00Z",
      "license_type": "ETVPL",
      "license_status": "REJECTED"
    },
    {
      "application_id": 203,
      "cadastralId": "07084B01200341",
      "requested_date": "2025-07-02T09:15:00Z",
      "license_type": "ETV",
      "license_status": "DOCUMENTS_SUBMITTED"
    },
    {
      "application_id": 204,
      "cadastralId": "07123C05100177",
      "requested_date": "2025-07-02T16:00:00Z",
      "license_type": "ETVPL",
      "license_status": "DRAFT"
    },
    {
      "application_id": 205,
      "cadastralId": "07085A02200456",
      "requested_date": "2025-07-03T11:20:00Z",
      "license_type": "ETV60",
      "license_status": "EXPIRED"
    },
    {
      "application_id": 206,
      "cadastralId": "07123D03300912",
      "requested_date": "2025-07-03T15:45:00Z",
      "license_type": "ETV",
      "license_status": "ACCEPTED"
    },
    {
      "application_id": 207,
      "cadastralId": "07092A01400072",
      "requested_date": "2025-07-04T08:00:00Z",
      "license_type": "ETVPL",
      "license_status": "EXPIRED"
    },
    {
      "application_id": 208,
      "cadastralId": "07123B05700831",
      "requested_date": "2025-07-04T12:00:00Z",
      "license_type": "ETV60",
      "license_status": "DRAFT"
    },
    {
      "application_id": 209,
      "cadastralId": "07086A00900214",
      "requested_date": "2025-07-05T13:30:00Z",
      "license_type": "ETV",
      "license_status": "EXPIRED"
    },
    {
      "application_id": 210,
      "cadastralId": "07123A01900166",
      "requested_date": "2025-07-05T17:00:00Z",
      "license_type": "ETVPL",
      "license_status": "REJECTED"
    },
    {
      "application_id": 211,
      "cadastralId": "07084C00400095",
      "requested_date": "2025-07-06T10:30:00Z",
      "license_type": "ETV60",
      "license_status": "DOCUMENTS_SUBMITTED"
    },
    {
      "application_id": 212,
      "cadastralId": "07123A04100320",
      "requested_date": "2025-07-06T14:15:00Z",
      "license_type": "ETV",
      "license_status": "ACCEPTED"
    },
    {
      "application_id": 213,
      "cadastralId": "07085B01600402",
      "requested_date": "2025-07-07T09:45:00Z",
      "license_type": "ETVPL",
      "license_status": "DRAFT"
    },
    {
      "application_id": 214,
      "cadastralId": "07123D06200911",
      "requested_date": "2025-07-07T16:30:00Z",
      "license_type": "ETV60",
      "license_status": "REJECTED"
    },
    {
      "application_id": 215,
      "cadastralId": "07092A01300144",
      "requested_date": "2025-07-08T11:10:00Z",
      "license_type": "ETV",
      "license_status": "DOCUMENTS_SUBMITTED"
    },
    {
      "application_id": 216,
      "cadastralId": "07123B03800773",
      "requested_date": "2025-07-08T15:50:00Z",
      "license_type": "ETVPL",
      "license_status": "ACCEPTED"
    },
    {
      "application_id": 217,
      "cadastralId": "07086C02100058",
      "requested_date": "2025-07-09T10:20:00Z",
      "license_type": "ETV60",
      "license_status": "DRAFT"
    },
    {
      "application_id": 218,
      "cadastralId": "07123A04400610",
      "requested_date": "2025-07-09T14:40:00Z",
      "license_type": "ETV",
      "license_status": "EXPIRED"
    },
    {
      "application_id": 219,
      "cadastralId": "07084B02500281",
      "requested_date": "2025-07-10T09:00:00Z",
      "license_type": "ETVPL",
      "license_status": "REJECTED"
    },
    {
      "application_id": 220,
      "cadastralId": "07123C03900033",
      "requested_date": "2025-07-10T13:30:00Z",
      "license_type": "ETV60",
      "license_status": "DOCUMENTS_SUBMITTED"
    },
    {
      "application_id": 221,
      "cadastralId": "07085A01800490",
      "requested_date": "2025-07-11T11:45:00Z",
      "license_type": "ETV",
      "license_status": "ACCEPTED"
    },
    {
      "application_id": 222,
      "cadastralId": "07123D05400088",
      "requested_date": "2025-07-11T16:20:00Z",
      "license_type": "ETVPL",
      "license_status": "DRAFT"
    },
    {
      "application_id": 223,
      "cadastralId": "07092B01100039",
      "requested_date": "2025-07-12T10:05:00Z",
      "license_type": "ETV60",
      "license_status": "EXPIRED"
    },
    {
      "application_id": 224,
      "cadastralId": "07123A02700264",
      "requested_date": "2025-07-12T14:55:00Z",
      "license_type": "ETV",
      "license_status": "REJECTED"
    }
  ];

  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const totalPage = Math.ceil(licenseRecords.length / itemsPerPage); // Determine total number of pages
  let currentData: typeof licenseRecords = [];
  if (licenseRecords.length > 0) {
    currentData = licenseRecords.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
    );
  }

  const formatDate = (rawDate: string) => {
    return new Date(rawDate).toLocaleString(t("locale"), {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  function handleNewApplicationClick() {
    navigate(`/license-application-request`)
  }

  return (
      <div className="container mx-auto px-4 md:px-6">
        <div className="relative min-h-[calc(100vh-4rem)] bg-white flex justify-center">
          <div className="font-inter min-w-192 flex flex-col items-center">

            <FormHeader
                heading={licenseRecords.length > 0
                    ? t("dashboard.headline.getStarted")
                    : ""}
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

                      {currentData.map(item => (
                          <tr key={item.application_id}>
                            <td>{item.application_id}</td>
                            <td>{item.cadastralId}</td>
                            <td>{formatDate(item.requested_date)}</td>
                            <td>{item.license_type}</td>
                            <td>
                    <span
                        className={`inline-flex items-center justify-center text-center p-1 px-4 min-w-[10rem] rounded-md ${
                            item.license_status === "ACCEPTED"
                                ? "text-green-600 bg-green-300"
                                : item.license_status === "EXPIRED"
                                    ? "text-red-800 bg-red-200"
                                    : item.license_status === "DOCUMENTS_SUBMITTED"|| "VERIFICATION_PENDING"|| "AWAITING_PAYMENT"|| "PAYMENT_RECEIVED"|| "SUBMITTED"|| "IN_BALLOT"
                                        ? "text-orange-800 bg-orange-200"
                                        : item.license_status === "DRAFT"
                                            ? "text-gray-800 bg-gray-200"
                                            : "text-red-800 bg-red-200"
                        }`}
                    >
                      {
                        item.license_status === "ACCEPTED" ? t("dashboard.licenceStatus.accepted") :
                            item.license_status === "EXPIRED" ? t("dashboard.licenceStatus.expired") :
                                item.license_status === "DRAFT" ? t("dashboard.licenceStatus.draft") :
                                    ["DOCUMENTS_SUBMITTED", "VERIFICATION_PENDING", "AWAITING_PAYMENT", "PAYMENT_RECEIVED", "SUBMITTED", "IN_BALLOT"].includes(item.license_status)
                                        ? t("dashboard.licenceStatus.inProcess")
                                        : item.license_status === "REJECTED"
                                            ? t("dashboard.licenceStatus.declined")
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

                    <div className="my-8"></div>

                    <Pagination
                        currentPage={currentPage}
                        totalPage={totalPage}
                        onPageChange={setCurrentPage}
                        start={(currentPage - 1) * itemsPerPage + 1}
                        end={currentPage * itemsPerPage}
                        numberOfItems={licenseRecords.length}
                    />
                  </div>
                </div>
            )}
          </div>
        </div>
      </div>
  );
}