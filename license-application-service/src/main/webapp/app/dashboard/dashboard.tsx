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
            <div className="relative min-h-[calc(100vh-4rem)] bg-white justify-center mt-8">
            <h2 className="font-bold text-center mb-6">{t("dashboard.headline")}</h2>
            <button
                type="button"
                className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg mb-6 w-max block m-auto">
                {t("dashboard.button-text")}
            </button>
            <h1 className="font-bold">{t("dashboard.title")}</h1>
            </div>
        </div>
    );
}