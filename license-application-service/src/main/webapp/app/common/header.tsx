import React, { useState } from "react";
import { Link } from "react-router";
// import { ChevronDown } from "lucide-react";
import { useTranslation } from "react-i18next";
import { LanguageInfo } from "./utils";
import { Menu, Users } from "lucide-react";

export default function Header() {
  const { t, i18n } = useTranslation();
  const [open, setOpen] = useState(false);

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
    setOpen(false);
  };

  // const headerRef = useRef<HTMLElement | null>(null);

  // const handleClick = (event: Event) => {
  //   // close any open dropdown
  //   const $clickedDropdown = (event.target as HTMLElement).closest(
  //     ".js-dropdown"
  //   );
  //   const $dropdowns = headerRef.current!.querySelectorAll(".js-dropdown");
  //   $dropdowns.forEach(($dropdown: Element) => {
  //     if (
  //       $clickedDropdown !== $dropdown &&
  //       $dropdown.getAttribute("data-dropdown-keepopen") !== "true"
  //     ) {
  //       $dropdown.ariaExpanded = "false";
  //       $dropdown.nextElementSibling!.classList.add("hidden");
  //     }
  //   });

  //   // toggle selected if applicable
  //   if ($clickedDropdown) {
  //     $clickedDropdown.ariaExpanded =
  //       "" + ($clickedDropdown.ariaExpanded !== "true");
  //     $clickedDropdown.nextElementSibling!.classList.toggle("hidden");
  //   }
  // };

  // useEffect(() => {
  //   document.body.addEventListener("click", handleClick);
  //   return () => document.body.removeEventListener("click", handleClick);
  // }, []);

  return (
    <header className="bg-gray-5 max-h-20 font-inter">
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
                width="104"
                height="104"
                className="inline-block"
              />
              <div className="text-xl md:text-2xl lg:text-[1.5rem] xl:text-3xl pl-5 font-semibold text-mallorca-purple">
                {t("app.title")}
              </div>
            </Link>
          </div>

          {/* Full Navigation Button */}
          <div className="hidden lg:flex items-center h-12">
            {/* Language Dropdown */}
            <div className="h-full" id="navbarToggle">
              <ul className="rounded min-w-34 w-full h-full">
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
                            className="inline-block h-5 w-10"
                        />
                      <div className="text-md text-left h-5 min-w-15">
                        {language_title_img_map[i18n.language]?.language}
                      </div>
                    </div>
                  </button>

                  {open && (
                    <ul className="absolute left-0 bg-white shadow-sm shadow-black rounded-lg z-50 min-w-34">
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
                              } px-3 py-2 ${roundedClass}`}
                            >
                              <img
                                src={lang?.img}
                                alt={t("app.title")}
                                className="inline-block h-5 w-10"
                              />
                              <div className="text-left h-5 min-w-15">
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

            {/* Contact Button */}
            <div className="ml-1 h-full">
              <Link
                to="/contact"
                className="ml-1 rounded-lg w-28 h-full flex items-center justify-center cursor-pointer text-mallorca-purple/75 hover:bg-mallorca-purple/10 text-center"
              >
                <div className="flex justify-between items-center gap-2 h-full rounded px-3">
                  <div className="text-md text-left h-5">
                    {t("nav.contactBtn")}
                  </div>

                  <span className="items-baseline inline-flex">
                    <Users size={20} />
                  </span>
                </div>
              </Link>
            </div>

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
