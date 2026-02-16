import React from "react";
import ApplicationDetails from "./applicationDetails";

test("should create", () => {
  const { container } = renderWithRouter(
    <ApplicationDetails
      open={false}
      applicationData={undefined}
      userData={undefined}
      onClose={jest.fn()}
    />,
  );

  expect(container).toBeTruthy();
});
