import React, {useState} from "react";
import {useTranslation} from "react-i18next";

export default function NotificationSettings() {
  const {t} = useTranslation();
  const [showSubmitChanges, setShowSubmitChanges] = useState(false);
  const [form, setForm] = useState({
    app_notif_type: false,
    app_notif_freq: false,
    app_notif_lic_reminder: false,
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let {id, value: rawValue, name} = e.target;
    let value: string | boolean = rawValue;
    const key = id || name;

    if (key === "app_notif_type") {
      value = !form.app_notif_type;
    }

    if (key === "app_notif_freq") {
      value = !form.app_notif_freq;
    }

    if (key === "app_notif_lic_reminder") {
      value = !form.app_notif_lic_reminder;
    }

    setForm((prev) => ({
      ...prev,
      [key]: value,
    }));

    setShowSubmitChanges(true);
  };

  const fetchOrResetChanges = () => {
    setShowSubmitChanges(false);

    // Call fetch API to fetch new values
    setForm({
      app_notif_type: false,
      app_notif_freq: false,
      app_notif_lic_reminder: false,
    });
  };

  const handleSubmit = () => {
    // validations to check if the values are boolean
    if (
        typeof form.app_notif_type !== "boolean" ||
        typeof form.app_notif_freq !== "boolean" ||
        typeof form.app_notif_lic_reminder !== "boolean"
    ) {
      alert(t("notificationSettings.alerts.clientError"));
      window.location.reload();
      return;
    }

    alert(t("notificationSettings.alerts.apiPlaceholder"));
  };

  return (
      <div className="pb-5">
        <h2 className="text-center text-2xl font-semibold mt-16 mb-8">
          {t("notificationSettings.description")}
        </h2>

        <div className="mx-3 flex flex-col items-center gap-2">
          <div className="w-full max-w-xl justify-between">
            {/* Notification Preference */}
            <div className="flex justify-between text-mallorca-purple text-lg">
              <div className="pb-4">
                {t("notificationSettings.sections.appUpdates.label")}
              </div>
              <span className="text-mallorca-purple ml-5">
          <input
              id="app_notif_type"
              name="app_notif_type"
              type="checkbox"
              checked={form.app_notif_type}
              onChange={handleChange}
              className="checked:bg-mallorca-purple p-2 rounded mr-2"
          />
        </span>
            </div>

            {/* Application Status Updates */}
            <div className="flex justify-between text-mallorca-purple text-lg">
              <div className="pb-4">
                {t("notificationSettings.sections.appStatus.label")}
              </div>
              <span className="text-mallorca-purple ml-5">
          <input
              id="app_notif_freq"
              name="app_notif_freq"
              type="checkbox"
              checked={form.app_notif_freq}
              onChange={handleChange}
              className="checked:bg-mallorca-purple p-2 rounded mr-2"
          />
        </span>
            </div>

            {/* License Renewal Updates */}
            <div className="flex justify-between text-mallorca-purple text-lg">
              <div className="pb-4">
                {t("notificationSettings.sections.license.label")}
              </div>
              <span className="text-mallorca-purple ml-5">
          <input
              id="app_notif_lic_reminder"
              name="app_notif_lic_reminder"
              type="checkbox"
              checked={form.app_notif_lic_reminder}
              onChange={handleChange}
              className="checked:bg-mallorca-purple p-2 rounded mr-2"
          />
        </span>
            </div>
          </div>
        </div>

        <div
            className={`pt-5 ${showSubmitChanges ? "flex" : "invisible"} justify-between px-5`}
        >
          <button
              onClick={handleSubmit}
              className="px-15 py-1 border-2 border-mallorca-purple bg-mallorca-purple rounded-lg text-white"
          >
            {t("notificationSettings.buttons.save")}
          </button>
          <button
              onClick={fetchOrResetChanges}
              className="px-15 py-1 bg-white border-2 border-mallorca-purple rounded-lg text-mallorca-purple"
          >
            {t("notificationSettings.buttons.cancel")}
          </button>
        </div>
      </div>
  );
}
