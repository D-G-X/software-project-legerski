import React, {useState} from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import "./admin-dashboard.css";
import Pagination from "../common/Pagination";
import { Link, useNavigate } from "react-router";
import { useListApplications } from "app/services/applications/applications";
import { ApplicationResource } from "../../types";

export default function AdminDashboard() {

    const { t } = useTranslation();
    const navigate = useNavigate();
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 5;
    useDocumentTitle(t("home.index.headline"));

    const { data: response } = useListApplications({
        user_id: "f28d1d3b-9bcb-4a74-a65a-2fede2b0a6c3",
    });

    const applications: ApplicationResource[] = Array.isArray(response?.data)
        ? response.data
        : [];
    const totalPage = Math.ceil(applications.length / itemsPerPage);

    // const currentData = applications.slice(
    //     (currentPage - 1) * itemsPerPage,
    //     currentPage * itemsPerPage
    // );

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
        navigate(`/login`);
    }

    const localData = [
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 1,
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "me@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },

    ];


    return (
        <div className="container mx-auto px-4 md:px-6">
            <div className="relative min-h-[calc(100vh-8rem)] bg-white flex justify-center">
                <div className="font-inter flex flex-col w-full mt-4">
                    <h1 className="text-3xl font-bold text-mallorca-purple text-left">{t("admin-dashboard.headline")}</h1>
                    <div className={"flex justify-between mt-2"}>
                        <Link to={"/login"} className={"text-green-500"}>{t("admin-dashboard.links.active_requests")}</Link>
                        <button onClick={handleNewApplicationClick} className={"text-blue-500 underline"}>{t("admin-dashboard.links.ballot_management")}</button>
                    </div>

                    {/*<button*/}
                    {/*    type="button"*/}
                    {/*    className="bg-mallorca-purple text-white px-10 py-2 rounded-md font-medium text-lg mt-12 w-max"*/}
                    {/*    onClick={handleNewApplicationClick}*/}
                    {/*>*/}
                    {/*    {t("dashboard.buttonLabel")}*/}
                    {/*</button>*/}

                    {localData.length > 0 && (
                        <div className="mt-16 w-full">
                            <h1 className="font-bold text-xl text-mallorca-purple">
                                {t("dashboard.title")}
                            </h1>

                            <div className="overflow-x-auto">
                                <table className="mt-8 text-lg dashboard-table text-mallorca-purple">
                                    <tr>
                                        <th>{t("admin-dashboard.table.requester_name")}</th>
                                        <th>{t("admin-dashboard.table.address")}</th>
                                        <th>{t("admin-dashboard.table.phone_number")}</th>
                                        <th>{t("admin-dashboard.table.email")}</th>
                                        <th>{t("admin-dashboard.table.country")}</th>
                                        <th>{t("admin-dashboard.table.status")}</th>
                                    </tr>

                                    {localData.map((item) => (
                                        <tr key={item.id}>
                                            <td>{item.name}</td>
                                            <td>{item.address}</td>
                                            <td>{formatDate(item.phone_number)}</td>
                                            <td>{item.email}</td>
                                            <td>{item.country}</td>
                                            <td>
                        <span
                            className={`inline-flex items-center justify-center text-center p-1 px-4 min-w-[14rem] rounded-md ${
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
                                        </tr>
                                    ))}
                                </table>

                                <div className="my-8" />

                                <Pagination
                                    currentPage={currentPage}
                                    totalPage={totalPage}
                                    onPageChange={setCurrentPage}
                                    start={(currentPage - 1) * itemsPerPage + 1}
                                    end={currentPage * itemsPerPage}
                                    numberOfItems={localData.length}
                                />
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}