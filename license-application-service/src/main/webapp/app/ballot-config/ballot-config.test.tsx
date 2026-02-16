import React from "react";
import BallotConfig from "./ballot-config";

test("should create", () => {
  const {container} = renderWithRouter(<BallotConfig/>);

  expect(container).toBeTruthy();
});
