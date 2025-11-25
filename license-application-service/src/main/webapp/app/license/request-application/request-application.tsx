import React, { useState } from "react";
import { validateResults } from "app/common/utils";
import {
  validateAddress,
  validateCity,
  validateEmail,
  validateName,
  validatePhoneNumber,
  validatePostalCode,
} from "../../common/validationRules";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import "./request-application.css";
import { Upload } from "lucide-react";

export default function RequestApplication() {
  const { t } = useTranslation();
  useDocumentTitle(t("license.request.title"));

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

  const [form, setForm] = useState({
    first_name: "",
    last_name: "",
    email: "",
    phone_number: "",
    reason: "",
    street_address_1: "",
    street_address_2: "",
    city: "",
    postal_code: "",
    country: "",
    rental_license_type: "",
    rental_start_date: "",
    additional_comments: "",
    consent_personal_data: false,
    id_proof_doc: "",
    address_proof_doc: "",
  });

  const [errors, setErrors] = useState({
    first_name: "",
    last_name: "",
    email: "",
    phone_number: "",
    reason: "",
    street_address_1: "",
    street_address_2: "",
    city: "",
    postal_code: "",
    country: "",
    rental_license_type: "",
    rental_start_date: "",
    additional_comments: "",
    app_submit: "",
    consent_personal_data: "",
    id_proof_doc: "",
    address_proof_doc: "",
  });

  const handleChange = (
    e:
      | React.ChangeEvent<HTMLInputElement>
      | React.ChangeEvent<HTMLTextAreaElement>
  ) => {
    let { id, value: rawValue, name } = e.target;

    let value: string | boolean = rawValue; // allow boolean overrides

    const key = id || name;

    if (key === "consent_personal_data") {
      value = !form.consent_personal_data;
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

  const handleSubmit = () => {
    const firstNameValidateResult: validateResults = validateName(
      form.first_name
    );
    const lastNameValidateResult: validateResults = validateName(
      form.last_name
    );
    const emailValidateResult: validateResults = validateEmail(form.email);
    const phoneNumberValidateResult: validateResults = validatePhoneNumber(
      form.phone_number
    );
    const strAddrsLineOneValidateResult: validateResults = validateAddress(
      form.street_address_1
    );
    const strAddrsLineTwoValidateResult: validateResults = validateAddress(
      form.street_address_2
    );
    const cityValidateResults: validateResults = validateCity(form.city);

    const postCodeValidateResults: validateResults = validatePostalCode(
      form.postal_code
    );

    let newErrors = {
      first_name: "",
      last_name: "",
      email: "",
      phone_number: "",
      reason: "",
      street_address_1: "",
      street_address_2: "",
      city: "",
      postal_code: "",
      country: "",
      rental_license_type: "",
      rental_start_date: "",
      additional_comments: "",
      app_submit: "",
      consent_personal_data: "",
      id_proof_doc: "",
      address_proof_doc: "",
    };

    if (!firstNameValidateResult.isValid) {
      newErrors.first_name = firstNameValidateResult.message;
    }

    if (!lastNameValidateResult.isValid) {
      newErrors.last_name = lastNameValidateResult.message;
    }

    if (!emailValidateResult.isValid) {
      newErrors.email = emailValidateResult.message;
    }

    if (!phoneNumberValidateResult.isValid) {
      newErrors.phone_number = phoneNumberValidateResult.message;
    }

    if (!strAddrsLineOneValidateResult.isValid) {
      newErrors.street_address_1 = strAddrsLineOneValidateResult.message;
    }

    if (!strAddrsLineTwoValidateResult.isValid) {
      newErrors.street_address_2 = strAddrsLineTwoValidateResult.message;
    }

    if (!cityValidateResults.isValid) {
      newErrors.city = cityValidateResults.message;
    }

    if (!postCodeValidateResults.isValid) {
      newErrors.postal_code = postCodeValidateResults.message;
    }

    if (!form.rental_license_type) {
      newErrors.rental_license_type = t(
        "license.request.rentalType.errorMissing"
      );
    }

    if (!form.rental_start_date) {
      newErrors.rental_start_date = t(
        "license.request.rentalStart.errorMissing"
      );
    }

    if (!form.consent_personal_data) {
      newErrors.consent_personal_data = t(
        "license.request.consent.errorMissing"
      );
    }

    if (!form.id_proof_doc) {
      newErrors.id_proof_doc = t("license.request.documents.id.errorMissing");
    }

    if (!form.address_proof_doc) {
      newErrors.address_proof_doc = t(
        "license.request.documents.address.errorMissing"
      );
    }

    if (
      newErrors.first_name ||
      newErrors.last_name ||
      newErrors.email ||
      newErrors.phone_number ||
      newErrors.street_address_1 ||
      newErrors.street_address_2 ||
      newErrors.city ||
      newErrors.postal_code ||
      newErrors.rental_license_type ||
      newErrors.rental_start_date ||
      newErrors.id_proof_doc ||
      newErrors.address_proof_doc
    ) {
      setErrors(newErrors);
      console.log(newErrors);
      return false;
    }

    setErrors({
      first_name: "",
      last_name: "",
      email: "",
      phone_number: "",
      reason: "",
      street_address_1: "",
      street_address_2: "",
      city: "",
      postal_code: "",
      country: "",
      app_submit: "",
      rental_license_type: "",
      rental_start_date: "",
      additional_comments: "",
      consent_personal_data: "",
      id_proof_doc: "",
      address_proof_doc: "",
    });

    // implement the API call for register;
    alert("API has to be integrated yet!!");
    return true;
  };

  return (
    <div className="mx-15 mt-4 mb-10 relative min-h-[calc(100vh-4rem)] bg-white items-center justify-center">
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
                  onChange={handleChange}
                  className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
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
                    onChange={handleChange}
                    className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
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
                placeholder={t("license.request.email.placeholder")}
                onChange={handleChange}
                className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
              />
              {errors.email && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.email}
                </div>
              )}
            </div>
          </div>

          {/* Phone number */}
          <div className="mb-4">
            <label>
              <span className="block">
                {t("license.request.phone.label")}{" "}
                <span className="text-red-500">*</span>
              </span>
              <span className="block text-gray-400">
                {t("license.request.phone.description")}
              </span>
            </label>
            <div className="mt-1">
              <input
                type="tel"
                id="phone_number"
                value={form.phone_number}
                placeholder={t("license.request.phone.placeholder")}
                onChange={handleChange}
                className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
              />
              {errors.phone_number && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.phone_number}
                </div>
              )}
            </div>
          </div>

          {/* Reason */}
          <div className="mb-4">
            <label>
              <span className="block">{t("license.request.reason.label")}</span>
              <span className="block text-gray-400">
                {t("license.request.reason.description")}
              </span>
            </label>
            <div className="mt-1">
              <textarea
                id="reason"
                value={form.reason}
                placeholder={t("license.request.reason.placeholder")}
                onChange={handleChange}
                className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
              />
              {errors.reason && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.reason}
                </div>
              )}
            </div>
          </div>

          {/* Property Address */}
          <div className="mb-4">
            <label>
              <span className="block">
                {t("license.request.address.label")}{" "}
                <span className="text-red-500">*</span>
              </span>
              <span className="block text-gray-400">
                {t("license.request.address.description")}
              </span>
            </label>
            <div className="mt-1">
              <input
                type="text"
                id="street_address_1"
                value={form.street_address_1}
                placeholder={t("license.request.address.street1Placeholder")}
                onChange={handleChange}
                className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
              />
              {errors.street_address_1 && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.street_address_1}
                </div>
              )}
            </div>
            <div className="mt-2">
              <input
                type="text"
                id="street_address_2"
                value={form.street_address_2}
                placeholder={t("license.request.address.street2Placeholder")}
                onChange={handleChange}
                className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
              />
              {errors.street_address_2 && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.street_address_2}
                </div>
              )}
            </div>
            <div className="grid grid-cols-2 gap-5 mt-2">
              <div className="">
                <input
                  type="number"
                  id="postal_code"
                  value={form.postal_code}
                  placeholder={t("license.request.address.postalPlaceholder")}
                  onChange={handleChange}
                  className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
                />
                {errors.postal_code && (
                  <div className="text-red-500 mt-1 pl-4 text-xs">
                    {errors.postal_code}
                  </div>
                )}
              </div>
              <div className="">
                <input
                  type="text"
                  id="city"
                  value={form.city}
                  placeholder={t("license.request.address.cityPlaceholder")}
                  onChange={handleChange}
                  className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
                />
                {errors.city && (
                  <div className="text-red-500 mt-1 pl-4 text-xs">
                    {errors.city}
                  </div>
                )}
              </div>
            </div>
            <div className="mt-2">
              <input
                type="text"
                id="country"
                disabled
                value={t("license.request.address.countryLabel")}
                className="bg-gray-200 rounded-xl text-mallorca-purple/60 focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full"
              />
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

          {/* Expected Rental Start Date */}
          <div className="mb-4">
            <label>
              <span className="block">
                {t("license.request.rentalStart.label")}
                <span className="text-red-500">*</span>
              </span>
              <span className="block text-gray-400">
                {t("license.request.rentalStart.description")}
              </span>
            </label>
            <div className="mt-1 relative">
              <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-mallorca-purple">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  strokeWidth={2}
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
              </span>
              <input
                type="date"
                id="rental_start_date"
                min={new Date().toISOString().split("T")[0]}
                onChange={handleChange}
                placeholder="DD/MM/YYYY"
                className="border border-mallorca-purple rounded-xl text-mallorca-purple focus:outline-none focus:ring-1 focus:ring-mallorca-purple w-full pl-10"
              />
            </div>
            {errors.rental_start_date && (
              <div className="text-red-500 mt-1 pl-4 text-xs">
                {errors.rental_start_date}
              </div>
            )}
          </div>

          {/* Documents Upload */}
          <div className="mb-4">
            <label>
              <span className="block">
                {t("license.request.documents.label")}
                <span className="text-red-500">*</span>
              </span>
            </label>
            <div className="w-full mt-1">
              <div>
                <label className="block text-gray-400 my-1 mb-2">
                  {t("license.request.documents.id.description")}
                </label>
                <label className="flex items-center justify-between shadow-sm cursor-pointer transition p-2 bg-mallorca-purple/25 hover:bg-mallorca-purple/75 border-2 border-mallorca-purple rounded-2xl text-mallorca-purple hover:text-white">
                  <div className="flex items-center gap-2 p-1 rounded-xl">
                    <span className="inline-flex items-center justify-center rounded-full ml-1 mr-2">
                      <Upload size={18} />
                    </span>
                    <span className="font-medium pr-3">
                      {t("license.request.documents.input.buttonLabel")}
                    </span>
                  </div>
                  <span className="text-sm ">
                    {t("license.request.documents.input.fileSizeLabel")}
                  </span>
                  <input id="id_proof" type="file" className="hidden" />
                </label>
              </div>
              {errors.id_proof_doc && (
                <div className="text-red-500 mt-1 pl-4 text-xs">
                  {errors.id_proof_doc}
                </div>
              )}

              <div>
                <label className="block text-gray-400 my-2 mb-1">
                  {t("license.request.documents.address.description")}
                </label>
                <label className="flex items-center justify-between shadow-sm cursor-pointer transition p-2 bg-mallorca-purple/25 hover:bg-mallorca-purple/75 border-2 border-mallorca-purple rounded-2xl text-mallorca-purple hover:text-white">
                  <div className="flex items-center gap-2 p-1 rounded-xl">
                    <span className="inline-flex items-center justify-center rounded-full ml-1 mr-2">
                      <Upload size={18} />
                    </span>
                    <span className="font-medium pr-3">
                      {t("license.request.documents.input.buttonLabel")}
                    </span>
                  </div>
                  <span className="text-sm ">
                    {t("license.request.documents.input.fileSizeLabel")}
                  </span>
                  <input id="address_proof" type="file" className="hidden" />
                </label>
              </div>
            </div>

            {errors.address_proof_doc && (
              <div className="text-red-500 mt-1 pl-4 text-xs">
                {errors.address_proof_doc}
              </div>
            )}
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

          {/* Consent Checkbox */}
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
              {t("license.request.consent.label")}
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
                onClick={handleSubmit}
                className="bg-mallorca-purple border-2 border-mallorca-purple text-white px-10 py-2 rounded-md w-full font-medium text-lg hover:bg-mallorca-red/75 hover:border-mallorca-red"
              >
                {t("license.request.buttons.submit")}
              </button>
              <button
                type="submit"
                onClick={handleSubmit}
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
              backgroundImage: "url('/images/request_form_image.png')",
              backgroundPosition: "center bottom",
            }}
          ></div>
        </div>
      </div>
    </div>
  );
}
