import React, { useContext, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  Dot,
  MessageSquare,
  MessageSquareDashed,
  Settings,
} from "lucide-react";
import { useNavigate } from "react-router";
import { formatDate, formatRelativeDate } from "./format";
import { UserNotificationResource } from "../../types";
import {
  useGetNotifications,
  useUpdateNotification,
} from "../services/users/users";
import { AxiosError } from "axios";
import { getUserIdFromToken } from "./authTokenDecode";
import { AuthContext } from "./AuthContext";

type NotificationsResult = {
  data: UserNotificationResource[] | null;
  isFound: boolean;
  isLoading: boolean;
};

type UpdateNotificationResult = {
  updateNotification: (id: string) => void;
  isLoading: boolean;
};

function useGetNotificationsData(userId: string): NotificationsResult {
  const { data: response, error, isLoading } = useGetNotifications(userId);

  const isFound = (error as AxiosError | undefined)?.response?.status !== 404;
  if (error && !isFound) {
    console.error("Error fetching notifications:", error);
  }
  return {
    data: response?.data ?? null,
    isFound,
    isLoading,
  };
}

function useUpdateNotificationStatus(): UpdateNotificationResult {
  const { mutate, isPending } = useUpdateNotification();

  return {
    updateNotification: (id: string) => mutate({ id }),
    isLoading: isPending,
  };
}

