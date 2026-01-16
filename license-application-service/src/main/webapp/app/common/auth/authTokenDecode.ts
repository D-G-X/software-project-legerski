import {jwtDecode} from "jwt-decode";

export function getUserIdFromToken(token?: string | null) {
  if (!token) return null;

  try {
    const decoded: any = jwtDecode(token);
    return decoded.sub ?? null;
  } catch {
    return null;
  }
}
