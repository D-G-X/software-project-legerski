import React from "react";
import { createBrowserRouter, RouterProvider } from "react-router";
import App from "./app";
import Home from "./home/home";
import Error from "./error/error";
import Login from "./login/login";
import Register from "./register/register";
import ForgotPasswordRequest from "./forgot-password/request";
import ResetPassword from "./forgot-password/reset";
import Payment from "./payment/payment";
import LegalNotice from "./legal/LegalNotice";
import ContactPage from "./contact/ContactPage";

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
        { path: "payment", element: <Payment /> },
        { path: 'legal', element: <LegalNotice /> },
        { path: 'contact', element: <ContactPage /> },
        { path: "error", element: <Error /> },
        { path: "*", element: <Error /> },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}