export default function NotificationPopup({ className = "" }) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const auth = useContext(AuthContext);
  const userId = getUserIdFromToken(auth?.accessToken);
  const { data } = useGetNotificationsData(userId);
  const [notifications, setNotifications] = useState<
    UserNotificationResource[]
  >([]);
  const { updateNotification } = useUpdateNotificationStatus();

  useEffect(() => {
    if (data) {
      setNotifications(data);
    }
  }, [data]);

  const popupRef = useRef<HTMLDivElement>(null); // render null first

  const unreadCount = notifications?.filter((n) => !n.is_read).length || 0;

  const markAsRead = (id?: string) => {
    if (!id) return;
    setNotifications((prev) =>
      prev ? prev.map((n) => (n.id === id ? { ...n, is_read: true } : n)) : prev
    );
    updateNotification(id);
  };

  const markAllAsRead = () => {
    const idsToUpdate = (notifications ?? []).filter(
      (n) => !n.is_read && typeof n.id === "string"
    );

    setNotifications((prev) =>
      prev ? prev.map((n) => (n.is_read ? n : { ...n, is_read: true })) : prev
    );

    if (!idsToUpdate?.length) return;

    (async () => {
      for (const id of idsToUpdate) {
        updateNotification(id.toString());
      }
    })();
  };

  function handleSettingsClick() {
    navigate(`/notification-settings`);
    setOpen(false);
  }

  const [currentNotification, setCurrentNotification] =
    useState<UserNotificationResource>({} as UserNotificationResource);

  const openModal = (n: UserNotificationResource) => {
    setCurrentNotification(n);
  };

  // Close on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (popupRef.current && !popupRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  return (
    <>
      <div className={`relative ${className}`}>
        {/* Button */}
        <button
          onClick={() => setOpen((o) => !o)}
          className="h-full w-full rounded-full"
        >
          <div className="flex items-center gap-2 h-full px-3">
            <span className="text-md">{t("nav.notificationBtn")}</span>
            <MessageSquare size={20} />
          </div>

          {unreadCount > 0 && (
            <span className="absolute -top-2 -right-2 bg-red-600 text-white text-xs px-2 py-0.5 rounded-full">
              {unreadCount}
            </span>
          )}
        </button>

        {/* Popup */}
        {open && (
          <div
            ref={popupRef}
            className="absolute bottom-full mb-2 left-0 px-2 w-lg bg-white shadow-xl rounded-lg border border-gray-200 z-50"
          >
            <div className="mt-4 flex items-center justify-between px-2">
              {/* Title – left */}
              <span className="text-xl font-bold text-mallorca-purple">
                {t("notifications.title")}
              </span>

              {/* Buttons – right */}
              <div className="flex items-center gap-4">
                <button
                  type="button"
                  onClick={handleSettingsClick}
                  className="relative inline-flex items-center group"
                >
                  <Settings size={20} />
                  <span
                    className="pointer-events-none absolute bottom-full left-1/2 -translate-x-1/2
                         mb-2 whitespace-nowrap rounded bg-black px-2 py-1 text-xs text-white
                         opacity-0 group-hover:opacity-100 group-focus-visible:opacity-100 transition z-50"
                  >
                    {t("notifications.settingsBtn")}
                  </span>
                </button>

                <button
                  type="button"
                  onClick={markAllAsRead}
                  className="relative inline-flex items-center group"
                >
                  <MessageSquareDashed size={20} />
                  <span
                    className="pointer-events-none absolute bottom-full left-1/2 -translate-x-1/2
                         mb-2 whitespace-nowrap rounded bg-black px-2 py-1 text-xs text-white
                         opacity-0 group-hover:opacity-100 group-focus-visible:opacity-100 transition z-50"
                  >
                    {t("notifications.markAllAsReadBtn")}
                  </span>
                </button>
              </div>
            </div>

            <hr className="mt-2 border-gray-800" />

            <ul className="max-h-lg mt-1 my-3 overflow-y-auto divide-y">
              {notifications.length != 0 ? (
                notifications?.map((n) => (
                  <li
                    key={n.id}
                    onClick={(e) => {
                      markAsRead(n.id);
                      e.stopPropagation();
                      openModal(n);
                    }}
                    className="cursor-pointer px-2 py-2 hover:bg-gray-100 transition rounded-xl"
                  >
                    <div className="flex justify-between items-center">
                      {/* Message */}
                      <div
                        className={`flex items-center gap-2 font-bold ${
                          n.is_read ? "text-gray-400" : "text-mallorca-purple"
                        }`}
                      >
                        <Dot
                          color={`${n.is_read ? "#ffffff50" : "#351341"}`}
                          className="w-5 h-5 shrink-0"
                        />
                        <span className="text-sm">
                          {t("notifications.messageLabel")} {n.application_id}
                        </span>
                      </div>

                      {/* Date */}
                      <span className="text-xs text-gray-400 mb-1">
                        {formatRelativeDate(n.date, t("locale"))}
                      </span>
                    </div>
                  </li>
                ))
              ) : (
                <div className="text-mallorca-purple/50 text-center py-5">
                  {t("nav.noNotification")}
                </div>
              )}
            </ul>
          </div>
        )}
      </div>
      {currentNotification.id && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-xl p-6 w-full max-w-md">
            <h2 className="text-xl font-bold text-mallorca-purple text-center mt-2">
              {t("notifications.popup.title")}
            </h2>

            <div className="space-y-2 text-md font-semibold mt-6">
              <div>
                <span className="font-medium">
                  {t("notifications.popup.id") + ": "}
                </span>
                {currentNotification.application_id}
              </div>

              <div className="mt-2">
                <span className="font-medium">
                  {currentNotification.message}
                </span>
              </div>

              <div className="flex justify-between items-center mt-6">
                <span className="font-medium text-gray-500 text-sm">
                  {formatDate(currentNotification.date, t)}
                </span>

                  {/* Buttons – right */}
                  <div className="flex items-center gap-4">
                    <button
                        type="button"
                        onClick={handleSettingsClick}
                        className="relative inline-flex items-center group"
                    >
                      <Settings size={20}/>
                      <span className="pointer-events-none absolute bottom-full left-1/2 -translate-x-1/2
                         mb-2 whitespace-nowrap rounded bg-black px-2 py-1 text-xs text-white
                         opacity-0 group-hover:opacity-100 group-focus-visible:opacity-100 transition z-50">
                      {t("notifications.settingsBtn")}
                    </span>
                    </button>

                    <button
                        type="button"
                        onClick={markAllAsRead}
                        className="relative inline-flex items-center group"
                    >
                      <MessageSquareDashed size={20}/>
                      <span className="pointer-events-none absolute bottom-full left-1/2 -translate-x-1/2
                         mb-2 whitespace-nowrap rounded bg-black px-2 py-1 text-xs text-white
                         opacity-0 group-hover:opacity-100 group-focus-visible:opacity-100 transition z-50">
                      {t("notifications.markAllAsReadBtn")}
                    </span>
                    </button>
                  </div>
                </div>

                <hr className="mt-2 border-gray-800"/>

                <ul className="max-h-lg mt-1 my-3 overflow-y-auto divide-y">
                  {notifications.length ?
                      <>
                        {notifications?.map((n) => (
                            <li
                                key={n.id}
                                onClick={(e) => {
                                  markAsRead(n.id);
                                  e.stopPropagation();
                                  openModal(n);
                                }}

                                className="cursor-pointer px-2 py-2 hover:bg-gray-100 transition rounded-xl"
                            >
                              <div className="flex justify-between items-center">
                                {/* Message */}
                                <div
                                    className={`flex items-center gap-2 font-bold ${
                                        n.is_read
                                            ? "text-gray-400"
                                            : "text-mallorca-purple"
                                    }`}
                                >
                                  <Dot color={`${n.is_read ? "#ffffff50" : "#351341"}`}
                                       className="w-5 h-5 shrink-0"/>
                                  <span className="text-sm">
                              {t("notifications.messageLabel")} {n.application_id}
                            </span>
                                </div>

                                {/* Date */}
                                <span className="text-xs text-gray-400 mb-1">
                            {formatRelativeDate(n.date, t("locale"))}
                          </span>
                              </div>
                            </li>
                        ))}
                      </>
                      :
                      <div className="p-4 text-center text-gray-500">
                        {t("notifications.noNotifications")}
                      </div>
                  }

                </ul>
              </div>
          )}
        </div>
        {currentNotification.id && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
              <div className="bg-white rounded-xl p-6 w-full max-w-md">
                <h2 className="text-xl font-bold text-mallorca-purple text-center mt-2">
                  {t("notifications.popup.title")}
                </h2>

                <div className="space-y-2 text-md font-semibold mt-6">
                  <div>
                    <span
                        className="font-medium">{t("notifications.popup.id") + ": "}</span>{currentNotification.application_id}
                  </div>

                  <div className="mt-2">
                    <span className="font-medium">
                      {currentNotification.message}
                    </span>
                  </div>

                  <div className="flex justify-between items-center mt-6">

                    <span className="font-medium text-gray-500 text-sm">
                      {formatDate(currentNotification.date, t)}
                    </span>

                    <button
                        onClick={() => setCurrentNotification({} as UserNotificationResource)}
                        className="px-4 py-2 rounded-md bg-mallorca-purple text-white"
                    >
                      {t("notifications.popup.close")}
                    </button>

                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
