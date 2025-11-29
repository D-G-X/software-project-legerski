import React from "react";
import PaymentConfirm from "./paymentConfirm";

test("should create", () => {
  const { container } = renderWithRouter(<PaymentConfirm />);
  expect(container).toBeTruthy();
});

test("should render title", () => {
  const { container } = renderWithRouter(<PaymentConfirm />);
  expect(container).toBeTruthy();
});
