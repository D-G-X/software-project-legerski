import React, { useContext } from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import { AuthContext } from "app/common/AuthContext";
import { Dashboard } from "app/dashboard/dashboard";
import BallotDashboard from "app/ballot-dashboard/ballot-dashboard";

export default function Home() {
  const { t } = useTranslation();
  const auth = useContext(AuthContext);

  useDocumentTitle(t("home.index.headline"));

  return auth?.accessToken ? (
    auth.role === "admin" ? (
      <BallotDashboard></BallotDashboard>
    ) : (
      <Dashboard />
    )
  ) : (
    <main className="relative w-full overflow-hidden">
      <div
        className="absolute inset-0 bg-center bg-cover bg-no-repeat opacity-80 scale-110 -z-10"
        style={{
          backgroundImage: "url('/images/background.webp')",
          backgroundPosition: "center bottom",
        }}
      />

      <div className="relative min-h-[calc(100vh-8rem)] flex items-center justify-center">
        <h1
          className="
            text-stroke-1
            sm:text-stroke-3
            lg:text-stroke-5
            text-7xl
            sm:text-8xl
            md:text-9xl
            xl:text-[15.5rem]
            font-extrabold
            text-center
            text-mallorca-purple
            tracking-widest
            font-inter
            select-none
            z-10
          "
        >
          {t("home.index.headline")}
        </h1>
      </div>
    </main>
  );
}
