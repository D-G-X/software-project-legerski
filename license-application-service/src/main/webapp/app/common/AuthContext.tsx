// src/app/context/AuthContext.tsx
import React, { createContext, useState, ReactNode } from "react";
interface AuthContextType {
  accessToken: string | null;
  refreshToken: string | null;
  setAccessToken: (token: string | null) => void;
  setRefreshToken: (token: string | null) => void;
  signOut: () => void;
  redirectToHome?: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined
);

// AuthContext.tsx
export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);

  const signOut = () => {
    setAccessToken(null);
    setRefreshToken(null);
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/login"; // redirect to login page
  };

  return (
    <AuthContext.Provider
      value={{
        accessToken,
        refreshToken,
        setAccessToken,
        setRefreshToken,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
