import React from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router";
import { FormHeader } from "../common/headingTitle";
import { Mail, MapPin, Phone } from "lucide-react";

export default function ContactPage() {
  const { t } = useTranslation("contact");
  const navigate = useNavigate();

  return (
    <div className="container mx-auto px-4 md:px-6 min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center font-inter">
      <div>
        {/* Title */}
        <FormHeader heading={t("title")} subHeading={t("subtitle")} />

        {/* EMAIL SUPPORT */}
        <div className="my-8">
          <div className="font-semibold flex items-center gap-2 text-mallorca-purple">
            <Mail className="w-5 h-5" />
            {t("email.title")}
            <a
              href="mailto:avisio.legal@palma.es"
              className="text-blue-600 underline block mt-1"
            >
              avisio.legal@palma.es
            </a>
          </div>
        </div>

        {/* PHONE SUPPORT */}
        <div className="mb-6">
          <div className="font-semibold flex items-center gap-2 text-mallorca-purple">
            <Phone className="w-5 h-5" />
            {t("phone.title")}
            <p className="text-gray-700 ">
              +34 971 123 456{" "}
              <span className="text-sm text-gray-500">
                ({t("phone.hours")})
              </span>
            </p>
          </div>
        </div>

        {/* OFFICE ADDRESS */}
        <div className="mb-10">
          <div className="flex items-start gap-2 text-mallorca-purple">
            <MapPin className="w-5 h-5 mt-1" />
            <div>
              <span className="font-semibold">{t("address.title")}</span>
              <div className="text-gray-700 leading-relaxed mt-1">
                Plaça de Cort 1,
                <br />
                07001 Palma de Mallorca,
                <br />
                Illes Balears, Spain
              </div>
            </div>
          </div>
        </div>

        {/* Button LINKS */}
        <button
          onClick={() => navigate("/")}
          className="bg-mallorca-purple text-white px-4 py-1 rounded-lg shadow hover:bg-[#4B1A58] mx-auto block"
        >
          {t("back")}
        </button>
      </div>
    </div>
  );
}
