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
            "address": "In der Au 16B, Stuttgart",
            "requested_date": "2025-07-01T10:00:00Z",
            "license_type": "ETV",
            "license_status": "Accepted",
        },
        {
            "application_id": 202,
            "address": "Königstraße 54, Stuttgart",
            "requested_date": "2025-07-01T14:30:00Z",
            "license_type": "B2C",
            "license_status": "Declined",
        },
        {
            "application_id": 203,
            "address": "Hauptstraße 88, Ludwigsburg",
            "requested_date": "2025-07-02T09:15:00Z",
            "license_type": "ETV",
            "license_status": "In Process",
        },
        {
            "application_id": 204,
            "address": "Filderbahnplatz 28, Stuttgart",
            "requested_date": "2025-07-02T16:00:00Z",
            "license_type": "B2C",
            "license_status": "Draft",
        },
        {
            "application_id": 205,
            "address": "Gerberstraße 12, Esslingen am Neckar",
            "requested_date": "2025-07-03T11:20:00Z",
            "license_type": "HML",
            "license_status": "Expired",
        },
        {
            "application_id": 206,
            "address": "Charlottenplatz 1, Stuttgart",
            "requested_date": "2025-07-03T15:45:00Z",
            "license_type": "ETV",
            "license_status": "Accepted",
        },
        {
            "application_id": 207,
            "address": "Schillerplatz 7, Waiblingen",
            "requested_date": "2025-07-04T08:00:00Z",
            "license_type": "B2C",
            "license_status": "Expired",
        },
        {
            "application_id": 208,
            "address": "Theodor-Heuss-Straße 30, Stuttgart",
            "requested_date": "2025-07-04T12:00:00Z",
            "license_type": "HML",
            "license_status": "Draft",
        },
        {
            "application_id": 209,
            "address": "Marktplatz 1, Fellbach",
            "requested_date": "2025-07-05T13:30:00Z",
            "license_type": "ETV",
            "license_status": "Expired",
        },
        {
            "application_id": 210,
            "address": "Rotebühlplatz 2, Stuttgart",
            "requested_date": "2025-07-05T17:00:00Z",
            "license_type": "B2C",
            "license_status": "Declined",
        }
    ];


    const [currentPage, setCurrentPage] = useState(1);
    const itepmsPerPage = 3;

    const totalPage = Math.ceil(licenseRecords.length / itepmsPerPage); // Determine total number of pages
    const currentData = licenseRecords.slice((currentPage - 1) * itepmsPerPage, currentPage * itepmsPerPage); // Get current page data

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
                        start={((currentPage - 1) * itepmsPerPage) + 1}
                        end={currentPage * itepmsPerPage}
                        numberOfItems={licenseRecords.length}
                    />

                </div>


            </div>
        </div>
    );
}