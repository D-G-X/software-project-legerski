import React from "react";
import ForgotPasswordRequest from "./request";

test("should create", () => {
  const { container } = renderWithRouter(<ForgotPasswordRequest />);

  expect(container).toBeTruthy();
});
