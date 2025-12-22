import React from "react";
import ApplicationDocumentUpload from "./document-upload";

test("should create", () => {
  const { container } = renderWithRouter(<ApplicationDocumentUpload />);

  expect(container).toBeTruthy();
});
