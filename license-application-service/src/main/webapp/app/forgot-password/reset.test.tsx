import React from "react";
import ResetPassword from "./reset";

test("should create", () => {
  const { container } = renderWithRouter(<ResetPassword />);

  expect(container).toBeTruthy();
});
