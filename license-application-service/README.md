# License Application Service

## Project Setup

### Prerequisites

- [Docker Engine](https://docs.docker.com/engine/) version 20.10 or higher and [Docker Compose](https://docs.docker.com/compose/) version 1.29 or higher are required.
- [Java JDK](https://www.oracle.com/de/java/) version 21 or higher is required.
- [Node.js](https://nodejs.org/) version 22 or higher is required.

### Quick Start

1. Have Docker running.
2. When prompted: "Maven build script found `license-application-service`", accept by clicking "Load".
3. Run `init.sh` (Linux/macOS) or `init.ps1` (Windows) in `license-application-service`. 
4. Launch the Maven setup config: `Setup Database and API` which performs the following steps:

   - `docker compose up` to start the database,
   - `mvn liquibase:update` to apply database changelogs,
   - `mvn compile` to generate OpenAPI and jOOQ classes.

   > ⚠️ If this step fails, run `docker compose up` manually in the `license-application-service` directory and re-run the Maven setup config.

5. Open the two freshly auto-generated modules:

   - `license-application-service/target/generated-sources/jooq`
   - `license-application-service/target/generated-sources/openapi`

   and set their `src` folders as "Generated Sources Root".

   > ℹ️ Right-click on the folder in the Project view -> "Mark Directory as" -> "Generated Sources Root".

6. Backend client secret setup:
   - Go to the [Keycloak admin console](http://localhost:8081/realms/master/protocol/openid-connect/auth?client_id=security-admin-console&redirect_uri=http%3A%2F%2Flocalhost%3A8081%2Fadmin%2Fmaster%2Fconsole%2F&state=5a67f871-4955-446e-b7a4-dcf04d1510fa&response_mode=query&response_type=code&scope=openid&nonce=bdd6a9a4-aaa7-40fa-a6dd-bd0c842c0789&code_challenge=1dG9jQi6II2K35M8kujnMSicF6yUOZfMQmZx-8BAIUI&code_challenge_method=S256) and log in with admin/admin credentials
   - Select the `license-realm` realm from the dropdown and navigate to the `Clients` tab
   - Select the `backend-api` client, go to the "Credentials" tab and copy the `Client Secret` value. If the secret is not visible, click on the `Regenerate` button to create a new one 
   - Paste it to the `KEYCLOAK_CLIENT_SECRET` key in the `.env` and `application.yml` file (copy and rename .env.example, if necessary).

   > Note that the keycloak dashboard doesn't work on Safari!

7. Set up document validator user in Keycloak:
   - Go to the [Keycloak admin console](http://localhost:8081/realms/master/protocol/openid-connect/auth?client_id=security-admin-console&redirect_uri=http%3A%2F%2Flocalhost%3A8081%2Fadmin%2Fmaster%2Fconsole%2F&state=5a67f871-4955-446e-b7a4-dcf04d1510fa&response_mode=query&response_type=code&scope=openid&nonce=bdd6a9a4-aaa7-40fa-a6dd-bd0c842c0789&code_challenge=1dG9jQi6II2K35M8kujnMSicF6yUOZfMQmZx-8BAIUI&code_challenge_method=S256) and log in with admin/admin credentials
   - Select the `license-realm` realm from the dropdown and navigate to the `Users` tab
   - Click on `Add User` and create a new user with username `documentvalidator@xx.xx` and first/last name of your choice
   - Set the password `securepassword123` in the `Credentials` tab (make sure to disable the `Temporary` option)
   - In the `Role Mappings` tab, click on `Assign role` and select the filter `filter by relam roles`. Select and assign the `user` role from the list.

8. Set up an admin user in Keycloak:
   - Go to the [Keycloak admin console](http://localhost:8081/realms/master/protocol/openid-connect/auth?client_id=security-admin-console&redirect_uri=http%3A%2F%2Flocalhost%3A8081%2Fadmin%2Fmaster%2Fconsole%2F&state=5a67f871-4955-446e-b7a4-dcf04d1510fa&response_mode=query&response_type=code&scope=openid&nonce=bdd6a9a4-aaa7-40fa-a6dd-bd0c842c0789&code_challenge=1dG9jQi6II2K35M8kujnMSicF6yUOZfMQmZx-8BAIUI&code_challenge_method=S256) and log in with admin/admin credentials
   - Select the `license-realm` realm from the dropdown and navigate to the `Users` tab
   - Click on `Add User` and create a new user with username and first/last name of your choice (make sure that the email is valid for the frontend login)
   - Set a password of your choice in the `Credentials` tab (make sure to disable the `Temporary` option and that the password is valid for the frontend login)
   - In the `Role Mappings` tab, click on `Assign role` and select the filter `filter by relam roles`. Select and assign the `admin` role from the list.

### Development

1. Launch the Spring Boot configuration: `Dev LicenseApplicationService`.
2. Launch the npm configuration: `npm dev`.

   > ℹ️ All npm configurations automatically perform `npm install` before building or testing the frontend.

### Manual Production Build (optional)

1. Launch the Spring Boot configuration: `Prod LicenseApplicationService`.
2. Launch the npm configuration: `npm prod`.

### Manual Testing (optional)

1. _TODO: Add JUnit test instructions here_
2. Launch the npm configuration: `npm test`.

### Clean-Up (only after dependency updates)

1. Stop the Spring Boot **and** npm applications if running.
2. Run `clean.sh` (Linux/macOS) or `clean.ps1` (Windows) in `license-application-service`.

This removes outdated npm and maven packages.

---

### Quick Fixes

If you have issues with compiling or starting the backend, try the following steps in IntelliJ:

	•	Settings/Preferences → Build, Execution, Deployment → Build Tools → Maven
	•	Runner → “Delegate IDE build/run actions to Maven”

(or depending on your IntelliJ-Version: “Use Maven for build and run”)

By doing this, IntelliJ will use Maven to build and run the project instead of its internal build process.

---

## API Documentation

- #### [Swagger API](http://localhost:8080/swagger-ui/index.html)
- #### [OpenAPI Docs](http://localhost:8080/v3/api-docs)

## Further readings

- [Maven docs](https://maven.apache.org/guides/index.html)
- [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Learn React](https://react.dev/learn)
- [Webpack concepts](https://webpack.js.org/concepts/)
- [npm docs](https://docs.npmjs.com/)
- [Tailwind CSS](https://tailwindcss.com/)

---

# Auth Context Documentation

This document explains the implementation and usage of the `AuthContext`
used for managing authentication state (access token, refresh token,
logout, and JWT decoding). It also includes examples of extracting user
information from JWT tokens.

## 🚀 Features

- Global authentication state using **React Context**
- Persists tokens using **localStorage**
- Provides `signOut()` for logout + redirect
- Works with Axios and React Query
- Includes helper method for decoding JWT tokens
- Easy consumption via `useContext`

---

# 📌 AuthContext Overview

### **`AuthContext.tsx`**

```tsx
import React, { createContext, useState, ReactNode } from "react";

interface AuthContextType {
  accessToken: string | null;
  refreshToken: string | null;
  setAccessToken: (token: string | null) => void;
  setRefreshToken: (token: string | null) => void;
  signOut: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined
);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);

  const signOut = () => {
    setAccessToken(null);
    setRefreshToken(null);
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/login";
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
```

---

# 📌 Initializing Tokens on App Startup

```tsx
const AppInitializer = () => {
  const auth = useContext(AuthContext);

  useEffect(() => {
    const storedAccess = localStorage.getItem("accessToken");
    const storedRefresh = localStorage.getItem("refreshToken");

    if (storedAccess) auth?.setAccessToken(storedAccess);
    if (storedRefresh) auth?.setRefreshToken(storedRefresh);
  }, [auth]);

  return <AppRoutes />;
};
```

---

# 🎯 Using AuthContext in a Component

```tsx
import { useContext } from "react";
import { AuthContext } from "../common/AuthContext";

const Dashboard = () => {
  const auth = useContext(AuthContext);

  return (
    <div>
      <h2>Dashboard</h2>

      <p>Access Token: {auth?.accessToken}</p>

      <button onClick={auth?.signOut}>Sign Out</button>
    </div>
  );
};

export default Dashboard;
```

---

# 🔐 Decoding JWT Token

### **`authTokenDecode.ts`**

```ts
import { jwtDecode } from "jwt-decode";

export function getUserIdFromToken(token?: string | null) {
  if (!token) return null;

  try {
    const decoded: any = jwtDecode(token);
    return decoded.sub ?? null;
  } catch {
    return null;
  }
}
```

---

# 🧪 Example: Getting User ID After Login

```tsx
const handleLogin = async () => {
  const response = await axios.post("/auth/login", {
    email,
    password,
  });

  const accessToken = response.data.accessToken;
  const refreshToken = response.data.refreshToken;

  auth?.setAccessToken(accessToken);
  auth?.setRefreshToken(refreshToken);

  localStorage.setItem("accessToken", accessToken);
  localStorage.setItem("refreshToken", refreshToken);

  const userId = getUserIdFromToken(accessToken);
  console.log("Logged-in user ID:", userId);
};
```

---

# 🧩 Example: Reading User ID Anywhere in App

```tsx
import { useContext } from "react";
import { AuthContext } from "../common/AuthContext";
import { getUserIdFromToken } from "../../utils/authTokenDecode";

const RequestApplication = () => {
  const auth = useContext(AuthContext);

  const userID = getUserIdFromToken(auth?.accessToken);

  return (
    <div>
      <p>User ID: {userID}</p>
    </div>
  );
};

export default RequestApplication;
```

---

# ✔️ Summary

Your Auth system now:

- Stores tokens globally\
- Syncs tokens with localStorage\
- Decodes JWT to extract user information\
- Provides a clean, centralized auth structure\
- Works well with React Query & Axios

---
