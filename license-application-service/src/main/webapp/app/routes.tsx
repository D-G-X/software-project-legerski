import React from "react";
import { createBrowserRouter, RouterProvider } from "react-router";
import App from "./app";
import Home from "./home/home";
import AppUserList from "./app-user/app-user-list";
import AppUserAdd from "./app-user/app-user-add";
import AppUserEdit from "./app-user/app-user-edit";
import ApplicationList from "./application/application-list";
import ApplicationAdd from "./application/application-add";
import ApplicationEdit from "./application/application-edit";
import LicenseList from "./license/license-list";
import LicenseAdd from "./license/license-add";
import LicenseEdit from "./license/license-edit";
import Error from "./error/error";
import ForgotPasswordRequest from "./forgotpassword/forgotpasswordrequest";
import RequestSent from "./forgotpassword/requestsent";
import ResetPassword from "./forgotpassword/resetpassword";
import ResetSuccess from "./forgotpassword/resetsuccess";
import Login from "./login/login";
import Register from "./register/register";

export default function AppRoutes() {
  const router = createBrowserRouter([
    {
      element: <App />,
      children: [
        { path: "", element: <Home /> },
        { path: "forgot-password", element: <ForgotPasswordRequest /> },
        { path: "forgot-password-sent", element: <RequestSent /> },
        { path: "forgot-reset-password", element: <ResetPassword /> },
        { path: "forgot-reset-success", element: <ResetSuccess /> },
        { path: "login", element: <Login /> },
        { path: "register", element: <Register /> },
        { path: "appUsers", element: <AppUserList /> },
        { path: "appUsers/add", element: <AppUserAdd /> },
        { path: "appUsers/edit/:id", element: <AppUserEdit /> },
        { path: "applications", element: <ApplicationList /> },
        { path: "applications/add", element: <ApplicationAdd /> },
        { path: "applications/edit/:id", element: <ApplicationEdit /> },
        { path: "licenses", element: <LicenseList /> },
        { path: "licenses/add", element: <LicenseAdd /> },
        { path: "licenses/edit/:id", element: <LicenseEdit /> },
        { path: "error", element: <Error /> },
        { path: "*", element: <Error /> },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}
