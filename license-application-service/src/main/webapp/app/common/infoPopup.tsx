import React from "react";

type TextPopupProps = {
  text: string;
};

export const InfoPopup: React.FC<TextPopupProps> = ({text}) => {
  return (
      <div
          className="mt-6 flex items-center justify-center"
      >
        <div
            className="w-full max-w-md rounded-2xl px-6 py-3 shadow-xl bg-mallorca-purple"
            onClick={(e) => e.stopPropagation()}
        >
          <span className="my-4 text-md text-white whitespace-pre-line">
            {text}
          </span>
        </div>
      </div>
  );
};