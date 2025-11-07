import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router';
import { ChevronDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';


export default function Header() {
  const { t, i18n } = useTranslation();
  const [open, setOpen] = useState(false);

  const toggleDropdown = () => setOpen((prev) => !prev);
  const changeLanguage = (lng: string) => {
      i18n.changeLanguage(lng);
      setOpen(false);
  };

  const headerRef = useRef<HTMLElement|null>(null);

  const handleClick = (event: Event) => {
    // close any open dropdown
    const $clickedDropdown = (event.target as HTMLElement).closest('.js-dropdown');
    const $dropdowns = headerRef.current!.querySelectorAll('.js-dropdown');
    $dropdowns.forEach(($dropdown:Element) => {
      if ($clickedDropdown !== $dropdown && $dropdown.getAttribute('data-dropdown-keepopen') !== 'true') {
        $dropdown.ariaExpanded = 'false';
        $dropdown.nextElementSibling!.classList.add('hidden');
      }
    });
    // toggle selected if applicable
    if ($clickedDropdown) {
      $clickedDropdown.ariaExpanded = '' + ($clickedDropdown.ariaExpanded !== 'true');
      $clickedDropdown.nextElementSibling!.classList.toggle('hidden');
    }
  };

  useEffect(() => {
    document.body.addEventListener('click', handleClick);
    return () => document.body.removeEventListener('click', handleClick);
  }, []);

  return (
    <header ref={headerRef} className="bg-gray-50">
      <div className="container mx-auto px-4 md:px-6">
        <nav className="flex flex-wrap items-center justify-between py-2">
          <Link to="/" className="flex py-1.5 mr-4">
            <img src="/images/logo.svg" alt={t('app.title')} width="100" height="100" className="inline-block" />
            <span className="text-xl pl-3">{t('app.title')}</span>
          </Link>
          <button type="button" className="js-dropdown md:hidden border rounded cursor-pointer" data-dropdown-keepopen="true"
              aria-label={t('navigation.toggle')} aria-controls="navbarToggle" aria-expanded="false">
            <div className="space-y-1.5 my-2.5 mx-4">
              <div className="w-6 h-0.5 bg-gray-500"></div>
              <div className="w-6 h-0.5 bg-gray-500"></div>
              <div className="w-6 h-0.5 bg-gray-500"></div>
            </div>
          </button>
          <div className="hidden md:block flex grow md:grow-0 justify-end basis-full md:basis-auto pt-3 md:pt-1 pb-1" id="navbarToggle">
              <ul className="flex">

                  {/* Language Dropdown */}
                  <li className="relative group">
                      <button
                          type="button"
                          onClick={toggleDropdown}
                          className="block text-gray-500 p-2 cursor-pointer flex items-center"
                      >
                          <span className="mr-1">{i18n.language.toUpperCase()}</span>
                          <ChevronDown
                              className={`w-4 h-4 transition-transform duration-200 ${
                                  open ? 'rotate-90' : 'rotate-0'
                              }`}
                            />
                      </button>
                      {open && (
                          <ul className="hidden group-hover:block absolute right-0 bg-white border border-gray-300 rounded min-w-[6rem] py-2 z-50">
                              <li>
                                  <button
                                      onClick={() => open && changeLanguage('en')}
                                      className="block w-full text-left px-4 py-1 hover:bg-gray-200"
                                  >
                                      <img src="/images/languages/english.png" alt={t('app.title')} width="50" className="inline-block" />
                                  </button>
                              </li>
                              <li>
                                  <button
                                      onClick={() => open && changeLanguage('es')}
                                      className="block w-full text-left px-4 py-1 hover:bg-gray-200"
                                  >
                                      <img src="/images/languages/spanish.png" alt={t('app.title')} width="50" className="inline-block" />
                                  </button>
                              </li>
                          </ul>
                        )}
                  </li>
              </ul>
          </div>
        </nav>
      </div>
    </header>
  );
}
