import React from "react";
import { useState } from 'react'
import {useTranslation} from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import "./dashboard.css";
import Pagination from "../common/Pagination";
import {Link} from "react-router";
// import useDocumentTitle from "app/common/use-document-title";



export default function Dashboard(){

    const { t } = useTranslation();

    const licenseRecords = [
      {
        "application_id": 201,
        "address": "07123A04500012",
        "requested_date": "2025-07-01T10:00:00Z",
        "license_type": "ETV",
        "license_status": "Accepted"
      },
      {
        "application_id": 202,
        "address": "07123A04600098",
        "requested_date": "2025-07-01T14:30:00Z",
        "license_type": "ETVPL",
        "license_status": "Declined"
      },
      {
        "application_id": 203,
        "address": "07084B01200341",
        "requested_date": "2025-07-02T09:15:00Z",
        "license_type": "ETV",
        "license_status": "In Process"
      },
      {
        "application_id": 204,
        "address": "07123C05100177",
        "requested_date": "2025-07-02T16:00:00Z",
        "license_type": "ETVPL",
        "license_status": "Draft"
      },
      {
        "application_id": 205,
        "address": "07085A02200456",
        "requested_date": "2025-07-03T11:20:00Z",
        "license_type": "ETV60",
        "license_status": "Expired"
      },
      {
        "application_id": 206,
        "address": "07123D03300912",
        "requested_date": "2025-07-03T15:45:00Z",
        "license_type": "ETV",
        "license_status": "Accepted"
      },
      {
        "application_id": 207,
        "address": "07092A01400072",
        "requested_date": "2025-07-04T08:00:00Z",
        "license_type": "ETVPL",
        "license_status": "Expired"
      },
      {
        "application_id": 208,
        "address": "07123B05700831",
        "requested_date": "2025-07-04T12:00:00Z",
        "license_type": "ETV60",
        "license_status": "Draft"
      },
      {
        "application_id": 209,
        "address": "07086A00900214",
        "requested_date": "2025-07-05T13:30:00Z",
        "license_type": "ETV",
        "license_status": "Expired"
      },
      {
        "application_id": 210,
        "address": "07123A01900166",
        "requested_date": "2025-07-05T17:00:00Z",
        "license_type": "ETVPL",
        "license_status": "Declined"
      },
      {
        "application_id": 211,
        "address": "07084C00400095",
        "requested_date": "2025-07-06T10:30:00Z",
        "license_type": "ETV60",
        "license_status": "In Process"
      },
      {
        "application_id": 212,
        "address": "07123A04100320",
        "requested_date": "2025-07-06T14:15:00Z",
        "license_type": "ETV",
        "license_status": "Accepted"
      },
      {
        "application_id": 213,
        "address": "07085B01600402",
        "requested_date": "2025-07-07T09:45:00Z",
        "license_type": "ETVPL",
        "license_status": "Draft"
      },
      {
        "application_id": 214,
        "address": "07123D06200911",
        "requested_date": "2025-07-07T16:30:00Z",
        "license_type": "ETV60",
        "license_status": "Declined"
      },
      {
        "application_id": 215,
        "address": "07092A01300144",
        "requested_date": "2025-07-08T11:10:00Z",
        "license_type": "ETV",
        "license_status": "In Process"
      },
      {
        "application_id": 216,
        "address": "07123B03800773",
        "requested_date": "2025-07-08T15:50:00Z",
        "license_type": "ETVPL",
        "license_status": "Accepted"
      },
      {
        "application_id": 217,
        "address": "07086C02100058",
        "requested_date": "2025-07-09T10:20:00Z",
        "license_type": "ETV60",
        "license_status": "Draft"
      },
      {
        "application_id": 218,
        "address": "07123A04400610",
        "requested_date": "2025-07-09T14:40:00Z",
        "license_type": "ETV",
        "license_status": "Expired"
      },
      {
        "application_id": 219,
        "address": "07084B02500281",
        "requested_date": "2025-07-10T09:00:00Z",
        "license_type": "ETVPL",
        "license_status": "Declined"
      },
      {
        "application_id": 220,
        "address": "07123C03900033",
        "requested_date": "2025-07-10T13:30:00Z",
        "license_type": "ETV60",
        "license_status": "In Process"
      },
      {
        "application_id": 221,
        "address": "07085A01800490",
        "requested_date": "2025-07-11T11:45:00Z",
        "license_type": "ETV",
        "license_status": "Accepted"
      },
      {
        "application_id": 222,
        "address": "07123D05400088",
        "requested_date": "2025-07-11T16:20:00Z",
        "license_type": "ETVPL",
        "license_status": "Draft"
      },
      {
        "application_id": 223,
        "address": "07092B01100039",
        "requested_date": "2025-07-12T10:05:00Z",
        "license_type": "ETV60",
        "license_status": "Expired"
      },
      {
        "application_id": 224,
        "address": "07123A02700264",
        "requested_date": "2025-07-12T14:55:00Z",
        "license_type": "ETV",
        "license_status": "Declined"
      }
    ];


    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 10;

    const totalPage = Math.ceil(licenseRecords.length / itemsPerPage); // Determine total number of pages
    const currentData = licenseRecords.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage); // Get current page data

        useDocumentTitle(t("home.index.headline"));

    return (
        <div className="container mx-auto px-4 md:px-6">
            <div className="relative min-h-[calc(100vh-4rem)] bg-white justify-center mt-8">
                <h2 className="font-bold text-center text-xl mb-6">{t("dashboard.headline")}</h2>
                <button
                    type="button"
                    className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg mb-6 w-max block m-auto">
                    {t("dashboard.button-text")}
                </button>
                <h1 className="font-bold text-xl">{t("dashboard.title")}</h1>
                <div className={"overflow-x-auto"}>
                    <table className={"mt-8"}>
                        <tr>
                            <th>{t("dashboard.table.application_id")}</th>
                            <th>{t("dashboard.table.address")}</th>
                            <th>{t("dashboard.table.request_date")}</th>
                            <th>{t("dashboard.table.licence_type")}</th>
                            <th>{t("dashboard.table.status")}</th>
                            <th>{t("dashboard.table.action.title")}</th>
                        </tr>
                        {currentData.map(item => (
                            <tr key={item.application_id}>
                                <td>{item.application_id}</td>
                                <td>{item.address}</td>
                                <td>{item.requested_date}</td>
                                <td>{item.license_type}</td>
                                <td>
                                <span className={`text-center p-1 px-4 border rounded-md ${item.license_status === 'Accepted' ? "text-green-600 bg-green-300 border-green-600" :
                                    (item.license_status === "Declined" ? "text-red-600 bg-red-300 border-red-600" :
                                        (item.license_status === "In Process" ? "text-orange-600 bg-orange-300 border-orange-600" :
                                            (item.license_status === "Draft" ? "text-gray-600 bg-gray-300 border-gray-600" : "text-green-600 bg-red-300 border-green-600" )))
                                }`}>
                                    {item.license_status === 'Accepted' ? t("dashboard.licence_status.accepted") :
                                        (item.license_status === "Declined" ? t("dashboard.licence_status.declined") :
                                            (item.license_status === "In Process" ? t("dashboard.licence_status.in_process") :
                                                (item.license_status === "Draft" ? t("dashboard.licence_status.draft") : t("dashboard.licence_status.expired") )))}
                                </span>

                                </td>
                                <td><Link to="register" className={"text-blue-500 underline"}>{t("dashboard.table.action.value")}</Link></td>
                            </tr>
                        ))}
                    </table>
                    <Pagination
                        currentPage={currentPage}
                        totalPage={totalPage}
                        onPageChange={setCurrentPage}
                        start={((currentPage - 1) * itemsPerPage) + 1}
                        end={currentPage * itemsPerPage}
                        numberOfItems={licenseRecords.length}
                    />

                </div>


            </div>
        </div>
    );
}