import React from "react";
import {Dashboard} from "./dashboard";

test("should create", () => {
  const { container } = renderWithRouter(<Dashboard />);
  expect(container).toBeTruthy();
});

test("should render title", () => {
  const { container } = renderWithRouter(<Dashboard />);
  expect(container).toBeTruthy();
});
