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
import BallotDetails from "./ballotDetails/ballotDetails";


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
        {
          path: "license-application-request",
          element: <RequestApplication />,
        },
        {
          path: "license-document-upload/:id",
          element: <ApplicationDocumentUpload />,
        },
        { path: "payment/:id", element: <Payment /> },
        { path: "legal", element: <LegalNotice /> },
        { path: "contact", element: <ContactPage /> },
        { path: "error", element: <Error /> },
        { path: "ballot-details", element: <BallotDetails />},
        { path: "*", element: <Error /> },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}
