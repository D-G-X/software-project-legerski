import React, {useState} from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import "./ballot-applications-dashboard.css";
import Pagination from "../common/Pagination";
import { Link, useParams } from "react-router";
// import { useListApplications } from "app/services/applications/applications";
// import { ApplicationResource } from "../../types";

export default function BallotApplicationsDashboard() {
    const { ballotId } = useParams();
    const { t } = useTranslation();
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 5;
    useDocumentTitle(t("home.index.headline"));
    // 1. New State for Filtering
    const [statusFilter, setStatusFilter] = useState("All");
    // 1. States for Search and Status Filter
    const [searchTerm, setSearchTerm] = useState("");

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

    // function handleNewApplicationClick() {
    //     navigate(`/login`);
    // }
    console.log("Ballot id:" + ballotId);
    const localData = [
        {
            "id": 1,
            "ballot_id": "1",
            "name": "Ahmed",
            "address": "Home",
            "phone_number": "111",
            "email": "ahmed@email.com",
            "country": "Australia",
            "application_status": "SUBMITTED"
        },
        {
            "id": 2,
            "ballot_id": "1",
            "name": "Sarah",
            "address": "Work",
            "phone_number": "222",
            "email": "sarah@email.com",
            "country": "USA",
            "application_status": "SUBMITTED"
        },
        {
            "id": 3,
            "ballot_id": "1",
            "name": "Kenji",
            "address": "Apartment 1B",
            "phone_number": "333",
            "email": "kenji@email.com",
            "country": "Japan",
            "application_status": "APPROVED"
        },
        {
            "id": 4,
            "ballot_id": "1",
            "name": "Maria",
            "address": "Unit 20",
            "phone_number": "444",
            "email": "maria@email.com",
            "country": "Mexico",
            "application_status": "SUBMITTED"
        },
        {
            "id": 5,
            "ballot_id": "1",
            "name": "David",
            "address": "Cottage",
            "phone_number": "555",
            "email": "david@email.com",
            "country": "Canada",
            "application_status": "REJECTED"
        },
        {
            "id": 6,
            "ballot_id": "1",
            "name": "Fatima",
            "address": "Farmhouse",
            "phone_number": "666",
            "email": "fatima@email.com",
            "country": "UAE",
            "application_status": "SUBMITTED"
        },
        {
            "id": 7,
            "ballot_id": "1",
            "name": "Liam",
            "address": "Dorm 3A",
            "phone_number": "777",
            "email": "liam@email.com",
            "country": "Ireland",
            "application_status": "APPROVED"
        },
        {
            "id": 8,
            "ballot_id": "1",
            "name": "Sofia",
            "address": "City Tower",
            "phone_number": "888",
            "email": "sofia@email.com",
            "country": "Brazil",
            "application_status": "SUBMITTED"
        },
        {
            "id": 9,
            "ballot_id": "1",
            "name": "Javier",
            "address": "Suburbia",
            "phone_number": "999",
            "email": "javier@email.com",
            "country": "Spain",
            "application_status": "REJECTED"
        },
        {
            "id": 10,
            "ballot_id": "1",
            "name": "Chloe",
            "address": "Penthouse",
            "phone_number": "000",
            "email": "chloe@email.com",
            "country": "France",
            "application_status": "APPROVED"
        },
        {
            "id": 11,
            "ballot_id": "1",
            "name": "Wei",
            "address": "No. 1 Street",
            "phone_number": "101",
            "email": "wei@email.com",
            "country": "China",
            "application_status": "SUBMITTED"
        },
        {
            "id": 12,
            "ballot_id": "1",
            "name": "Hans",
            "address": "Main Road 5",
            "phone_number": "102",
            "email": "hans@email.com",
            "country": "Germany",
            "application_status": "PENDING"
        },
        {
            "id": 13,
            "ballot_id": "1",
            "name": "Aisha",
            "address": "The Coast",
            "phone_number": "103",
            "email": "aisha@email.com",
            "country": "Nigeria",
            "application_status": "APPROVED"
        },
        {
            "id": 5,
            "ballot_id": "1",
            "name": "David",
            "address": "Cottage",
            "phone_number": "555",
            "email": "david@email.com",
            "country": "Canada",
            "application_status": "REJECTED"
        },
        {
            "id": 6,
            "ballot_id": "1",
            "name": "Fatima",
            "address": "Farmhouse",
            "phone_number": "666",
            "email": "fatima@email.com",
            "country": "UAE",
            "application_status": "SUBMITTED"
        },
        {
            "id": 7,
            "ballot_id": "1",
            "name": "Liam",
            "address": "Dorm 3A",
            "phone_number": "777",
            "email": "liam@email.com",
            "country": "Ireland",
            "application_status": "APPROVED"
        },
        {
            "id": 8,
            "ballot_id": "1",
            "name": "Sofia",
            "address": "City Tower",
            "phone_number": "888",
            "email": "sofia@email.com",
            "country": "Brazil",
            "application_status": "SUBMITTED"
        },
        {
            "id": 9,
            "ballot_id": "1",
            "name": "Javier",
            "address": "Suburbia",
            "phone_number": "999",
            "email": "javier@email.com",
            "country": "Spain",
            "application_status": "REJECTED"
        },
        {
            "id": 10,
            "ballot_id": "1",
            "name": "Chloe",
            "address": "Penthouse",
            "phone_number": "000",
            "email": "chloe@email.com",
            "country": "France",
            "application_status": "APPROVED"
        },
        {
            "id": 11,
            "ballot_id": "1",
            "name": "Wei",
            "address": "No. 1 Street",
            "phone_number": "101",
            "email": "wei@email.com",
            "country": "China",
            "application_status": "SUBMITTED"
        },
        {
            "id": 12,
            "ballot_id": "1",
            "name": "Hans",
            "address": "Main Road 5",
            "phone_number": "102",
            "email": "hans@email.com",
            "country": "Germany",
            "application_status": "PENDING"
        },
        {
            "id": 13,
            "ballot_id": "1",
            "name": "Aisha",
            "address": "The Coast",
            "phone_number": "103",
            "email": "aisha@email.com",
            "country": "Nigeria",
            "application_status": "APPROVED"
        }
    ];

    const ballotApplications = localData.filter(item => item.ballot_id === ballotId); // Filtering applications based on ballot id

    // 2. Combined Filter Logic
    // This checks both the search input AND the dropdown status
    const filteredBallotApplications = ballotApplications.filter((ballotApplication) => {
        const matchesSearch = ballotApplication.address
            .toLowerCase()
            .includes(searchTerm.toLowerCase());

        const matchesStatus = statusFilter === "All" || ballotApplication.application_status === statusFilter;

        return matchesSearch && matchesStatus;
    });



    const totalPage = Math.ceil(filteredBallotApplications.length / itemsPerPage);

    const currentData = filteredBallotApplications.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
    );

    // Handlers
    const handleSearchChange = (e: { target: { value: React.SetStateAction<string>; }; }) => {
        setSearchTerm(e.target.value);
        setCurrentPage(1); // Reset to first page on search
    };

    const handleFilterChange = (e: { target: { value: React.SetStateAction<string>; }; }) => {
        setStatusFilter(e.target.value);
        setCurrentPage(1);
    };

    return (
        <div className="container mx-auto px-4 md:px-6">
            <div className="relative min-h-[calc(100vh-8rem)] bg-white flex justify-center">
                <div className="font-inter flex flex-col w-full mt-4">
                    <h1 className="text-3xl font-bold text-mallorca-purple text-left">{t("ballot-applications-dashboard.headline.ballot")} {ballotId} {t("ballot-applications-dashboard.headline.applications")}</h1>
                    <div className={"flex justify-between my-4"}>
                        <Link to={"/login"} className={"text-green-500"}>{t("admin-dashboard.links.active_requests")}</Link>
                        <Link to={"/ballot-dashboard"} className={"text-blue-500 underline"}>{t("admin-dashboard.links.ballot_management")}</Link>
                    </div>
                    {/* 4. Search and Filter UI Bar */}
                    <div className="flex flex-wrap items-center justify-between">
                        {/* Search Input */}
                        <div className="relative flex-grow max-w-sm">
                            <input
                                type="text"
                                placeholder={t("Search by address...")}
                                value={searchTerm}
                                onChange={handleSearchChange}
                                className="w-full border border-gray-300 rounded-md px-4 py-2 text-sm focus:ring-2 focus:ring-mallorca-purple outline-none"
                            />
                        </div>
                        {/* 4. Dropdown UI */}
                        <div className="flex items-center gap-2">
                            <label className="text-sm font-medium">{t("Filter by Status")}:</label>
                            <select
                                value={statusFilter}
                                onChange={handleFilterChange}
                                className="border border-gray-300 rounded px-3 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-mallorca-purple w-100"
                            >
                                <option value="All">All</option>
                                <option value="SUBMITTED">Submitted</option>
                                <option value="REJECTED">Rejected</option>
                                <option value="UPCOMING">Upcoming</option>
                                <option value="APPROVED">Approved</option>
                                <option value="PENDING">Pending</option>
                            </select>
                        </div>
                    </div>

                    {currentData.length > 0 && (
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

                                    {currentData.map((item) => (
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
                                    numberOfItems={ballotApplications.length}
                                />
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}