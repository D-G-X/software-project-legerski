import React, { useContext, useEffect, useRef, useState } from "react";
import { Link } from "react-router";
import { useTranslation } from "react-i18next";
import { LanguageInfo } from "./utils";
import { Menu } from "lucide-react";
import { AuthContext } from "./AuthContext";

export default function Header() {
  const auth = useContext(AuthContext);
  const { t, i18n } = useTranslation();
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLUListElement>(null);

  const language_title_img_map: Record<string, LanguageInfo> = {
    en: {
      language: t("nav.languages.en.name"),
      img: "/images/languages/english.png",
      imgAlt: t("nav.languages.en.iconAlt"),
    },
    fr: {
      language: t("nav.languages.fr.name"),
      img: "/images/languages/french.png",
      imgAlt: t("nav.languages.fr.iconAlt"),
    },
    de: {
      language: t("nav.languages.de.name"),
      img: "/images/languages/german.png",
      imgAlt: t("nav.languages.de.iconAlt"),
    },
    hi: {
      language: t("nav.languages.hi.name"),
      img: "/images/languages/hindi.png",
      imgAlt: t("nav.languages.hi.iconAlt"),
    },
    id: {
      language: t("nav.languages.id.name"),
      img: "/images/languages/indonesian.png",
      imgAlt: t("nav.languages.id.iconAlt"),
    },
    es: {
      language: t("nav.languages.es.name"),
      img: "/images/languages/spanish.png",
      imgAlt: t("nav.languages.es.iconAlt"),
    },
  };

  const totalItems = Object.keys(language_title_img_map).length;

  const toggleDropdown = () => setOpen((prev) => !prev);
  const changeLanguage = (lng: string) => {
    i18n.changeLanguage(lng);
    localStorage.setItem("language", lng);
    setOpen(false);
  };

  useEffect(() => {
    const savedLanguage = localStorage.getItem("language");
    if (savedLanguage) {
      i18n.changeLanguage(savedLanguage); // Set the language if saved in localStorage
    }

    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setOpen(false); // Close dropdown if click is outside
      }
    };

    document.addEventListener("mousedown", handleClickOutside); // Listen for mouse clicks
    return () => {
      document.removeEventListener("mousedown", handleClickOutside); // Clean up the event listener
    };
  }, []);

  return (
    <header className="bg-gray-5 h-20 font-inter">
      <nav className="py-4 w-full px-5">
        <div className="flex justify-between w-full">
          {/* Title and Logo */}
          <div>
            <Link
              to="/"
              className="flex justify-center items-center py-1.5 mr-4"
            >
              <img
                src="/images/logo.svg"
                alt={t("nav.appLogoAlt")}
                width="80"
                height="80"
                className="inline-block"
              />
              <div className="text-xl md:text-xl lg:text-[1.5rem] xl:text-3xl pl-5 font-semibold text-mallorca-purple">
                {t("app.title")}
              </div>
            </Link>
          </div>

          {/* Full Navigation Button */}
          <div className="hidden lg:flex items-center h-12">
            {/* Language Dropdown */}
            <div className="h-full" id="navbarToggle">
              <ul className="rounded min-w-36 w-full h-full">
                <li className="relative w-full h-full">
                  <button
                    type="button"
                    onClick={toggleDropdown}
                    className="text-gray-500 cursor-pointer flex items-center h-full"
                  >
                    <div className="flex justify-between items-center gap-2 h-full text-mallorca-purple/75 hover:bg-mallorca-purple/10 rounded-lg px-3">
                      <img
                        src={language_title_img_map[i18n.language]?.img}
                        alt={t("app.title")}
                        className="inline-block h-5 min-w-10"
                      />
                      <div className="text-md text-left h-5 min-w-18">
                        {language_title_img_map[i18n.language]?.language}
                      </div>
                    </div>
                  </button>

                  {open && (
                    <ul
                      ref={dropdownRef}
                      className="absolute left-0 bg-white shadow-sm shadow-black rounded-lg z-50 min-w-36"
                    >
                      {Object.keys(language_title_img_map).map((key, index) => {
                        const lang = language_title_img_map[key];
                        const isSelected = i18n.language === key;
                        const roundedClass =
                          index === 0
                            ? "rounded-t-md"
                            : index === totalItems - 1
                            ? "rounded-b-md"
                            : "";

                        return (
                          <li
                            key={key}
                            className={`${
                              isSelected ? "bg-gray-300" : "bg-white"
                            } ${roundedClass}`}
                          >
                            <button
                              disabled={isSelected}
                              onClick={() => changeLanguage(key)}
                              className={`cursor-pointer flex justify-between items-center gap-2 text-mallorca-purple/75 ${
                                isSelected ? "" : "hover:bg-gray-100"
                              } p-2 ${roundedClass}`}
                            >
                              <img
                                src={lang?.img}
                                alt={t("app.title")}
                                className="inline-block h-5 min-w-10"
                              />
                              <div className="text-left h-5 min-w-22">
                                {lang?.language}
                              </div>
                            </button>
                          </li>
                        );
                      })}
                    </ul>
                  )}
                </li>
              </ul>
            </div>

            {/* Signout Button */}
            {auth?.accessToken && (
              <div className="ml-3">
                <button
                  onClick={auth?.signOut}
                  className="rounded-lg block bg-mallorca-purple border-2 border-mallorca-purple text-white p-1 px-4 hover:bg-mallorca-red/75 hover:border-mallorca-red"
                >
                  {t("nav.signOutBtn")}
                </button>
              </div>
            )}

            {!auth?.accessToken && (
              <>
                {/* Signin Button */}
                <div className="ml-3">
                  <Link
                    to="/login"
                    className="block rounded-lg bg-mallorca-purple/75 border-2 border-mallorca-purple/75 text-white p-1 px-4 hover:bg-mallorca-red/75 hover:border-mallorca-red"
                  >
                    {t("nav.signInBtn")}
                  </Link>
                </div>
                {/* Register Button */}
                <div className="ml-3">
                  <Link
                    to="/register"
                    className="rounded-lg block bg-mallorca-purple border-2 border-mallorca-purple text-white p-1 px-4 hover:bg-mallorca-red/75 hover:border-mallorca-red"
                  >
                    {t("nav.registerBtn")}
                  </Link>
                </div>
              </>
            )}
          </div>

          {/* Hamburger Navigation Menu */}
          <div className="flex lg:hidden items-center">
            <button
              type="button"
              className="js-dropdown rounded cursor-pointer p-2 text-center hover:bg-mallorca-purple/50"
              data-dropdown-keepopen="true"
              aria-label={t("nav.toggle")}
              aria-controls="navbarToggle"
              aria-expanded="false"
            >
              <Menu className="w-5 h-5" />
            </button>
          </div>
        </div>
      </nav>
    </header>
  );
}
