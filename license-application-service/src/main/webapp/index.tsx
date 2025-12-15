import React, { useContext, useEffect } from "react";
import ReactDOM from "react-dom/client";
import { initReactI18next } from "react-i18next";
import i18n from "i18next";
import axios from "axios";
import english from "./locales/english.json";
import french from "./locales/french.json";
import german from "./locales/german.json";
import hindi from "./locales/hindi.json";
import indonesian from "./locales/indonesian.json";
import spanish from "./locales/spanish.json";
import legalNoticeEnglish from "./locales/legalNoticeEnglish.json";
import legalNoticeFrench from "./locales/legalNoticeFrench.json";
import legalNoticeGerman from "./locales/legalNoticeGerman.json";
import legalNoticeHindi from "./locales/legalNoticeHindi.json";
import legalNoticeIndonesian from "./locales/legalNoticeIndonesian.json";
import legalNoticeSpanish from "./locales/legalNoticeSpanish.json";
import contactEnglish from "./locales/contactEnglish.json";
import contactFrench from "./locales/contactFrench.json";
import contactGerman from "./locales/contactGerman.json";
import contactHindi from "./locales/contactHindi.json";
import contactIndonesian from "./locales/contactIndonesian.json";
import contactSpanish from "./locales/contactSpanish.json";
import AppRoutes from "./app/routes";
import { AuthContext, AuthProvider } from "./app/common/AuthContext";
import "./index.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

const queryClient = new QueryClient();

const AppInitializer = () => {
  const auth = useContext(AuthContext);

  useEffect(() => {
    const storedAccess = localStorage.getItem("accessToken");
    const storedRefresh = localStorage.getItem("refreshToken");
    const storedAccessRefreshToken = localStorage.getItem("accessTokenExpiry");
    const storedRefreshToken = localStorage.getItem("refreshTokenExpiry");
    const storedTokenType = localStorage.getItem("tokenType");

    if (storedAccess) auth?.setAccessToken(storedAccess);
    if (storedRefresh) auth?.setRefreshToken(storedRefresh);
    if (storedRefresh)
      auth?.setAccessTokenExpiry(Number(storedAccessRefreshToken));
    if (storedRefresh) auth?.setRefreshTokenExpiry(Number(storedRefreshToken));
    if (storedRefresh) auth?.setTokenType(storedTokenType);
  }, [auth]);

  return <AppRoutes />;
};

i18n.use(initReactI18next).init({
  resources: {
    en: {
      translation: english,
      legalNotice: legalNoticeEnglish,
      contact: contactEnglish,
    },
    fr: {
      translation: french,
      legalNotice: legalNoticeFrench,
      contact: contactFrench,
    },
    de: {
      translation: german,
      legalNotice: legalNoticeGerman,
      contact: contactGerman,
    },
    hi: {
      translation: hindi,
      legalNotice: legalNoticeHindi,
      contact: contactHindi,
    },
    id: {
      translation: indonesian,
      legalNotice: legalNoticeIndonesian,
      contact: contactIndonesian,
    },
    es: {
      translation: spanish,
      legalNotice: legalNoticeSpanish,
      contact: contactSpanish,
    },
  },
  lng: localStorage.getItem("language") || "en",
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
});

axios.defaults.baseURL = "http://localhost:8080";

const root = document.getElementById("root")!!;

ReactDOM.createRoot(root).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <AppInitializer />
      </AuthProvider>
    </QueryClientProvider>
  </React.StrictMode>
);
