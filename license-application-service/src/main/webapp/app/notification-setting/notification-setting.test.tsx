import React from "react";
import NotificationSettings from "./notification-setting";

test("should create", () => {
  const { container } = renderWithRouter(<NotificationSettings />);

  expect(container).toBeTruthy();
});
