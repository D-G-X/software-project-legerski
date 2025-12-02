import React from "react";
import {useTranslation} from "react-i18next";
import {MessageSquare} from "lucide-react";

export default function NotificationDropup({className = ""}) {
  const {t} = useTranslation();

  return (
      <div className={`relative ${className}`}>
        {/* Button */}
        <button
            className="h-full w-full rounded-full"
        >
          <div className="flex items-center gap-2 h-full px-3">
            <span className="text-md">{t("nav.notificationBtn")}</span>
            <MessageSquare size={20} />
          </div>

          {123 > 0 && (
              <span className="absolute -top-2 -right-2 bg-red-600 text-white text-xs px-2 py-0.5 rounded-full">
          {123}
        </span>
          )}
        </button>

        {/* TODO Dropup */}
      </div>
  );
}