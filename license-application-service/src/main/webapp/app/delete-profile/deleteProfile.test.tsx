import React from "react";
import DeleteProfile from "./deleteProfile";

test("should create", () => {
  const {container} = renderWithRouter(<DeleteProfile/>);

  expect(container).toBeTruthy();
});
