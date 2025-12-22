import React, { useContext, useState } from "react";
import { useTranslation } from "react-i18next";
import useDocumentTitle from "app/common/use-document-title";
import { Upload } from "lucide-react";
import { useNavigate, useParams } from "react-router";
import { useGetApplication } from "app/services/applications/applications";
import { AuthContext } from "app/common/AuthContext";

interface DocUploadForm {
  id_proof: File | null;
  address_proof: File | null;
}

interface DocUploadErrors {
  id_proof: string;
  address_proof: string;
}

export default function ApplicationDocumentUpload() {
  const params = useParams();
  const applicationID = params.id!;
  const auth = useContext(AuthContext);

  const { t } = useTranslation();
  const navigate = useNavigate();
  useDocumentTitle(t("license.request.title"));

  const [docUploadForm, setDocUploadForm] = useState<DocUploadForm>({
    id_proof: null,
    address_proof: null,
  });

  const [errors, setErrors] = useState<DocUploadErrors>({
    id_proof: "",
    address_proof: "",
  });

  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB in bytes
  const ALLOWED_TYPES = ["application/pdf"];

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, files } = e.target;
    if (!files || files.length === 0) return;

    const file = files[0];

    if (!file) return;

    if (!ALLOWED_TYPES.includes(file.type)) {
      setErrors((prev) => ({
        ...prev,
        [id]: t("license.document_upload.input.fileTypeError"),
      }));
      return;
    }

    if (file.size > MAX_FILE_SIZE) {
      setErrors((prev) => ({
        ...prev,
        [id]: t("license.document_upload.input.fileSizeError"),
      }));
      return;
    }

    setDocUploadForm((prev) => ({
      ...prev,
      [id]: file,
    }));

    setErrors((prev) => ({
      ...prev,
      [id]: "",
    }));
  };

  const { refetch } = useGetApplication(Number(applicationID), {
    query: {
      enabled: false,
    },
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
  });

  const handleSubmit = async () => {
    const newErrors: DocUploadErrors = { id_proof: "", address_proof: "" };
    let hasError = false;

    if (!docUploadForm.id_proof) {
      newErrors.id_proof = t("license.document_upload.id_doc.errorMissing");
      hasError = true;
    }
    if (!docUploadForm.address_proof) {
      newErrors.address_proof = t(
        "license.document_upload.address_doc.errorMissing"
      );
      hasError = true;
    }

    setErrors(newErrors);
    if (hasError) return;

    // Prepare FormData
    const formData = new FormData();
    if (docUploadForm.id_proof)
      formData.append("id_file", docUploadForm.id_proof);
    if (docUploadForm.address_proof)
      formData.append("proof_file", docUploadForm.address_proof);
    if (applicationID) formData.append("application_id", applicationID);

    try {
      const response = await fetch("http://localhost:8083/process-document", {
        method: "POST",
        body: formData,
      });

      if (response.status !== 200) {
        throw new Error(
          `Upload failed: ${response.status} ${response.statusText}`
        );
      }

      const applcationDetails = await refetch();

      // Navigate to payment
      // have to add new status, for now the below if else will always return successful verification of documents
      if (
        applcationDetails.data?.data.application_status ||
        applcationDetails.data?.data.application_status ===
          "DOCUMENTS_SUBMITTED"
      ) {
        navigate("/payment/" + applicationID);
        alert(t("license.document_upload.success_message"));
      } else if (
        applcationDetails.data?.data.application_status === "REJECTED"
      ) {
        alert(t("license.document_upload.reject_message"));
      } else {
        alert(t("license.document_upload.error"));
      }
    } catch (err) {
      alert(t("license.document_upload.error"));
    }
  };

  return (
    <div className="h-[calc(100vh-8rem)] px-5 xl:px-15 pt-4 bg-white">
      <h1 className="text-mallorca-purple font-semibold tracking-wide text-2xl text-center py-4">
        {t("license.request.title")}
      </h1>

      <div className="flex justify-center my-8">
        <div className="mb-4 w-full lg:w-[75%] xl:w-[50%]">
          <label>
            <span className="block">{t("license.document_upload.label")}</span>
          </label>

          {/* ID Proof */}
          <div className="mt-4">
            <h2>
              {t("license.document_upload.id_doc.label")}{" "}
              <span className="text-red-500">*</span>
            </h2>
            <label className="block text-gray-400 my-1 mb-2">
              {t("license.document_upload.id_doc.description")}
            </label>
            <label className="block md:flex items-center justify-between shadow-sm cursor-pointer transition p-2 bg-mallorca-purple/10 hover:bg-mallorca-purple/75 border-2 border-mallorca-purple rounded-2xl text-mallorca-purple hover:text-white">
              <div className="flex items-center gap-2 p-1 rounded-xl">
                <Upload size={18} />
                <span className="font-medium pr-3">
                  {t("license.document_upload.input.buttonLabel")}
                </span>
                {docUploadForm.id_proof && (
                  <span className="text-sm truncate max-w-xs">
                    {docUploadForm.id_proof && (
                      <span className="text-sm truncate max-w-xs">
                        {docUploadForm.id_proof.name.length > 40
                          ? docUploadForm.id_proof.name.substring(0, 40) + "…"
                          : docUploadForm.id_proof.name}
                      </span>
                    )}
                  </span>
                )}
              </div>
              <span className="text-sm ">
                {t("license.document_upload.input.fileSizeLabel")}
              </span>
              <input
                id="id_proof"
                type="file"
                className="hidden"
                onChange={handleFileChange}
                accept=".pdf"
              />
            </label>
            {errors.id_proof && (
              <div className="text-red-500 mt-1 pl-4 text-xs">
                {errors.id_proof}
              </div>
            )}
          </div>

          {/* Address Proof */}
          <div className="mt-8">
            <h2>
              {t("license.document_upload.address_doc.label")}{" "}
              <span className="text-red-500">*</span>
            </h2>
            <label className="block text-gray-400 my-2 mb-1">
              {t("license.document_upload.address_doc.description")}
            </label>
            <label className="block md:flex items-center justify-between shadow-sm cursor-pointer transition p-2 bg-mallorca-purple/10 hover:bg-mallorca-purple/75 border-2 border-mallorca-purple rounded-2xl text-mallorca-purple hover:text-white">
              <div className="flex items-center gap-5 p-1 rounded-xl">
                <Upload size={18} />
                <span className="font-medium pr-3">
                  {t("license.document_upload.input.buttonLabel")}
                </span>
                {docUploadForm.address_proof && (
                  <span className="text-sm truncate max-w-xs">
                    {docUploadForm.address_proof.name.length > 30
                      ? docUploadForm.address_proof.name.substring(0, 30) + "…"
                      : docUploadForm.address_proof.name}
                  </span>
                )}
              </div>
              <span className="text-sm ">
                {t("license.document_upload.input.fileSizeLabel")}
              </span>
              <input
                id="address_proof"
                type="file"
                className="hidden"
                onChange={handleFileChange}
                accept=".pdf"
              />
            </label>
            {errors.address_proof && (
              <div className="text-red-500 mt-1 pl-4 text-xs">
                {errors.address_proof}
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="text-center">
        <button
          className="p-2 bg-mallorca-purple rounded-xl text-white w-full lg:w-[75%] xl:w-[50%] cursor-pointer"
          onClick={handleSubmit}
        >
          Submit Documents
        </button>
      </div>
    </div>
  );
}
