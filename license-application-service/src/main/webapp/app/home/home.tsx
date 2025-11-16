import React from 'react';
import { useTranslation } from 'react-i18next';
import useDocumentTitle from 'app/common/use-document-title';
import './home.css';


export default function Home() {
  const { t } = useTranslation();
  useDocumentTitle(t('home.index.headline'));

  return (<>
      <div className="relative min-h-[calc(100vh-4rem)]">
          <h1
              className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2
                   text-[256px] font-extrabold text-center"
              style={{ color: '#351341' }}
          >
              {t('home.index.headline')}
          </h1>
      </div>
  </>);
}