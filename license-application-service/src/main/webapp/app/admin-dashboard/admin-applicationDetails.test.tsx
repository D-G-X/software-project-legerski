import React from "react";
import AdminApplicationDetails from "./admin-applicationDetails";

test("should create", () => {
  const { container } = renderWithRouter(
    <AdminApplicationDetails
      open={false}
      applicationData={null}
      onClose={jest.fn()}
    />
  );

  expect(container).toBeTruthy();
});

// test("should render title when open", () => {
//   const { getByText } = renderWithRouter(
//     <AdminApplicationDetails
//       open={true}
//       applicationData={{ id: 1 } as any}
//       onClose={jest.fn()}
//     />
//   );

//   expect(getByText(/application details/i)).toBeInTheDocument();
// });
