import React, {useState, useContext} from "react";
import {useTranslation} from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import {AuthContext} from "app/common/AuthContext";
import {getUserIdFromToken} from "app/common/authTokenDecode";
import {useDeleteUser, DeleteUserMutationError} from "app/services/users/users";
import {useNavigate} from "react-router";

const DeleteProfile = () => {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const auth = useContext(AuthContext);
    const userId = getUserIdFromToken(auth?.accessToken);
    const [isLoading, setIsLoading] = useState(false);

    useDocumentTitle(t("deleteProfile.deleteAccount"));

    const deleteUserMutation = useDeleteUser({
        axios: {headers: {Authorization: `Bearer ${auth?.accessToken}`}},
        mutation: {
            onSuccess: () => {
                alert("Account deleted successfully!");
                auth?.signOut();
            },
            onError: (err: DeleteUserMutationError) => {
                console.error(err);
                alert("Failed to delete account.");
                setIsLoading(false);
            },
        },
    });

    const handleDelete = (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();
        if (!userId) return;
        if (!window.confirm(t("deleteProfile.areYouSureDelete"))) return;
        setIsLoading(true);
        deleteUserMutation.mutate({userId});
    };


    const handleCancel = () => {
        navigate("/profile");
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
