import React from "react";
import RequestApplication from "./request-application";

test("should create", () => {
  const { container } = renderWithRouter(<RequestApplication />);

  expect(container).toBeTruthy();
});
