import React from "react";
import ApplicationDetails from "./applicationDetails";

test("should create", () => {
  const { container } = renderWithRouter(<ApplicationDetails open={false} entry={null} onClose={function(): void {
      throw new Error("Function not implemented.");
  } } />);
  expect(container).toBeTruthy();
});

test("should render title", () => {
  const { container } = renderWithRouter(<ApplicationDetails open={false} entry={null} onClose={function(): void {
      throw new Error("Function not implemented.");
  } } />);
  expect(container).toBeTruthy();
});
