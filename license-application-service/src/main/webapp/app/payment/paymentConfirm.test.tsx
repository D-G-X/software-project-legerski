import React from "react";
import PaymentConfirm from "./paymentConfirm";

test("should create", () => {
  const { container } = renderWithRouter(<PaymentConfirm data={{
    id: 123456789,
    application_id: 123456,
    amount: 123.45,
    name: "Peter Heusch",
    iban: "DE01123456789012345678",
    bic: "PEHEDEFFXXX",
    payment_date: new Date().toISOString(),
    payment_status: "UNPAID"
  }} />);
  expect(container).toBeTruthy();
});

test("should render title", () => {
  const { container } = renderWithRouter(<PaymentConfirm data={{
    id: 123456789,
    application_id: 123456,
    amount: 123.45,
    name: "Peter Heusch",
    iban: "DE01123456789012345678",
    bic: "PEHEDEFFXXX",
    payment_date: new Date().toISOString(),
    payment_status: "UNPAID"
  }} />);
  expect(container).toBeTruthy();
});
