import React from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router";

export default function ContactPage() {
    const { t } = useTranslation("contact");
    const navigate = useNavigate();

    return (
        <div className="min-h-screen bg-white text-gray-800 flex flex-col">

            {/* MAIN CONTENT */}
            <main className="flex-1 max-w-3xl mx-auto px-6 py-10">

                {/* Title */}
                <h1 className="text-2xl font-bold mb-2">{t("title")}</h1>
                <p className="text-gray-600 mb-8">{t("subtitle")}</p>

                {/* EMAIL SUPPORT */}
                <div className="mb-6">
                    <div className="font-semibold">📧 {t("email.title")}</div>
                    <a
                        href="mailto:avisio.legal@palma.es"
                        className="text-blue-600 underline block mt-1"
                    >
                        avisio.legal@palma.es
                    </a>
                </div>

                {/* PHONE SUPPORT */}
                <div className="mb-6">
                    <div className="font-semibold">📞 {t("phone.title")}</div>
                    <p className="text-gray-700 mt-1">
                        +34 971 123 456 <span className="text-sm text-gray-500">({t("phone.hours")})</span>
                    </p>
                </div>

                {/* OFFICE ADDRESS (with pin icon + multiline) */}
                <div className="mb-10">
                    <div className="flex items-start gap-2">
                        <span className="text-lg">📍</span>
                        <div>
                            <span className="font-semibold">{t("address.title")}</span>{" "}
                            <div className="text-gray-700 leading-relaxed mt-1">
                                Plaça de Cort 1,<br />
                                07001 Palma de Mallorca,<br />
                                Illes Balears, Spain
                            </div>
                        </div>
                    </div>
                </div>

                {/* Button LINKS */}
                <button
                    onClick={() => navigate("/")}
                    // className= "text-white px-3 py-1 rounded-lg shadow hover:bg-purple-300 mb-1"
                    // style={{ backgroundColor: "#351341" }}
                    className="bg-[#351341] text-white px-6 py-3 rounded-lg shadow hover:bg-[#4B1A58] mx-auto block"
                >
                    {t("back")}
                </button>


            </main>
            {/* Footer */}
            <footer className="border-t text-center text-sm text-gray-500 py-4">
                <a href="/legalnotice" className="hover:underline">{t("legalNotice")}</a>
            </footer>
        </div>
    );
}
