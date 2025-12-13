import React, {useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {Dot, MessageSquare, MessageSquareDashed, MessageSquareOff, Settings} from "lucide-react";
import {useNavigate} from "react-router";
import {formatDate, formatRelativeDate} from "./format";

type Notification = {
  applicationId: number;
  date: string;
  message: string;
  read: boolean;
};

export default function NotificationDropup({className = ""}) {
  const {t} = useTranslation();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([
    {applicationId: 1, date: "2025-02-12T10:15:00Z", message: "New message received", read: false},
    {applicationId: 2, date: "2025-12-12T10:15:00Z", message: "Application approved", read: false},
    {
      applicationId: 3,
      date: "2025-12-13T19:05:00Z",
      message: "Reminder: Upload missing document",
      read: true
    },
  ]);

  const dropupRef = useRef<HTMLDivElement>(null); // render null first

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAsRead = (id: number) => {
    setNotifications((prev) =>
        prev.map((n) => (n.applicationId === id ? {...n, read: true} : n))
    );
  };

  const markAllAsRead = () => {
    setNotifications(prev =>
        prev.map(n => n.read ? n : {...n, read: true})
    );
  };

  function handleSettingsClick() {
    navigate(`/notification-settings`);
    setOpen(false)
  }

  const [openNotification, setOpenNotification] = useState<Notification | null>(null);

  const openModal = (n: Notification) => {
    setOpenNotification(n);
  };

  // Close on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropupRef.current && !dropupRef.current.contains(e.target as Node)) {
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
              <MessageSquare size={20}/>
            </div>

            {unreadCount > 0 && (
                <span
                    className="absolute -top-2 -right-2 bg-red-600 text-white text-xs px-2 py-0.5 rounded-full">
                {unreadCount}
              </span>
            )}
          </button>

          {/* Dropup */}
          {open && (
              <div
                  ref={dropupRef}
                  className="absolute bottom-full mb-4 left-0 px-2 w-lg bg-white shadow-xl rounded-lg border border-gray-200 z-50"
              >
                <div className="mt-2 flex items-center justify-between px-2">
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

                    <button
                        type="button"
                        className="relative inline-flex items-center group"
                    >
                      <MessageSquareOff size={20}/>
                      <span className="pointer-events-none absolute bottom-full left-1/2 -translate-x-1/2
                         mb-2 whitespace-nowrap rounded bg-black px-2 py-1 text-xs text-white
                         opacity-0 group-hover:opacity-100 group-focus-visible:opacity-100 transition z-50">
                      {t("notifications.deleteAllBtn")}
                    </span>
                    </button>
                  </div>
                </div>

                <hr className="mt-4 border-gray-800"/>

                <ul className="max-h-lg mt-1 my-3 overflow-y-auto divide-y">
                  {notifications.map((n) => (
                      <li
                          key={n.applicationId}
                          onClick={(e) => {
                            markAsRead(n.applicationId)
                            e.stopPropagation();
                            openModal(n);
                          }}

                          className="cursor-pointer px-4 py-2 hover:bg-gray-100 transition rounded-xl"
                      >
                        <div className="flex flex-col">
                          {/* Message */}
                          <div
                              className={`flex items-center gap-2 ${
                                  n.read
                                      ? "text-gray-400 font-normal"
                                      : "text-mallorca-purple font-bold"
                              }`}
                          >
                            {!n.read && <Dot className="w-5 h-5 shrink-0"/>}
                            <span className="text-sm">
                              {t("notifications.messageLabel")} {n.applicationId}
                            </span>
                          </div>

                          {/* Date */}
                          <span className="text-xs text-gray-400 mb-1">
                            {formatRelativeDate(n.date, t("locale"))}
                          </span>
                        </div>
                      </li>
                  ))}
                </ul>
              </div>
          )}
        </div>
        {openNotification && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
              <div className="bg-white rounded-xl p-6 w-full max-w-md">
                <h2 className="text-lg font-bold text-mallorca-purple text-center mb-4">
                  {t("notifications.popup.title")}
                </h2>

                <div className="space-y-2 text-sm">
                  <div>
                    <span className="font-medium">{t("notifications.popup.id") + ": "}</span>{openNotification.applicationId}
                  </div>

                  <div>
                    <span className="font-medium">
                      {t("notifications.messageLabel")}:
                    </span>{" "}
                    {openNotification.message}
                  </div>

                  <div>
                    <span className="font-medium">{t("notifications.date")}:</span>{" "}
                    {formatDate(openNotification.date)}
                  </div>
                </div>

                <div className="mt-6 flex justify-end">
                  <button
                      onClick={() => setOpenNotification(null)}
                      className="px-4 py-2 rounded-md bg-mallorca-purple text-white"
                  >
                    {t("notifications.popup.close")}
                  </button>
                </div>
              </div>
            </div>
        )}
      </>
  );
}