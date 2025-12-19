import React from "react";
import BallotDetails from "./ballot-details";

test("should create", () => {
  const { container } = renderWithRouter(<BallotDetails />);

  expect(container).toBeTruthy();
});
