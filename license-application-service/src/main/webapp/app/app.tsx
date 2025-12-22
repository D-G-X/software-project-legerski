import React from "react";
import { Outlet, useLocation } from "react-router";
import Header from "app/common/header";
import Footer from "./common/footer";
import ErrorBoundary from "app/error/error-boundary";

/**
 * Provide the app layout and some general functionality.
 */
export default function App() {
  const { state } = useLocation();
  const msgSuccess = state?.msgSuccess || null;
  const msgInfo = state?.msgInfo || null;
  const msgError = state?.msgError || null;

  return (
    <>
      <Header />

      <div>
        {msgSuccess && (
          <p
            className="bg-green-200 border-green-800 text-green-800 border rounded p-4 mb-6"
            role="alert"
          >
            {msgSuccess}
          </p>
        )}
        {msgInfo && (
          <p
            className="bg-blue-200 border-blue-800 text-blue-800 border rounded p-4 mb-6"
            role="alert"
          >
            {msgInfo}
          </p>
        )}
        {msgError && (
          <p
            className="bg-red-200 border-red-800 text-red-800 border rounded p-4 mb-6"
            role="alert"
          >
            {msgError}
          </p>
        )}
        <ErrorBoundary>
          <Outlet />
        </ErrorBoundary>
      </div>
      <Footer />
    </>
  );
}
