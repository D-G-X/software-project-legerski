import React from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";
import {Scale, Users} from "lucide-react";

export default function Footer() {
  const { t } = useTranslation();

return (
    <footer className="bg-green-50 max-h-20 font-inter">
      <nav className="py-4 w-full px-5">
        <div className="flex justify-between w-full">
          <div className="hidden lg:flex items-center h-12 ml-auto">

            {/* Contact Button */}
            <div className="ml-1 h-full">
              <Link
                  to="/contact"
                  className="ml-1 rounded-lg min-w-24 h-full flex items-center justify-center cursor-pointer text-mallorca-purple/75 hover:bg-mallorca-purple/10 text-center"
              >
                <div className="flex justify-between items-center gap-2 h-full rounded px-3">
                  <div className="text-md text-left h-5">
                    {t("nav.contactBtn")}
                  </div>

                  <span className="items-baseline inline-flex">
                    <Users size={20}/>
                  </span>
                </div>
              </Link>
            </div>
            {/* Legal Notice Button */}
            <div className="ml-1 h-full">
              <Link
                  to="/legal"
                  className="ml-1 rounded-lg min-w-24 h-full flex items-center justify-center cursor-pointer text-mallorca-purple/75 hover:bg-mallorca-purple/10 text-center"
              >
                <div className="flex justify-between items-center gap-2 h-full rounded px-3">
                  <div className="text-md text-left h-5">
                    {t("nav.legalBtn")}
                  </div>

                  <span className="items-baseline inline-flex">
                    <Scale size={20} />
                  </span>
                </div>
              </Link>
            </div>
          </div>

        </div>
      </nav>
    </footer>
  );
}
