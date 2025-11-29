import React from "react";
import { useState } from 'react'
import {useTranslation} from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import "./dashboard.css";
import Pagination from "../common/Pagination";
// import useDocumentTitle from "app/common/use-document-title";



export default function Dashboard(){

    const licenseRecords = [
        {
            "application_id": 201,
            "address": "In der Au 16B, Stuttgart",
            "requested_date": "2025-07-01T10:00:00Z",
            "license_type": "ETV",
            "license_status": "ACTIVE",
        },
        {
            "application_id": 202,
            "address": "Königstraße 54, Stuttgart",
            "requested_date": "2025-07-01T14:30:00Z",
            "license_type": "B2C",
            "license_status": "PENDING",
        },
        {
            "application_id": 203,
            "address": "Hauptstraße 88, Ludwigsburg",
            "requested_date": "2025-07-02T09:15:00Z",
            "license_type": "ETV",
            "license_status": "REVOKED",
        },
        {
            "application_id": 204,
            "address": "Filderbahnplatz 28, Stuttgart",
            "requested_date": "2025-07-02T16:00:00Z",
            "license_type": "B2C",
            "license_status": "ACTIVE",
        },
        {
            "application_id": 205,
            "address": "Gerberstraße 12, Esslingen am Neckar",
            "requested_date": "2025-07-03T11:20:00Z",
            "license_type": "HML",
            "license_status": "PENDING",
        },
        {
            "application_id": 206,
            "address": "Charlottenplatz 1, Stuttgart",
            "requested_date": "2025-07-03T15:45:00Z",
            "license_type": "ETV",
            "license_status": "ACTIVE",
        },
        {
            "application_id": 207,
            "address": "Schillerplatz 7, Waiblingen",
            "requested_date": "2025-07-04T08:00:00Z",
            "license_type": "B2C",
            "license_status": "ARCHIVED",
        },
        {
            "application_id": 208,
            "address": "Theodor-Heuss-Straße 30, Stuttgart",
            "requested_date": "2025-07-04T12:00:00Z",
            "license_type": "HML",
            "license_status": "PENDING",
        },
        {
            "application_id": 209,
            "address": "Marktplatz 1, Fellbach",
            "requested_date": "2025-07-05T13:30:00Z",
            "license_type": "ETV",
            "license_status": "ACTIVE",
        },
        {
            "application_id": 210,
            "address": "Rotebühlplatz 2, Stuttgart",
            "requested_date": "2025-07-05T17:00:00Z",
            "license_type": "B2C",
            "license_status": "REVOKED",
        }
    ];


    const [currentPage, setCurrentPage] = useState(1);
    const itepmsPerPage = 3;

    const totalPage = Math.ceil(licenseRecords.length / itepmsPerPage); // Determine total number of pages
    const currentData = licenseRecords.slice((currentPage - 1) * itepmsPerPage, currentPage * itepmsPerPage); // Get current page data

    const { t } = useTranslation();
    useDocumentTitle(t("home.index.headline"));

    return (
        <div className="container mx-auto px-4 md:px-6">
            <div className="justify-center mt-8">
                <h2 className="font-bold text-center mb-6">{t("dashboard.headline")}</h2>
                <button
                    type="button"
                    className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg mb-6 w-max block m-auto">
                    {t("dashboard.button-text")}
                </button>
            </div>
            <div>
                <h1 className="font-bold">{t("dashboard.title")}</h1>
                <table>
                    <tr>
                        <th>Application ID</th>
                        <th>Address</th>
                        <th>Request Date</th>
                        <th>Licence Type</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                    {currentData.map(item => (
                        <tr key={item.application_id}>
                            <td>{item.application_id}</td>
                            <td>{item.address}</td>
                            <td>{item.requested_date}</td>
                            <td>{item.license_type}</td>
                            <td>{item.license_status}</td>
                            <td>More Action</td>
                        </tr>
                    ))}
                </table>

                <Pagination
                    currentPage={currentPage}
                    totalPage={totalPage}
                    onPageChange={setCurrentPage}
                />
            </div>



        </div>
    );
}