import React from "react";
import { createBrowserRouter, RouterProvider } from "react-router";
import App from "./app";
import Home from "./home/home";
import Error from "./error/error";
import Login from "./login/login";
import Register from "./register/register";
import PaymentForm from "./license/payment/paymentForm";
import PaymentConfirm from "./license/payment/paymentConfirm";
import ForgotPasswordRequest from "./forgot-password/request";
import ResetPassword from "./forgot-password/reset";
import RequestApplication from "./license/request-application/request-application";
import ApplicationDocumentUpload from "./license/document-upload/document-upload";
import Profile from "./profile/profile";
import DeleteProfile from "./delete-profile/deleteProfile";
import LegalNotice from "./legal/legal";
import ContactPage from "./contact/contact";
import BallotDetails from "./ballot-details/ballot-details";
import AdminDashboard from "./admin-dashboard/admin-dashboard";
import BallotDashboard from "./ballot-dashboard/ballot-dashboard";
import NotificationSettings from "./notification-setting/notification-setting";
import IssuedLicensePage from "./issued-license/issued-license";
import { ProtectedLoader } from "./common/ProtectedLoader";
import BallotConfig from "./ballot-config/ballot-config";

export default function AppRoutes() {
  const router = createBrowserRouter([
    {
      element: <App />,
      children: [
        { path: "", element: <Home /> },
        { path: "forgot-password", element: <ForgotPasswordRequest /> },
        { path: "reset-password", element: <ResetPassword /> },
        { path: "login", element: <Login /> },
        { path: "register", element: <Register /> },
        { path: "legal", element: <LegalNotice /> },
        { path: "contact", element: <ContactPage /> },
        { path: "error", element: <Error /> },
        { path: "*", element: <Error /> },

        // Post login (any user)
        {
          path: "issued-license",
          element: <IssuedLicensePage />,
          loader: ProtectedLoader(["admin", "user"]),
        },
        {
          path: "notification-settings",
          element: <NotificationSettings />,
          loader: ProtectedLoader(["admin", "user"]),
        },
        {
          path: "license-application-request/:type/:id",
          element: <RequestApplication />,
          loader: ProtectedLoader(["user","admin"]),
        },
        {
          path: "license-application-request",
          element: <RequestApplication />,
          loader: ProtectedLoader(["user","admin"]),
        },
        {
          path: "license-document-upload/:id",
          element: <ApplicationDocumentUpload />,
          loader: ProtectedLoader(["user","admin"]),
        },
        {
          path: "payment/:id",
          element: <PaymentForm />,
          loader: ProtectedLoader(["user","admin"]),
        },
        {
          path: "payment/:id/done",
          element: <PaymentConfirm />,
          loader: ProtectedLoader(["user","admin"]),
        },
        {
          element: <Profile />,
          loader: ProtectedLoader(["user", "admin"]),
        },
        {
          path: "deleteProfile",
          element: <DeleteProfile />,
          loader: ProtectedLoader(["user","admin"]),
        },

        // Admin-only routes
        {
          path: "admin-dashboard",
          element: <AdminDashboard />,
          loader: ProtectedLoader(["admin"]),
        },
        {
          path: "ballot-config",
          element: <BallotConfig />,
          loader: ProtectedLoader(["admin"]),
        },
        {
          path: "ballot-dashboard",
          element: <BallotDashboard />,
          loader: ProtectedLoader(["admin"]),
        },
        {
          path: "ballot-details",
          element: <BallotDetails />,
          loader: ProtectedLoader(["admin"]),
        },
        {
          path: "notification-settings",
          element: <NotificationSettings />,
          loader: ProtectedLoader(["admin"]),
        },
        {
          path: "license-application-request",
          element: <RequestApplication />,
        },
        {
          path: "license-document-upload/:id",
          element: <ApplicationDocumentUpload />,
        },
        {
          path: "deleteProfile",
          element: <DeleteProfile />,
          loader: ProtectedLoader(["user", "admin"]),
        },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}
