import React from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../common/use-document-title";

export default function Profile() {
    const { t } = useTranslation();
    useDocumentTitle(t("profile.title"));

    return (
        <div className="w-full max-w-5xl mx-auto py-12 px-4">

            {/* ACCOUNT INFORMATION TITLE */}
            <h2 className="text-center text-2xl font-semibold mb-8">
                {t("profile.accountInformation")}
            </h2>

            {/* ACCOUNT INFORMATION CARD */}
            <div className="bg-white shadow-sm border border-gray-200 rounded-lg p-8 relative">

                {/* Notification settings button */}
                <div className="absolute right-4 top-4">
                    <button className="px-6 py-2 bg-mallorca-purple text-white rounded-md hover:bg-mallorca-purple-dark text-sm">
                        {t("profile.notificationSettings")}
                    </button>
                </div>

                {/* First Row */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                    <div className="flex flex-col">
                        <label className="mb-1 font-medium">
                            {t("profile.firstName")} *
                        </label>
                        <input
                            type="text"
                            defaultValue="Peter"
                            className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
                        />
                    </div>

                    <div className="flex flex-col">
                        <label className="mb-1 font-medium">
                            {t("profile.lastName")} *
                        </label>
                        <input
                            type="text"
                            defaultValue="Griffin"
                            className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
                        />
                    </div>
                </div>

                {/* Second Row */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                    <div className="flex flex-col">
                        <label className="mb-1 font-medium">
                            {t("profile.email")} *
                        </label>
                        <input
                            type="email"
                            defaultValue="user@gmail.com"
                            className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
                        />
                    </div>

                    <div className="flex flex-col">
                        <label className="mb-1 font-medium">
                            {t("profile.phoneNumber")}
                        </label>
                        <input
                            type="text"
                            defaultValue="user@gmail.com"
                            className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
                        />
                    </div>
                </div>

                {/* Save Button */}
                <button className="w-full mt-8 py-3 bg-mallorca-purple text-white rounded-md hover:bg-mallorca-purple-dark">
                    {t("profile.saveChanges")}
                </button>
            </div>

            {/* CHANGE PASSWORD SECTION */}
            <h2 className="text-center text-2xl font-semibold mt-16 mb-8">
                {t("profile.changePassword")}
            </h2>

            {/* PASSWORD SECTION CARD */}
            <div className="bg-white shadow-sm border border-gray-200 rounded-lg p-8">

                {/* Current Password */}
                <div className="flex flex-col mb-6">
                    <label className="mb-1 font-medium">
                        {t("profile.currentPassword")} *
                    </label>
                    <input
                        type="password"
                        className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
                    />
                </div>

                {/* New + Confirm Password */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="flex flex-col">
                        <label className="mb-1 font-medium">
                            {t("profile.newPassword")}
                        </label>
                        <input
                            type="password"
                            className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
                        />
                    </div>

                    <div className="flex flex-col">
                        <label className="mb-1 font-medium">
                            {t("profile.confirmPassword")}
                        </label>
                        <input
                            type="password"
                            className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
                        />
                    </div>
                </div>

                {/* Change Password Button */}
                <button className="w-full mt-8 py-3 bg-mallorca-purple text-white rounded-md hover:bg-mallorca-purple-dark">
                    {t("profile.changePassword")}
                </button>

                {/* Delete Account Button */}
                <button className="w-full mt-4 py-3 bg-red-600 text-white rounded-md hover:bg-red-500">
                    {t("profile.deleteAccount")}
                </button>
            </div>
        </div>
    );
}
