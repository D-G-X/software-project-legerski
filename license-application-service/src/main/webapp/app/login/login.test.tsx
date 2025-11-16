import React from "react";
import Login from "./login";

test("should create", () => {
  const { container } = renderWithRouter(<Login />);
  expect(container).toBeTruthy();
});

test("should render title", () => {
  const { container } = renderWithRouter(<Login />);
  expect(container).toBeTruthy();
});
