import React from "react";
import Login from "./login";

test("should create", () => {
  const { container } = renderWithRouter(<Login />);

  expect(container).toBeTruthy();
});
