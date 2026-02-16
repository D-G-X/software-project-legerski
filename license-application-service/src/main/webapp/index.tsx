import React, {useContext, useEffect} from "react";
import ReactDOM from "react-dom/client";
import {initReactI18next} from "react-i18next";
import i18n from "i18next";
import axios from "axios";
import english from "./locales/english.json";
import french from "./locales/french.json";
import german from "./locales/german.json";
import hindi from "./locales/hindi.json";
import indonesian from "./locales/indonesian.json";
import spanish from "./locales/spanish.json";
import AppRoutes from "./app/routes";
import {AuthContext, AuthProvider} from "./app/common/auth/AuthContext";
import "./index.css";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {GlobalLoaderProvider} from "app/common/GlobalLoader";

const queryClient = new QueryClient();

const AppInitializer = () => {
  const auth = useContext(AuthContext);

  useEffect(() => {
    const storedAccess = localStorage.getItem("accessToken");
    const storedRefresh = localStorage.getItem("refreshToken");
    const storedAccessRefreshToken = localStorage.getItem("accessTokenExpiry");
    const storedRefreshToken = localStorage.getItem("refreshTokenExpiry");
    const storedTokenType = localStorage.getItem("tokenType");
    const storedRole = localStorage.getItem("role");

    if (storedAccess) auth?.setAccessToken(storedAccess);
    if (storedRefresh) auth?.setRefreshToken(storedRefresh);
    if (storedRefresh)
      auth?.setAccessTokenExpiry(Number(storedAccessRefreshToken));
    if (storedRefresh) auth?.setRefreshTokenExpiry(Number(storedRefreshToken));
    if (storedRefresh) auth?.setTokenType(storedTokenType);
    if (storedRole) auth?.setRole(storedRole);
  }, [auth]);

  return <AppRoutes/>;
};

i18n.use(initReactI18next).init({
  resources: {
    en: {
      translation: english,
    },
    fr: {
      translation: french,
    },
    de: {
      translation: german,
    },
    hi: {
      translation: hindi,
    },
    id: {
      translation: indonesian,
    },
    es: {
      translation: spanish,
    },
  },
  lng: localStorage.getItem("language") || "en",
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
});

axios.defaults.baseURL = "http://localhost:8080"

const root = document.getElementById("root")!!;

ReactDOM.createRoot(root).render(
    <React.StrictMode>
      <GlobalLoaderProvider>
        <QueryClientProvider client={queryClient}>
          <AuthProvider>
            <AppInitializer/>
          </AuthProvider>
        </QueryClientProvider>
      </GlobalLoaderProvider>
    </React.StrictMode>
);
