import React from "react";
import { createBrowserRouter, RouterProvider } from "react-router";
import App from "./app";
import Home from "./home/home";
import Error from "./error/error";
import Login from "./login/login";
import Register from "./register/register";
import PaymentForm from "./payment/paymentForm";
import PaymentConfirm from "./payment/paymentConfirm";

export default function AppRoutes() {
  const router = createBrowserRouter([
    {
      element: <App />,
      children: [
        { path: "", element: <Home /> },
        { path: "login", element: <Login /> },
        { path: "register", element: <Register /> },
        { path: "payment/:id", element: <PaymentForm /> },
        { path: "payment/:id/done", element: <PaymentConfirm data={{
            id: 123456789,
            application_id: 123456,
            amount: 420.00,
            name: "Peter Heusch",
            iban: "DE01 1234 5678 9012 3456 78",
            bic: "PEHEDEFFXXX",
            payment_date: new Date().toISOString(),
            payment_status: "UNPAID"
        }} /> },
        { path: "error", element: <Error /> },
        { path: "*", element: <Error /> },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}
