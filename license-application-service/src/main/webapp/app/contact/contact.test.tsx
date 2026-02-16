import React from "react";
import ContactPage from "./contact";

test("should create", () => {
  const {container} = renderWithRouter(<ContactPage/>);

  expect(container).toBeTruthy();
});
