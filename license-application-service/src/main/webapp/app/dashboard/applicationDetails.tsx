import React from "react";
import {useTranslation} from "react-i18next";
import {FormHeader} from "app/common/headingTitle";
import {ApplicationResource} from "../../types";
import {Minimize2} from "lucide-react";
import "./applicationDetails.css";

interface ApplicationDetailsProps {
  open: boolean;
  entry: ApplicationResource | null;
  onClose: () => void;
}

export default function ApplicationDetails({open, entry, onClose}: ApplicationDetailsProps) {
  if (!open || !entry) return null;

  const {t} = useTranslation();

  return (
      <div
          className="fixed inset-0 z-50 flex flex-col bg-black/50"
          aria-modal="true"
          role="dialog"
      >
        {/* Spacer to push content below the header */}
        <div className="mt-20"></div>
        {/* Overlay to close the modal when clicking outside */}
        <div
            className="absolute inset-0"
            onClick={onClose}
        />

        <div className="container mx-auto px-4 md:px-6">

          <div
              className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center rounded-xl">
            {/* Close Button */}
            <button
                onClick={onClose}
                className="absolute flex top-4 right-4 items-center justify-center cursor-pointer px-3 py-1 rounded-md border
             text-mallorca-purple/75 hover:bg-mallorca-purple/10"
            ><Minimize2 />
            </button>
            <div className="font-inter min-w-96">

              <FormHeader
                  heading={t("applicationDetails.index.headline")}
                  subHeading={t("")}
              />

            </div>
          </div>
        </div>
      </div>
  );
}
