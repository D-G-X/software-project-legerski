import type {AxiosRequestConfig} from "axios";
import axios from "axios";

let isRefreshing = false;
let queue: Array<{
  resolve: (token: string) => void;
  reject: (err: any) => void;
}> = [];

let getNewAccessToken: (() => Promise<string | null>) | null = null;
export const setRefreshHandler = (fn: () => Promise<string | null>) => {
  getNewAccessToken = fn;
};

export const setAccessTokenHeader = (token?: string | null) => {
  if (token) axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  else delete axios.defaults.headers.common["Authorization"];
};

const processQueue = (err: any, token?: string) => {
  queue.forEach((p) => (err ? p.reject(err) : p.resolve(token!)));
  queue = [];
};

axios.interceptors.response.use(
    (res) => res,
    async (error) => {
      const original = error.config as AxiosRequestConfig & { _retry?: boolean };
      if (error.response?.status === 401 && !original._retry) {
        original._retry = true;

        if (!getNewAccessToken) {
          return Promise.reject(error);
        }

        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            queue.push({
              resolve: (token) => {
                original.headers = {
                  ...original.headers,
                  Authorization: `Bearer ${token}`,
                };
                resolve(axios(original));
              },
              reject,
            });
          });
        }

        isRefreshing = true;
        try {
          const token = await getNewAccessToken();
          if (!token) throw new Error("Refresh failed");
          processQueue(null, token);
          original.headers = {
            ...original.headers,
            Authorization: `Bearer ${token}`,
          };
          return axios(original);
        } catch (refreshErr) {
          processQueue(refreshErr, undefined);
          return Promise.reject(refreshErr);
        } finally {
          isRefreshing = false;
        }
      }
      return Promise.reject(error);
    }
);
