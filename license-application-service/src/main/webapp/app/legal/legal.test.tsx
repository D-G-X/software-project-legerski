import React from "react";
import LegalNotice from "./legal";

test("should create", () => {
  const {container} = renderWithRouter(<LegalNotice/>);

  expect(container).toBeTruthy();
});
