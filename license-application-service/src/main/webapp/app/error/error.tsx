import React from 'react';
import {useLocation} from 'react-router';
import {useTranslation} from 'react-i18next';
import {getReasonPhrase} from 'http-status-codes';
import {useDocumentTitle} from "../common/utils";


export default function Error() {
  const {t} = useTranslation();
  useDocumentTitle(t('error.page.headline'));

  const location = useLocation();
  let status = '404';
  let error = getReasonPhrase(status);

  // keep 404 for every url except /error
  if (location.pathname === '/error') {
    status = location.state?.errorStatus || '503';
    error = location.state?.errorMessage || getReasonPhrase(status);
  }

  return (
      <div className="container mx-auto px-4 md:px-6">
        <div
            className="relative min-h-[calc(100vh-8rem)] bg-white flex flex-col items-center justify-center text-center">
          <h1 className="text-3xl md:text-4xl font-medium text-mallorca-purple mb-4">
            {status} - {error}
          </h1>
          <p className="text-xl text-gray-700">
            {t('error.page.message')}
          </p>
        </div>
      </div>
  );
}
