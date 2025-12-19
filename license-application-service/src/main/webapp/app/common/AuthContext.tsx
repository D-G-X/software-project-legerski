// src/app/context/AuthContext.tsx
import React, { createContext, useState, ReactNode } from "react";
interface AuthContextType {
  role: string | null;
  accessToken: string | null;
  refreshToken: string | null;
  accessTokenExpiry: number | null;
  refreshTokenExpiry: number | null | undefined;
  tokenType: string | null;
  setAccessToken: (token: string | null) => void;
  setRefreshToken: (token: string | null) => void;
  setAccessTokenExpiry: (token: number | null) => void;
  setRefreshTokenExpiry: (token: number | null | undefined) => void;
  setTokenType: (token: string | null) => void;
  setRole: (token: string | null) => void;
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
  const [accessTokenExpiry, setAccessTokenExpiry] = useState<number | null>(
    null
  );
  const [refreshTokenExpiry, setRefreshTokenExpiry] = useState<
    number | null | undefined
  >(null);
  const [tokenType, setTokenType] = useState<string | null>(null);
  const [role, setRole] = useState<string | null>(null);

  const signOut = () => {
    setAccessToken(null);
    setRefreshToken(null);
    setAccessTokenExpiry(null);
    setRefreshTokenExpiry(null);
    setTokenType(null);
    setRole(null);
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("accessTokenExpiry");
    localStorage.removeItem("refreshTokenExpiry");
    localStorage.removeItem("tokenType");
    localStorage.removeItem("role");
    window.location.href = "/login"; // redirect to login page
  };

  return (
    <AuthContext.Provider
      value={{
        accessToken,
        refreshToken,
        accessTokenExpiry,
        refreshTokenExpiry,
        tokenType,
        role,
        setAccessToken,
        setRefreshToken,
        signOut,
        setAccessTokenExpiry,
        setRefreshTokenExpiry,
        setTokenType,
        setRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
