import React from "react";
import { t } from "i18next";

export function OrDivider() {
  return (
    <>
      <div className="flex items-center">
        <hr className="w-[50%] border border-mallorca-purple rounded-full"></hr>
        <span className="w-[10%] text-xl text-center text-mallorca-purple/50">
          {t("common.orDivider")}
        </span>
        <hr className="w-[50%] border border-mallorca-purple rounded-full"></hr>
      </div>
    </>
  );
}
