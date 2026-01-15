import React from "react";
import BallotApplications from "./ballot-applications";

test("should create", () => {
  const {container} = renderWithRouter(<BallotApplications/>);

  expect(container).toBeTruthy();
});
