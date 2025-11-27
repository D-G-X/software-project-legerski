import React from "react";
import { createBrowserRouter, RouterProvider } from "react-router";
import App from "./app";
import Home from "./home/home";
import Error from "./error/error";
import Login from "./login/login";
import Register from "./register/register";
import RequestApplication from "./license/request-application/request-application";
import Payment from "./payment/payment";

export default function AppRoutes() {
  const router = createBrowserRouter([
    {
      element: <App />,
      children: [
        { path: "", element: <Home /> },
        { path: "login", element: <Login /> },
        { path: "register", element: <Register /> },
        {
          path: "license-application-request",
          element: <RequestApplication />,
        },
        { path: "payment", element: <Payment /> },
        { path: "error", element: <Error /> },
        { path: "*", element: <Error /> },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}
