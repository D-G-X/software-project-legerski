import React from "react";
import { createBrowserRouter, RouterProvider } from "react-router";
import App from "./app";
import Home from "./home/home";
import Error from "./error/error";
import Login from "./login/login";
import Register from "./register/register";
import RequestApplication from "./license/request-application/request-application";
import ForgotPasswordRequest from "./forgot-password/request";
import ResetPassword from "./forgot-password/reset";
import Payment from "./payment/payment";
import ApplicationDocumentUpload from "./license/document-upload/document-upload";
import LegalNotice from "./legal/legal";
import ContactPage from "./contact/contact";
import BallotDetails from "./ballot-details/ballot-details";
import AdminDashboard from "./admin-dashboard/admin-dashboard";
import BallotDashboard from "./ballot-dashboard/ballot-dashboard";
import NotificationSettings from "./notification-setting/notification-setting";
import ProtectedRoute from "./common/ProtectedLoader";

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

        // Post login
        {
          path: "notification-settings",
          element: (
            <ProtectedRoute>
              <NotificationSettings />
            </ProtectedRoute>
          ),
        },
        {
          path: "license-application-request",
          element: (
            <ProtectedRoute>
              <RequestApplication />
            </ProtectedRoute>
          ),
        },
        {
          path: "license-document-upload/:id",
          element: (
            <ProtectedRoute>
              <ApplicationDocumentUpload />
            </ProtectedRoute>
          ),
        },
        {
          path: "payment/:id",
          element: (
            <ProtectedRoute>
              <Payment />
            </ProtectedRoute>
          ),
        },

        // only Admin
        {
          path: "admin-dashboard",
          element: (
            <ProtectedRoute allowedRoles={["admin"]}>
              <AdminDashboard />
            </ProtectedRoute>
          ),
        },
        {
          path: "ballot-dashboard",
          element: (
            <ProtectedRoute allowedRoles={["admin"]}>
              <BallotDashboard />
            </ProtectedRoute>
          ),
        },
        {
          path: "ballot-details",
          element: (
            <ProtectedRoute allowedRoles={["admin"]}>
              <BallotDetails />
            </ProtectedRoute>
          ),
        },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}
