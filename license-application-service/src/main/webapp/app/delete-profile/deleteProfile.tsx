import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../common/use-document-title";

const DeleteProfile = () => {
    const { t } = useTranslation();
    const [isLoading, setIsLoading] = useState(false);

    // Set page title
    useDocumentTitle(t("deleteProfile.deleteAccount"));

    const handleDelete = async () => {
        setIsLoading(true);
        try {
            // Simulate account deletion request
            // In a real scenario, replace this with an API call to delete the user account
            setTimeout(() => {
                setIsLoading(false);
                alert("Account deleted successfully!");
                window.location.href = "/logout";  // Redirect to logout page (or another page)
            }, 2000);
        } catch (error) {
            setIsLoading(false);
            alert("An error occurred. Please try again later.");
        }
    };

    const handleCancel = () => {
        window.location.href = "/profile";  // Redirect back to profile page
    };

    return (
        <div className="w-full max-w-4xl mx-auto py-12 px-4">
            <div className="bg-white shadow-sm border border-gray-200 rounded-lg p-8">
                <h2 className="text-center text-2xl font-semibold mb-6">
                    {t("deleteProfile.deleteAccount")}
                </h2>

                <div className="mb-6 text-center text-lg">
                    <p>{t("deleteProfile.areYouSureDelete")}</p>
                </div>

                <div className="bg-purple-100 text-purple-800 p-4 rounded-md mb-8">
                    <div className="flex items-center">
                        <span className="mr-2 text-xl">⚠️</span>
                        <p>{t("deleteProfile.warningMessage")}</p>
                    </div>
                </div>

                <div className="flex justify-center gap-4">
                    <button
                        className="px-6 py-2 bg-mallorca-purple text-white rounded-md hover:bg-mallorca-purple-dark"
                        onClick={handleCancel}
                        disabled={isLoading}
                    >
                        {t("deleteProfile.cancel")}
                    </button>

                    <button
                        className="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-500"
                        onClick={handleDelete}
                        disabled={isLoading}
                    >
                        {isLoading ? t("deleteProfile.loading") : t("deleteProfile.yesDelete")}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DeleteProfile;
