import React from "react";
import { createBrowserRouter, RouterProvider } from "react-router";
import App from "./app";
import Home from "./home/home";
import Error from "./error/error";
import Login from "./login/login";
import Register from "./register/register";
import PaymentForm from "./payment/paymentForm";
import PaymentConfirm from "./payment/paymentConfirm";
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
import { ProtectedLoader } from "./common/ProtectedLoader";

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
          path: "notification-settings",
          element: <NotificationSettings />,
          loader: ProtectedLoader(["dev", "admin", "user"]),
        },
        {
          path: "license-application-request",
          element: <RequestApplication />,
          loader: ProtectedLoader(["dev", "user"]),
        },
        {
          path: "license-document-upload/:id",
          element: <ApplicationDocumentUpload />,
          loader: ProtectedLoader(["dev", "user"]),
        },
        {
          path: "payment/:id",
          element: <PaymentForm />,
          loader: ProtectedLoader(["dev", "user"]),
        },
        {
          path: "/payment/:id/done",
          element: <PaymentConfirm />,
          loader: ProtectedLoader(["dev", "user"]),
        },
        {
          path: "profile",
          element: <Profile />,
          loader: ProtectedLoader(["dev", "user"]),
        },
        {
          path: "deleteProfile",
          element: <DeleteProfile />,
          loader: ProtectedLoader(["dev", "user"]),
        },

        // Admin-only routes
        {
          path: "admin-dashboard",
          element: <AdminDashboard />,
          loader: ProtectedLoader(["dev", "admin"]),
        },
        {
          path: "ballot-dashboard",
          element: <BallotDashboard />,
          loader: ProtectedLoader(["dev", "admin"]),
        },
        {
          path: "ballot-details",
          element: <BallotDetails />,
          loader: ProtectedLoader(["dev", "admin"]),
        },
        {
          path: "notification-settings",
          element: <NotificationSettings />,
          loader: ProtectedLoader(["dev", "admin"]),
        },
        {
          path: "license-application-request",
          element: <RequestApplication />,
        },
        {
          path: "license-document-upload/:id",
          element: <ApplicationDocumentUpload />,
        },
        { path: "*", element: <Error /> },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}
