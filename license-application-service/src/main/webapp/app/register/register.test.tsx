import React from "react";
import Register from "./register";

test("should create", () => {
  const { container } = renderWithRouter(<Register />);
  expect(container).toBeTruthy();
});

test("should render title", () => {
  const { container } = renderWithRouter(<Register />);
  expect(container).toBeTruthy();
});
