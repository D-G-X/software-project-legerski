import React from "react";
import {FormHeader} from "./headingTitle";
import {TriangleAlert} from "lucide-react";

type Props = {
  open: boolean;
  onCancel: () => void;
  onConfirm: () => void;
  headingLabel: string;
  subHeadingLabel: string;
  quoteTitle: string;
  quoteText?: string[];
  cancelLabel: string;
  confirmLabel: string;
};

export default function ReleaseLicensePopup({
                                              open,
                                              onCancel,
                                              onConfirm,
                                              headingLabel,
                                              subHeadingLabel,
                                              quoteTitle,
                                              quoteText,
                                              cancelLabel,
                                              confirmLabel,
                                            }: Props) {
  if (!open) return null;

  return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
        {/* backdrop click = cancel */}
        <div className="absolute inset-0" onClick={onCancel}/>

        <div className="relative z-10 w-full max-w-3xl rounded-xl bg-white p-6 shadow-xl">
          <FormHeader
              heading={headingLabel}
              subHeading={subHeadingLabel}
              className="my-8"
          />

          <blockquote className="mt-6 border-l-4 border-gray-300 pl-4">

            <div className="flex items-center gap-2 h-full px-3 text-mallorca-purple">
              <TriangleAlert/>
              <p className="font-medium italic">{quoteTitle}</p>
            </div>
            <div className="pl-2 mt-2 text-gray-700">
              {(quoteText ?? []).map((line, idx) => (
                  <p key={idx}>{line}</p>
              ))}
            </div>
          </blockquote>

          <div className="mt-8 flex flex-line items-center jusify-center gap-4">
            {/* Cancel Button */}
            <button
                type="button"
                onClick={onCancel}
                className="bg-mallorca-purple text-white px-10 py-2 rounded-md w-48 font-medium text-lg"
            >
              {cancelLabel}
            </button>

            {/* Confirm Button */}
            <button
                type="button"
                onClick={onConfirm}
                className="bg-white text-mallorca-purple px-10 py-2 rounded-md w-48 font-medium text-lg border border-mallorca-purple hover:bg-mallorca-purple/50 hover:text-white"
            >
              {confirmLabel}
            </button>
          </div>
        </div>
      </div>
  );
}