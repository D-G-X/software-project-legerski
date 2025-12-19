import React from "react";
import BallotDashboard from "./ballot-dashboard";

test("should create", () => {
  const { container } = renderWithRouter(<BallotDashboard />);

  expect(container).toBeTruthy();
});
