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
      <div className="container mx-auto px-4 md:px-6">
        <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
          <div className="">
            <div className="text-4xl font-bold text-mallorca-purple w-full mb-10">
              {t("notificationSettings.pageTitle")}
            </div>
            <div className="pb-5">
              <div className="pb-2">
                <span>{t("notificationSettings.description")}</span>
              </div>
              <div className="mx-3">
                {/* Notification Preference */}
                <div className="text-md py-4 text-mallorca-purple/50">
                  <div className="pb-4">
                    {t("notificationSettings.sections.appUpdates.title")}
                  </div>
                  <span className="bg-matte-grey px-4 py-2 text-white rounded-lg ml-5">
                  <input
                      id="app_notif_type"
                      name="app_notif_type"
                      type="checkbox"
                      checked={form.app_notif_type}
                      onChange={handleChange}
                      className="accent-matte-grey checked:bg-black p-2 rounded mr-2"
                  />
                  <label className="ml-2" htmlFor="app_notif_type">
                    {t("notificationSettings.sections.appUpdates.email")}
                  </label>
                </span>
                </div>

                {/* Application Status Updates */}
                <div className="text-md py-4 text-mallorca-purple/50">
                  <div className="pb-4">
                    {t("notificationSettings.sections.appStatus.title")}
                  </div>
                  <span className="bg-matte-grey px-4 py-2 text-white rounded-lg ml-5">
                  <input
                      id="app_notif_freq"
                      name="app_notif_freq"
                      type="checkbox"
                      checked={form.app_notif_freq}
                      onChange={handleChange}
                      className="accent-matte-grey checked:bg-black p-2 rounded mr-2"
                  />
                  <label className="ml-2" htmlFor="app_notif_freq">
                    {t("notificationSettings.sections.appStatus.label")}
                  </label>
                </span>
                </div>

                {/* License Renewal Updates */}
                <div className="text-md py-4 text-mallorca-purple/50">
                  <div className="pb-4">
                    {t("notificationSettings.sections.license.title")}
                  </div>
                  <span className="bg-matte-grey px-4 py-2 text-white rounded-lg ml-5">
                  <input
                      id="app_notif_lic_reminder"
                      name="app_notif_lic_reminder"
                      type="checkbox"
                      checked={form.app_notif_lic_reminder}
                      onChange={handleChange}
                      className="accent-matte-grey checked:bg-black p-2 rounded mr-2"
                  />
                  <label className="ml-2" htmlFor="app_notif_lic_reminder">
                    {t("notificationSettings.sections.license.label")}
                  </label>
                </span>
                </div>
              </div>
            </div>

            <div
                className={`pt-5 ${
                    showSubmitChanges ? "flex" : "invisible"
                } justify-between px-5`}
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
        </div>
      </div>
  );
}
