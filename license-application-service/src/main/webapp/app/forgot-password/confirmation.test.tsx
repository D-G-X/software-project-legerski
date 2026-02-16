import React from "react";
import {ResetConfirmation} from "./confirmation";

test("should create", () => {
  const {container} = renderWithRouter(<ResetConfirmation/>);

  expect(container).toBeTruthy();
});
