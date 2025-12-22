import React from "react";
import PaymentForm from "./paymentForm";

test("should create", () => {
  const { container } = renderWithRouter(<PaymentForm />);
  expect(container).toBeTruthy();
});

test("should render title", () => {
  const { container } = renderWithRouter(<PaymentForm />);
  expect(container).toBeTruthy();
});
