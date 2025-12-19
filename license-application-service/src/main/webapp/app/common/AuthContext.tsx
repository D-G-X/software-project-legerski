import { refreshLogin } from "app/services/authentication/authentication";
import React, { createContext, useState, ReactNode, useEffect } from "react";
import { setAccessTokenHeader, setRefreshHandler } from "./axios-config";
interface AuthContextType {
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
  signOut: () => void;
  refreshAccessToken: () => void;
  redirectToHome?: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined
);

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

  const signOut = () => {
    setAccessToken(null);
    setRefreshToken(null);
    setAccessTokenExpiry(null);
    setRefreshTokenExpiry(null);
    setTokenType(null);
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("accessTokenExpiry");
    localStorage.removeItem("refreshTokenExpiry");
    localStorage.removeItem("tokenType");
    window.location.href = "/login"; // redirect to login page
  };

  const refreshAccessToken = async () => {
    if (!refreshToken) return null;
    try {
      const resp = await refreshLogin({ refresh_token: refreshToken });
      const newToken = resp.data.access_token;
      setAccessToken(newToken);
      setAccessTokenHeader(newToken);
      return newToken;
    } catch {
      signOut();
      return null;
    }
  };

  useEffect(() => {
    setRefreshHandler(refreshAccessToken);
  }, [refreshToken]);

  return (
    <AuthContext.Provider
      value={{
        accessToken,
        refreshToken,
        accessTokenExpiry,
        refreshTokenExpiry,
        tokenType,
        setAccessToken,
        setRefreshToken,
        signOut,
        setAccessTokenExpiry,
        setRefreshTokenExpiry,
        setTokenType,
        refreshAccessToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
