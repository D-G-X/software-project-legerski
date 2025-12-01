import React from 'react';
import ReactDOM from 'react-dom/client';
import { initReactI18next } from 'react-i18next';
import i18n from 'i18next';
import axios from 'axios';
import english from './locales/english.json';
import spanish from './locales/spanish.json';
import AppRoutes from './app/routes';
import './index.css';
import legalNoticeEnglish from './locales/legalNoticeEnglish.json';
import legalNoticeSpanish from './locales/legalNoticeSpanish.json';
import contactEnglish from './locales/contactEnglish.json';
import contactSpanish from './locales/contactSpanish.json';


i18n
  .use(initReactI18next)
  .init({
    resources: {
        en: {
            translation: english,
            legalNotice: legalNoticeEnglish,
            contact: contactEnglish
        },
        es: {
            translation: spanish,
            legalNotice: legalNoticeSpanish,
            contact: contactSpanish
        }
    },
    lng: 'en',
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false
    }
  });

axios.defaults.baseURL = process.env.API_PATH;

const root = document.getElementById('root')!!;
ReactDOM.createRoot(root).render(
  <AppRoutes />
);
