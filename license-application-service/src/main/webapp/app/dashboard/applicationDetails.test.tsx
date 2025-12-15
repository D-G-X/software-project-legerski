import React from "react";
import ApplicationDetails from "./applicationDetails";

test("should create", () => {
  const { container } = renderWithRouter(
      <ApplicationDetails
          open={false}
          applicationData={null}
          onClose={jest.fn()}
          onRenew={jest.fn()}
      />
  );

  expect(container).toBeTruthy();
});

test("should render title when open", () => {
  const { getByText } = renderWithRouter(
      <ApplicationDetails
          open={true}
          applicationData={{ id: 1 } as any}
          onClose={jest.fn()}
          onRenew={jest.fn()}
      />
  );

  expect(getByText(/application details/i)).toBeInTheDocument();
});