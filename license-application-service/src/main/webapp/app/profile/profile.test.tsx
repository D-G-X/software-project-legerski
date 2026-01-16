import React from "react";
import Profile from "./profile";

test("should create", () => {
  const {container} = renderWithRouter(<Profile/>);

  expect(container).toBeTruthy();
});
