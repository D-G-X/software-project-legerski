import React, { useEffect, useRef, useState } from "react";
import { Link } from "react-router";
// import { ChevronDown } from "lucide-react";
import { useTranslation } from "react-i18next";
import { LanguageInfo } from "./utils";

export default function Header() {
  const { t, i18n } = useTranslation();
  const [open, setOpen] = useState(false);

  const language_title_img_map: Record<string, LanguageInfo> = {
    en: {
      language: "English",
      img: "/images/languages/english.png",
    },
    es: {
      language: "Spanish",
      img: "/images/languages/spanish.png",
    },
  };

  const totalItems = Object.keys(language_title_img_map).length;

  const toggleDropdown = () => setOpen((prev) => !prev);
  const changeLanguage = (lng: string) => {
    i18n.changeLanguage(lng);
    setOpen(false);
  };

  const headerRef = useRef<HTMLElement | null>(null);

  const handleClick = (event: Event) => {
    // close any open dropdown
    const $clickedDropdown = (event.target as HTMLElement).closest(
      ".js-dropdown"
    );
    const $dropdowns = headerRef.current!.querySelectorAll(".js-dropdown");
    $dropdowns.forEach(($dropdown: Element) => {
      if (
        $clickedDropdown !== $dropdown &&
        $dropdown.getAttribute("data-dropdown-keepopen") !== "true"
      ) {
        $dropdown.ariaExpanded = "false";
        $dropdown.nextElementSibling!.classList.add("hidden");
      }
    });
    // toggle selected if applicable
    if ($clickedDropdown) {
      $clickedDropdown.ariaExpanded =
        "" + ($clickedDropdown.ariaExpanded !== "true");
      $clickedDropdown.nextElementSibling!.classList.toggle("hidden");
    }
  };

  useEffect(() => {
    document.body.addEventListener("click", handleClick);
    return () => document.body.removeEventListener("click", handleClick);
  }, []);

  return (
    <header ref={headerRef} className="bg-gray-5 max-h-20 font-inter">
      <nav className="py-4 w-full px-5 md:px-10">
        <div className="flex justify-between w-full">
          <div>
            <Link
              to="/"
              className="flex justify-center items-center py-1.5 mr-4"
            >
              <img
                src="/images/logo.svg"
                alt={t("app.title")}
                width="100"
                height="100"
                className="inline-block"
              />
              <span className="text-xl md:text-2xl pl-5 font-semibold text-mallorca-purple">
                {t("app.title")}
              </span>
            </Link>
          </div>
          <div>
            {/* Language Dropdown */}
            <div
              className="flex md:block grow md:grow-0 justify-end basis-full md:basis-auto pt-3 md:pt-1 pb-1"
              id="navbarToggle"
            >
              <ul className="flex">
                <li className="relative group">
                  <button
                    type="button"
                    onClick={toggleDropdown}
                    className="text-gray-500 p-2 cursor-pointer flex items-center"
                  >
                    <div className="flex mr-2">
                      <span className="mx-3 text-lg text-mallorca-purple/75">
                        {language_title_img_map[i18n.language]?.language}
                      </span>
                      <span>
                        <img
                          src={language_title_img_map[i18n.language]?.img}
                          alt={t("app.title")}
                          className="inline-block h-5 w-8"
                        />
                      </span>
                    </div>
                    {/* <ChevronDown
                      className={`w-4 h-4 transition-transform duration-200 ${
                        open ? "rotate-180" : "rotate-0"
                      }`}
                    /> */}
                  </button>
                  {open && (
                    <ul className="absolute right-0 bg-white border-gray-300 shadow-sm shadow-black rounded z-50 min-w-40">
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
                              isSelected ? "bg-gray-200" : "bg-white"
                            } ${roundedClass}`}
                          >
                            <button
                              onClick={() => changeLanguage(key)}
                              className={`flex justify-between items-center gap-2 w-full text-mallorca-purple/75 hover:bg-gray-100 px-4 py-2 ${roundedClass}`}
                            >
                              <div className="text-lg text-left">
                                {lang?.language}
                              </div>
                              <img
                                src={lang?.img}
                                alt={t("app.title")}
                                className="inline-block h-5 w-8"
                              />
                            </button>
                          </li>
                        );
                      })}
                    </ul>
                  )}
                </li>
              </ul>
            </div>

            {/* Hamburger Menu */}
            {/* <div className="">
              <button
                type="button"
                className="js-dropdown md:hidden rounded cursor-pointer"
                data-dropdown-keepopen="true"
                aria-label={t("navigation.toggle")}
                aria-controls="navbarToggle"
                aria-expanded="false"
              >
                <div className="space-y-1.5 my-2.5 mx-4">
                  <div className="w-6 h-0.5 bg-gray-500"></div>
                  <div className="w-6 h-0.5 bg-gray-500"></div>
                  <div className="w-6 h-0.5 bg-gray-500"></div>
                </div>
              </button>
            </div> */}
          </div>
        </div>
      </nav>
    </header>
  );
}
