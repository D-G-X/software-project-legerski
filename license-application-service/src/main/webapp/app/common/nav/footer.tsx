import React, {useContext} from "react";
import {useTranslation} from "react-i18next";
import {Link} from "react-router";
import {Scale, Users} from "lucide-react";
import NotificationPopup from "../NotificationPopup";
import {AuthContext} from "../auth/AuthContext";

export default function Footer() {
  const auth = useContext(AuthContext);
  const {t} = useTranslation();

  return (
      <footer className="flex bg-white h-12 font-inter px-10">
        <div className="w-full h-full">
          <div className="flex justify-between items-center w-full h-full">

            {/* Left: Notification */}
            {auth?.accessToken ? (
                <NotificationPopup
                    className="ml-1 my-2 rounded-full min-w-24
                    h-[calc(100%-1rem)]
                    flex items-center justify-center cursor-pointer
                    text-mallorca-purple/75 hover:bg-mallorca-purple/10"
                />
            ) : (
                <div/>
            )}

            {/* Right side */}
            <div className="flex items-center h-full">

              {/* Contact */}
              <Link
                  id="contactLink"
                  to="/contact"
                  className="ml-1 my-2 rounded-full min-w-24
             h-[calc(100%-1rem)]
             flex items-center justify-center cursor-pointer
             text-mallorca-purple/75 hover:bg-mallorca-purple/10"
              >
                <div className="flex items-center gap-2 h-full px-3">
                  <span className="text-md">{t("nav.contactBtn")}</span>
                  <Users size={20}/>
                </div>
              </Link>

              {/* Legal */}
              <Link
                  id="legalLink"
                  to="/legal"
                  className="ml-1 my-2 rounded-full min-w-24
             h-[calc(100%-1rem)]
             flex items-center justify-center cursor-pointer
             text-mallorca-purple/75 hover:bg-mallorca-purple/10"
              >
                <div className="flex items-center gap-2 h-full px-3">
                  <span className="text-md">{t("nav.legalBtn")}</span>
                  <Scale size={20}/>
                </div>
              </Link>

            </div>
          </div>
        </div>
      </footer>
  );
}
