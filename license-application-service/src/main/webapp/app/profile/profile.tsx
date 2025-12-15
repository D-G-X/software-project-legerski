import React, { useContext, useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "../common/use-document-title";
import {
    UpdateUserMutationError,
    useGetUser,
    useUpdateUser
} from "app/services/users/users";
import { AuthContext } from "app/common/AuthContext";
import { getUserIdFromToken } from "app/common/authTokenDecode";

export default function Profile() {
  const auth = useContext(AuthContext);
  const userId = getUserIdFromToken(auth?.accessToken);
  const navigate = useNavigate();
  console.log(auth?.accessToken);
  const { t } = useTranslation();
  useDocumentTitle(t("profile.title"));

  const [user, setUser] = useState({
    firstName: "",
    lastName: "",
    email: ""
  });

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");


  // -----------------------------
  // FETCH USER DETAILS
  // -----------------------------

    const response = useGetUser(userId ?? "", {
        axios: {
            headers: {
                Authorization: `Bearer ${auth?.accessToken}`,
            },
        },
        query: {
            enabled: !!userId,
            select: (res) => res.data,
        },
    });

    useEffect(() => {
        if (!response.data) return;

        setUser({
            firstName: response.data.firstName ?? "",
            lastName: response.data.lastName ?? "",
            email: response.data.email ?? ""
        });
    }, [response.data]);


    console.log(response);

    const updateUserMutation = useUpdateUser({
        axios: { headers: { Authorization: `Bearer ${auth?.accessToken}` } },
        mutation: {
            onSuccess: () => {
                alert("User details updated successfully!");
            },
            onError: (err: UpdateUserMutationError) => {
                console.error(err);
                alert("Failed to update user details.");
            },
        },
    });

    const handleUpdateDetails = () => {
        if (!user.firstName || !user.lastName || !user.email) {
            alert('All fields are required!');
            return;
        }

        updateUserMutation.mutate({
            userId,
            data: {
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
            },
        });
    };

  // --------------------------------
  // PATCH: UPDATE PASSWORD
  // --------------------------------
    const handleChangePassword = () => {
        if (!currentPassword || !newPassword || !confirmPassword) {
            alert("Please fill in all password fields!");
            return;
        }

        if (newPassword !== confirmPassword) {
            alert("New passwords do not match!");
            return;
        }

        updateUserMutation.mutate({
            userId,
            data: {
                credentials: [
                    {
                        type: "password",
                        value: newPassword,
                        temporary: false,
                    },
                ],
            },
        });

        setCurrentPassword("");
        setNewPassword("");
        setConfirmPassword("");
    };

    // --------------------------------
    // PATCH: Delete Account
    // --------------------------------

    const handleDeleteAccount = () => {
        navigate("/deleteProfile");
    };


  return (
    <div className="w-full max-w-5xl mx-auto py-12 px-4">
      <h2 className="text-center text-2xl font-semibold mb-8">
        {t("profile.accountInformation")}
      </h2>

      <div className="bg-white shadow-sm border border-gray-200 rounded-lg p-8 relative">
        {/* Notification settings button */}
        <div className="absolute right-4 top-4">
          <button className="px-6 py-2 bg-mallorca-purple text-white rounded-md hover:bg-mallorca-purple-dark text-sm">
            {t("profile.notificationSettings")}
          </button>
        </div>

        {/* FIRST + LAST NAME */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
          <div className="flex flex-col">
            <label className="mb-1 font-medium">
              {t("profile.firstName")} *
            </label>
            <input
              type="text"
              value={user.firstName}
              onChange={(e) => setUser({ ...user, firstName: e.target.value })}
              className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
            />
          </div>

          <div className="flex flex-col">
            <label className="mb-1 font-medium">
              {t("profile.lastName")} *
            </label>
            <input
              type="text"
              value={user.lastName}
              onChange={(e) => setUser({ ...user, lastName: e.target.value })}
              className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
            />
          </div>
        </div>

        {/* EMAIL + PHONE */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
          <div className="flex flex-col">
            <label className="mb-1 font-medium">{t("profile.email")} *</label>
            <input
              type="email"
              value={user.email}
              onChange={(e) => setUser({ ...user, email: e.target.value })}
              className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
            />
          </div>
        </div>

        {/* SAVE BUTTON */}
        <button
          onClick={handleUpdateDetails}
          className="w-full mt-8 py-3 bg-mallorca-purple text-white rounded-md hover:bg-mallorca-purple-dark"
        >
          {t("profile.saveChanges")}
        </button>
      </div>

      {/* ------------------------------- */}
      {/* CHANGE PASSWORD SECTION */}
      {/* ------------------------------- */}

      <h2 className="text-center text-2xl font-semibold mt-16 mb-8">
        {t("profile.changePassword")}
      </h2>

      <div className="bg-white shadow-sm border border-gray-200 rounded-lg p-8">
        <div className="flex flex-col mb-6">
          <label className="mb-1 font-medium">
            {t("profile.currentPassword")} *
          </label>
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="flex flex-col">
            <label className="mb-1 font-medium">
              {t("profile.newPassword")}
            </label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
            />
          </div>

          <div className="flex flex-col">
            <label className="mb-1 font-medium">
              {t("profile.confirmPassword")}
            </label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
            />
          </div>
        </div>

        <button
          onClick={handleChangePassword}
          className="w-full mt-8 py-3 bg-mallorca-purple text-white rounded-md hover:bg-mallorca-purple-dark"
        >
          {t("profile.changePassword")}
        </button>

        <button
            onClick={handleDeleteAccount}
            className="w-full mt-4 py-3 bg-red-600 text-white rounded-md hover:bg-red-500">
          {t("profile.deleteAccount")}
        </button>
      </div>
    </div>
  );
}
