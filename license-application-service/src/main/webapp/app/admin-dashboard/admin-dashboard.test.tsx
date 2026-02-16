import React from "react";
import AdminDashboard from "./admin-dashboard";

test("should create", () => {
  const {container} = renderWithRouter(<AdminDashboard/>);

  expect(container).toBeTruthy();
});
