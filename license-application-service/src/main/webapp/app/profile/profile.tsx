import React, {useContext, useEffect, useState} from "react";
import {useNavigate} from "react-router";
import {useTranslation} from "react-i18next";
import {useDocumentTitle} from "../common/utils";
import {UpdateUserMutationError, useGetUser, useUpdateUser,} from "app/services/users/users";
import {AuthContext} from "app/common/auth/AuthContext";
import {getUserIdFromToken} from "app/common/auth/authTokenDecode";
import {useGlobalLoader} from "app/common/GlobalLoader";
import {isValidConfirmPassword, isValidEmail, isValidName, isValidPassword,} from "app/common/validationRules";

export default function Profile() {
  const auth = useContext(AuthContext);
  const userId = getUserIdFromToken(auth?.accessToken);
  const navigate = useNavigate();
  const {t} = useTranslation();
  useDocumentTitle(t("profile.title"));

  const [user, setUser] = useState({
    firstName: "",
    lastName: "",
    email: "",
  });

  const [errors, setErrors] = useState<{
    firstName?: string;
    lastName?: string;
    email?: string;
    newPassword?: string;
    confirmPassword?: string;
    currentPassword?: string;
  }>({});

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
      email: response.data.email ?? "",
    });
  }, [response.data]);

  const {show, hide} = useGlobalLoader();

  useEffect(() => {
    if (response.isFetching) show(t("profile.loading") || "Loading…");
    else hide();
  }, [response.isFetching, show, hide, t]);

  const updateUserMutation = useUpdateUser({
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
    mutation: {
      onError: (error: UpdateUserMutationError) => {
        const status = error?.response?.status;

        switch (status) {
          case 400:
            alert(t("profile.alerts.invalidRequest"));
            break;

          case 401:
            alert(t("profile.alerts.unauthorized"));
            break;

          case 404:
            alert(t("profile.alerts.userNotFound"));
            break;

          default:
            alert(t("profile.alerts.failed"));
            break;
        }

        console.error(error);
      },
    },
  });

  const handleUpdateDetails = async () => {
    const firstNameValidation = isValidName(user.firstName);
    const lastNameValidation = isValidName(user.lastName);
    const emailValidation = isValidEmail(user.email);

    const newErrors: typeof errors = {};

    if (!firstNameValidation.isValid)
      newErrors.firstName = firstNameValidation.message;

    if (!lastNameValidation.isValid)
      newErrors.lastName = lastNameValidation.message;

    if (!emailValidation.isValid) newErrors.email = emailValidation.message;

    setErrors(newErrors);

    if (Object.keys(newErrors).length > 0) return;

    try {
      show(t("profile.updating") || "Updating…");
      await updateUserMutation.mutateAsync({
        userId,
        data: {
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email,
        },
      });
      // double alerts
      alert(t("profile.updateSuccess") || "User details updated successfully!");
    } finally {
      hide();
    }
  };

  // --------------------------------
  // PATCH: UPDATE PASSWORD
  // --------------------------------
  const handleChangePassword = async () => {
    const passwordValidation = isValidPassword(newPassword);
    const confirmPasswordValidation = isValidConfirmPassword(
        newPassword,
        confirmPassword
    );

    const newErrors: typeof errors = {};

    if (!currentPassword || currentPassword.trim() === "") {
      setErrors({currentPassword: t("validation.password.required")});
      return;
    }

    if (!passwordValidation.isValid)
      newErrors.newPassword = passwordValidation.message;

    if (!confirmPasswordValidation.isValid)
      newErrors.confirmPassword = confirmPasswordValidation.message;

    setErrors(newErrors);

    if (Object.keys(newErrors).length > 0) return;

    try {
      show(t("profile.updatingPassword") || "Updating password…");
      await updateUserMutation.mutateAsync({
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
      // double alerts
      alert(t("profile.passwordUpdated") || "Password updated successfully!");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } finally {
      hide();
    }
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
          {/* <div className="absolute right-4 top-4">
          <button
            onClick={() => {
              navigate("/notification-settings");
            }}
            className="px-6 py-2 bg-mallorca-purple text-white rounded-md hover:bg-mallorca-purple-dark text-sm"
          >
            {t("profile.notificationSettings")}
          </button>
        </div> */}

          {/* FIRST + LAST NAME */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
            <div className="flex flex-col">
              <label className="mb-1 font-medium">
                {t("profile.firstName")} *
              </label>
              <input
                  type="text"
                  value={user.firstName}
                  onChange={(e) => {
                    setUser({...user, firstName: e.target.value});
                    setErrors({...errors, firstName: undefined});
                  }}
                  className="border border-gray-300 rounded-md px-3 py-2"
              />
              {errors.firstName && (
                  <span className="text-red-600 text-sm mt-1">
                {errors.firstName}
              </span>
              )}
            </div>

            <div className="flex flex-col">
              <label className="mb-1 font-medium">
                {t("profile.lastName")} *
              </label>
              <input
                  type="text"
                  value={user.lastName}
                  onChange={(e) => setUser({...user, lastName: e.target.value})}
                  className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
              />
              {errors.lastName && (
                  <span className="text-red-600 text-sm mt-1">
                {errors.lastName}
              </span>
              )}
            </div>
          </div>

          {/* EMAIL + PHONE */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
            <div className="flex flex-col">
              <label className="mb-1 font-medium">{t("profile.email")} *</label>
              <input
                  type="email"
                  value={user.email}
                  onChange={(e) => setUser({...user, email: e.target.value})}
                  className="border border-gray-300 rounded-md px-3 py-2 focus:ring-mallorca-purple focus:border-mallorca-purple"
              />
              {errors.email && (
                  <span className="text-red-600 text-sm mt-1">{errors.email}</span>
              )}
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
            {errors.currentPassword && (
                <span className="text-red-600 text-sm mt-1">
              {errors.currentPassword}
            </span>
            )}
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
              {errors.newPassword && (
                  <span className="text-red-600 text-sm mt-1">
                {errors.newPassword}
              </span>
              )}
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
              {errors.confirmPassword && (
                  <span className="text-red-600 text-sm mt-1">
                {errors.confirmPassword}
              </span>
              )}
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
              className="w-full mt-4 py-3 bg-red-600 text-white rounded-md hover:bg-red-500"
          >
            {t("profile.deleteAccount")}
          </button>
        </div>
      </div>
  );
}
