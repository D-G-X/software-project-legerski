import React from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import "./home.css";

export default function Home() {
  const { t } = useTranslation();
  useDocumentTitle(t("home.index.headline"));

  return (
    <main className="relative h-screen w-full overflow-hidden">
      <div
        className="absolute inset-0 bg-cover bg-top bg-no-repeat blur-[2px] opacity-80 scale-105"
        style={{ backgroundImage: "url('/images/background.jpg')" }}
      ></div>

      <div className="relative min-h-[calc(100vh-4rem)] flex items-center justify-center">
        <h1 className="xl:text-[16rem] font-extrabold text-center text-mallorca-purple z-10 tracking-widest">
          {t("home.index.headline")}
        </h1>
      </div>
    </main>
  );
}
