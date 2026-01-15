import {redirect} from "react-router";

export const ProtectedLoader = (allowedRoles?: string[]) => {
  return () => {
    const accessToken = localStorage.getItem("accessToken");
    const role = localStorage.getItem("role"); // single role

    if (!accessToken) {
      return redirect("/login"); // redirect if not logged in
    }

    if (
        allowedRoles &&
        allowedRoles.length > 0 &&
        !allowedRoles.includes(role ?? "")
    ) {
      return redirect("/error"); // redirect if role not allowed
    }

    return null; // allow rendering
  };
};
