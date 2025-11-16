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
        className="absolute inset-0 bg-center bg-cover bg-no-repeat opacity-80 scale-110"
        style={{
          backgroundImage: "url('/images/background.png')",
          backgroundPosition: "center bottom",
        }}
      ></div>

      <div className="relative min-h-[calc(100vh-4rem)] flex items-center justify-center">
        <h1 className="responsive-stroke text-7xl sm:text-8xl md:text-9xl xl:text-[15.5rem] font-extrabold text-center text-mallorca-purple z-10 tracking-widest font-inter">
          {t("home.index.headline")}
        </h1>
      </div>
    </main>
  );
}
