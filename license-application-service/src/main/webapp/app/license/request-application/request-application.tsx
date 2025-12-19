import React, { useContext, useEffect, useState } from "react";
import { validateResults } from "app/common/utils";
import {
  isValidCadastralNumber,
  isValidEmail,
  isValidName,
} from "../../common/validationRules";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import "./request-application.css";
import { Link, useNavigate, useParams } from "react-router";
import { AuthContext } from "app/common/AuthContext";
import { getUserIdFromToken } from "app/common/authTokenDecode";
import {
  useCreateApplication,
  useGetApplication,
  useUpdateApplication,
} from "app/services/applications/applications";
import { LicenseTypeApiEnum } from "types/licenseTypeApiEnum";
import { useGetUser } from "app/services/users/users";

export default function RequestApplication() {
  const params = useParams();
  const applicationId = parseInt(params.id!, 10);
  const requestType = params.type!;
  const auth = useContext(AuthContext);
  const { t } = useTranslation();
  const navigate = useNavigate();
  useDocumentTitle(t("license.request.title"));

  const [form, setForm] = useState({
    first_name: "",
    last_name: "",
    email: "",
    cadastral_number: "",
    rental_license_type: "",
    additional_comments: "",
    consent_personal_data: false,
    consent_legal_data: false,
  });

  const [errors, setErrors] = useState({
    first_name: "",
    last_name: "",
    email: "",
    cadastral_number: "",
    rental_license_type: "",
    additional_comments: "",
    app_submit: "",
    consent_personal_data: "",
    consent_legal_data: "",
  });

  const userID = getUserIdFromToken(auth?.accessToken);

  const userDetails = useGetUser(userID, {
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
    query: {
      select: (res) => res.data,
      enabled: !!userID,
    },
  });

  const applicationDetails = useGetApplication(applicationId, {
    query: {
      select: (response) => response.data,
      enabled: !!applicationId && ["renew", "edit"].includes(requestType),
    },
    axios: {
      headers: {
        Authorization: `${auth?.tokenType} ${auth?.accessToken}`,
      },
    },
  });

  useEffect(() => {
    if (!userDetails.data) return;

    setForm((prev) => ({
      ...prev,
      first_name: userDetails.data.firstName ?? prev.first_name,
      last_name: userDetails.data.lastName ?? prev.last_name,
      email: userDetails.data.email ?? prev.email,
    }));
  }, [userDetails.data]);

  useEffect(() => {
    setForm((prev) => ({
      ...prev,
      cadastral_number:
        applicationDetails.data?.cadastral_reference ?? prev.cadastral_number,
      rental_license_type:
        applicationDetails.data?.license_type ?? prev.rental_license_type,
      additional_comments:
        applicationDetails.data?.remarks ?? prev.additional_comments,
    }));
  }, [applicationDetails.data]);

  const rentalLicenseType = [
    {
      value: t("license.request.rentalType.types.ETV.value"),
      label: t("license.request.rentalType.types.ETV.label"),
      description: t("license.request.rentalType.types.ETV.description"),
    },
    {
      value: t("license.request.rentalType.types.ETVPL.value"),
      label: t("license.request.rentalType.types.ETVPL.label"),
      description: t("license.request.rentalType.types.ETVPL.description"),
    },
    {
      value: t("license.request.rentalType.types.ETV60.value"),
      label: t("license.request.rentalType.types.ETV60.label"),
      description: t("license.request.rentalType.types.ETV60.description"),
    },
  ];

  const handleChange = (
    e:
      | React.ChangeEvent<HTMLInputElement>
      | React.ChangeEvent<HTMLTextAreaElement>
  ) => {
    let { id, value: rawValue, name } = e.target;

    let value: string | boolean = rawValue;

    const key = id || name;

    if (key === "consent_personal_data") {
      value = !form.consent_personal_data;
    }

    if (key === "consent_legal_data") {
      value = !form.consent_legal_data;
    }

    setForm((prev) => ({
      ...prev,
      [key]: value,
    }));

    setErrors((prev) => ({
      ...prev,
      [key]: "",
    }));
  };

  const createApplication = useCreateApplication({
    mutation: {
      onError: (error) => {
        console.error("Application creation error:", error);
      },
    },
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
  });

  // uncomment after APIs are ready to handle edit form

  // const updateApplication = useUpdateApplication({
  //   mutation: {
  //     onError: (error) => {
  //       console.error("Application creation error:", error);
  //     },
  //   },
  //   axios: {
  //     headers: {
  //       Authorization: `Bearer ${auth?.accessToken}`,
  //     },
  //   },
  // });

  const handleSubmit = async (btn: string) => {
    let newErrors = {
      first_name: "",
      last_name: "",
      email: "",
      cadastral_number: "",
      rental_license_type: "",
      additional_comments: "",
      app_submit: "",
      consent_personal_data: "",
      consent_legal_data: "",
    };

    const firstNameValidateResult: validateResults = isValidName(
      form.first_name
    );
    const lastNameValidateResult: validateResults = isValidName(form.last_name);

    const emailValidateResult: validateResults = isValidEmail(form.email);
    const cadastralNumValidateResult: validateResults = isValidCadastralNumber(
      form.cadastral_number
    );
    if (!firstNameValidateResult.isValid) {
      newErrors.first_name = firstNameValidateResult.message;
    }

    if (!lastNameValidateResult.isValid) {
      newErrors.last_name = lastNameValidateResult.message;
    }

    if (!emailValidateResult.isValid) {
      newErrors.email = emailValidateResult.message;
    }
    if (!cadastralNumValidateResult.isValid) {
      newErrors.cadastral_number = cadastralNumValidateResult.message;
    }

    if (!form.rental_license_type) {
      newErrors.rental_license_type = t(
        "license.request.rentalType.errorMissing"
      );
    }

    if (!form.consent_personal_data) {
      newErrors.consent_personal_data = t(
        "license.request.personal_consent.errorMissing"
      );
    }

    if (!form.consent_legal_data) {
      newErrors.consent_legal_data = t(
        "license.request.legal_consent.errorMissing"
      );
    }

    if (
      newErrors.first_name ||
      newErrors.last_name ||
      newErrors.email ||
      newErrors.cadastral_number ||
      newErrors.rental_license_type ||
      newErrors.consent_legal_data ||
      newErrors.consent_personal_data
    ) {
      setErrors(newErrors);
      return false;
    }

    setErrors({
      first_name: "",
      last_name: "",
      email: "",
      cadastral_number: "",
      rental_license_type: "",
      additional_comments: "",
      consent_personal_data: "",
      consent_legal_data: "",
      app_submit: "",
    });

    try {
      const response = await createApplication.mutateAsync({
        data: {
          user_id: userID,
          license_type: form.rental_license_type as LicenseTypeApiEnum,
          cadastral_reference: form.cadastral_number,
          remarks: form.additional_comments,
        },
      });

      // uncomment after implementation of edit users
      // const baseData = {
      //   license_type: form.rental_license_type as LicenseTypeApiEnum,
      //   cadastral_reference: form.cadastral_number,
      //   remarks: form.additional_comments,
      // };

      // const response = ["edit"].includes(requestType)
      //   ? await updateApplication.mutateAsync({ data: baseData })
      //   : await createApplication.mutateAsync({
      //       data: { ...baseData, user_id: userID },
      //     });

      switch (response.status) {
        case 201:
          if (btn == "draft") {
            navigate("/");
            break;
          }
          navigate("/license-document-upload/" + response.data.id);
          break;

        default:
          alert("Unexpected Error occurred");
      }
      return true;
    } catch (error: any) {
      const status = error?.response?.status;

      switch (status) {
        case 400:
          alert("Bad Request Error");
          break;

        case 401:
          alert("Unauthorized Error");
          break;

        default:
          alert(t("register.registerUserAlerts.serverError"));
          break;
      }
      return false;
    }
  };

  return (
    <div className="mx-15 bg-white items-center justify-center min-h-[calc(100vh-8rem)]">
      <h1 className="text-mallorca-purple font-semibold tracking-wide text-2xl text-center py-4">
        {t("license.request.title")}
      </h1>
      <div className="font-inter min-w-96 grid md:grid-cols-2">
        {/* License Request Form */}
        <div className=" p-5 relative">
          <div className="italic text-right z-10 absolute top-5 right-5 text-gray-400">
            {t("license.request.noteRequired.beforeStar")}{" "}
            <span className="text-red-500">*</span>{" "}
            {t("license.request.noteRequired.afterStar")}
          </div>
          {/* First & Last Name Field */}
          <div className="mb-4 mt-3">
            <label>
              <span className="block">
                {t("license.request.fullName.label")}{" "}
                <span className="text-red-500">*</span>
              </span>
              <span className="block text-gray-400">
                {t("license.request.fullName.description")}{" "}
              </span>
            </label>
            <div className="grid grid-cols-2 gap-5 mt-1">
              {/* First Name */}
              <div className="">
                <input
                  type="text"
                  id="first_name"
                  value={form.first_name}
                  placeholder={t(
                    "license.request.fullName.firstNamePlaceholder"
                  )}
                  disabled
                  onChange={handleChange}
                  className="border border-mallorca-purple disabled:border-gray-300 rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full disabled:bg-gray-300 disabled:text-mallorca-purple/50"
                />
                {errors.first_name && (
                  <div className="text-red-500 mt-1 pl-4 text-xs">
                    {errors.first_name}
                  </div>
                )}
              </div>

              {/* Last Name */}
              <div>
                <div className="">
                  <input
                    type="text"
                    id="last_name"
                    value={form.last_name}
                    placeholder={t(
                      "license.request.fullName.lastNamePlaceholder"
                    )}
                    disabled
                    onChange={handleChange}
                    className="border border-mallorca-purple disabled:border-gray-300 rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full disabled:bg-gray-300 disabled:text-mallorca-purple/50"
                  />
                  {errors.last_name && (
                    <div className="text-red-500 mt-1 pl-4 text-xs">
                      {errors.last_name}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Email address */}
          <div className="mb-4">
            <label>
              <span className="block">
                {t("license.request.email.label")}{" "}
                <span className="text-red-500">*</span>
              </span>
              <span className="block text-gray-400">
                {t("license.request.email.description")}
              </span>
            </label>
            <div className="mt-1">
              <input
                type="email"
                id="email"
                value={form.email}
                disabled
                placeholder={t("license.request.email.placeholder")}
                onChange={handleChange}
                className="border border-mallorca-purple disabled:border-gray-300 rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full disabled:bg-gray-300 disabled:text-mallorca-purple/50"
              />
              {errors.email && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.email}
                </div>
              )}
            </div>
          </div>

          {/* Cadastral Number */}
          <div className="mb-4">
            <label>
              <span className="block">
                {t("license.request.cadastraNumber.label")}{" "}
                <span className="text-red-500">*</span>
              </span>
              <span className="block text-gray-400">
                {t("license.request.cadastraNumber.description")}
              </span>
            </label>
            <div className="mt-1">
              <input
                maxLength={20}
                minLength={20}
                type="text"
                id="cadastral_number"
                value={form.cadastral_number}
                placeholder={t("license.request.cadastraNumber.placeholder")}
                onChange={handleChange}
                className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
              />
              {errors.cadastral_number && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.cadastral_number}
                </div>
              )}
            </div>
          </div>

          {/* Rental License Type */}
          <div className="mb-4">
            <label>
              <span className="block">
                {t("license.request.rentalType.label")}{" "}
                <span className="text-red-500">*</span>
              </span>
              <span className="block text-gray-400">
                {t("license.request.rentalType.description")}
              </span>
            </label>
            <div className="mt-1">
              {rentalLicenseType.map(({ value, label, description }) => (
                <label
                  key={value}
                  style={{
                    display: "block",
                    marginBottom: 8,
                    cursor: "pointer",
                  }}
                >
                  <input
                    type="radio"
                    name="rental_license_type"
                    id="rental_license_type"
                    value={value}
                    checked={form.rental_license_type === value}
                    onChange={handleChange}
                    className="mr-3 text-mallorca-purple"
                  />
                  <span className="font-bold">{label + " "}</span>
                  <span className="text-gray-400">({description})</span>
                </label>
              ))}
              {errors.rental_license_type && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.rental_license_type}
                </div>
              )}
            </div>
          </div>

          {/* Additional Comments / Remarks */}
          <div className="mb-4">
            <label>
              <span className="block">
                {t("license.request.comments.label")}
              </span>
              <span className="block text-gray-400">
                {t("license.request.comments.description")}
              </span>
            </label>
            <textarea
              id="additional_comments"
              value={form.additional_comments}
              placeholder={t("license.request.comments.placeholder")}
              onChange={handleChange}
              className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full mt-1"
            />
            {errors.additional_comments && (
              <div className="text-red-500 mt-1 pl-4 text-xs">
                {errors.additional_comments}
              </div>
            )}
          </div>

          {/* Legal Consent Checkbox */}
          <div className="mb-2 flex items-start space-x-3">
            <input
              type="checkbox"
              id="consent_legal_data"
              name="consent_legal_data"
              checked={form.consent_legal_data}
              onChange={handleChange}
              className="mt-1 h-5 w-5 text-mallorca-purple border-gray-300 rounded"
            />
            <label
              htmlFor="consent_legal_data"
              className="text-base font-normal cursor-pointer"
            >
              {t("license.request.legal_consent.label_before_redirect")}{" "}
              <Link to="/legal" className="underline">
                {t("license.request.legal_consent.legal_redirect")}
              </Link>{" "}
              {t("license.request.legal_consent.label_after_redirect")}
              <span className="text-red-500">*</span>
            </label>
          </div>
          {errors.consent_legal_data && (
            <div className="text-red-500 pl-4 text-xs mb-2">
              {errors.consent_legal_data}
            </div>
          )}

          {/* Personal Data Consent Checkbox */}
          <div className="mb-4 flex items-start space-x-3">
            <input
              type="checkbox"
              id="consent_personal_data"
              name="consent_personal_data"
              checked={form.consent_personal_data}
              onChange={handleChange}
              className="mt-1 h-5 w-5 text-mallorca-purple border-gray-300 rounded"
            />
            <label
              htmlFor="consent_personal_data"
              className="text-base font-normal cursor-pointer"
            >
              {t("license.request.personal_consent.label")}
              <span className="text-red-500">*</span>
            </label>
          </div>
          {errors.consent_personal_data && (
            <div className="text-red-500 mt-1 pl-4 text-xs">
              {errors.consent_personal_data}
            </div>
          )}

          <div className="my-4">
            {errors.app_submit && (
              <div className="text-red-500 mt-2 pl-4">{errors.app_submit}</div>
            )}
            <div className="flex gap-2">
              <button
                type="submit"
                onClick={() => handleSubmit("submit")}
                className="bg-mallorca-purple border-2 border-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg hover:bg-mallorca-red/75 hover:border-mallorca-red"
              >
                {t("license.request.buttons.submit")}
              </button>
              <button
                type="submit"
                onClick={() => handleSubmit("draft")}
                className="block bg-mallorca-purple/75 border-2 border-mallorca-purple hover:bg-mallorca-red/75 hover:border-mallorca-red text-white px-10 py-2 rounded-md w-full font-medium text-lg"
              >
                {t("license.request.buttons.draft")}
              </button>
            </div>
          </div>
        </div>

        {/* Form Image */}
        <div className="p-5 pb-8">
          <div
            className="min-w-100 min-h-full inset-0 bg-center bg-cover bg-no-repeat rounded-4xl"
            style={{
              backgroundImage: "url('/images/request_form_image.webp')",
              backgroundPosition: "center bottom",
            }}
          ></div>
        </div>
      </div>
    </div>
  );
}
