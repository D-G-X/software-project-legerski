import React, { useState } from "react";

export default function NotificationSettings() {
  const [showSubmitChanges, setShowSubmitChanges] = useState(false);
  const [form, setForm] = useState({
    app_notif_type: false,
    app_notif_freq: false,
    app_notif_lic_reminder: false,
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let { id, value: rawValue, name } = e.target;
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
      alert("Client Error Occured");
      window.location.reload();
      return;
    }

    // submit API call
    alert("API call has to be implemented!");
  };

  return (
    <div className="container mx-auto px-4 md:px-6">
      <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
        <div className="">
          <div className="text-4xl font-bold text-mallorca-purple w-full mb-10">
            Notification Settings
          </div>
          <div className="pb-5">
            <div className="pb-2">
              <span>Manage how we send notifications to you</span>
            </div>
            <div className=" mx-3">
              {/* Notificaton Preference */}
              <div className="text-md py-4 text-mallorca-purple/50">
                <div className="pb-4">When my application is updated:</div>
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
                    Email
                  </label>
                </span>
              </div>

              {/* Application Updates */}
              <div className="text-md py-4 text-mallorca-purple/50">
                <div className="pb-4">Application Updates</div>
                <span className="bg-matte-grey px-4 py-2 text-white rounded-lg ml-5">
                  <input
                    id="app_notif_freq"
                    name="app_notif_freq"
                    type="checkbox"
                    className="accent-matte-grey checked:bg-black p-2 rounded mr-2"
                  />
                  <label className="ml-2" htmlFor="app_notif_freq">
                    When my application staus is updated
                  </label>
                </span>
              </div>

              {/* License Renewal Updates */}
              <div className="text-md py-4 text-mallorca-purple/50">
                <div className="pb-4">License Renewal Updates</div>
                <span className="bg-matte-grey px-4 py-2 text-white rounded-lg ml-5">
                  <input
                    id="app_notif_lic_reminder"
                    name="app_notif_lic_reminder"
                    type="checkbox"
                    className="accent-matte-grey checked:bg-black p-2 rounded mr-2"
                  />
                  <label className="ml-2" htmlFor="app_notif_lic_reminder">
                    When my license is 6 months due for expiry
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
              Save
            </button>
            <button
              onClick={fetchOrResetChanges}
              className="px-15 py-1 bg-white border-2 border-mallorca-purple rounded-lg text-mallorca-purple"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
