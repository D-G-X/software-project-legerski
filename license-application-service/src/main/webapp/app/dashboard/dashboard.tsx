import React from "react";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import "./dashboard.css";
// import useDocumentTitle from "app/common/use-document-title";

export default function Dashboard(){

    const { t } = useTranslation();
    useDocumentTitle(t("home.index.headline"));

    return (
        <div className="container mx-auto px-4 md:px-6">
            <div className="relative min-h-[calc(100vh-4rem)] bg-white flex justify-center">
            <h2>{t("dashboard.headline")}</h2>
            <button
                type="button"
                className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg">
                {t("dashboard.button-text")}
            </button>
            <h1>{t("dashboard.title")}</h1>
            </div>
        </div>
    );
}