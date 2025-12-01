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
import legalNoticeEnglish from './locales/legalNoticeEnglish.json';
import legalNoticeSpanish from './locales/legalNoticeSpanish.json';
import contactEnglish from './locales/contactEnglish.json';
import contactSpanish from './locales/contactSpanish.json';
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

    if (storedAccess) auth?.setAccessToken(storedAccess);
    if (storedRefresh) auth?.setRefreshToken(storedRefresh);
  }, [auth]);

  return <AppRoutes />;
};

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: english, legalNotice: legalNoticeEnglish, contact: contactEnglish },
    fr: { translation: french },
    de: { translation: german },
    hi: { translation: hindi },
    id: { translation: indonesian },
    es: { translation: spanish, legalNotice: legalNoticeSpanish, contact: contactSpanish },
  },
  lng: localStorage.getItem("language") || "en",
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
});

axios.defaults.baseURL = process.env.API_PATH;

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
