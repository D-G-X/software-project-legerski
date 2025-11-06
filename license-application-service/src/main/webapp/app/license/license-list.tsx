import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';
import { handleServerError } from 'app/common/utils';
import { LicenseDTO } from 'app/license/license-model';
import axios from 'axios';
import useDocumentTitle from 'app/common/use-document-title';


export default function LicenseList() {
  const { t } = useTranslation();
  useDocumentTitle(t('license.list.headline'));

  const [licenses, setLicenses] = useState<LicenseDTO[]>([]);
  const navigate = useNavigate();

  const getAllLicenses = async () => {
    try {
      const response = await axios.get('/api/licenses');
      setLicenses(response.data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  const confirmDelete = async (id: number) => {
    if (!confirm(t('delete.confirm'))) {
      return;
    }
    try {
      await axios.delete('/api/licenses/' + id);
      navigate('/licenses', {
            state: {
              msgInfo: t('license.delete.success')
            }
          });
      getAllLicenses();
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  useEffect(() => {
    getAllLicenses();
  }, []);

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('license.list.headline')}</h1>
      <div>
        <Link to="/licenses/add" className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2">{t('license.list.createNew')}</Link>
      </div>
    </div>
    {!licenses || licenses.length === 0 ? (
    <div>{t('license.list.empty')}</div>
    ) : (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr>
            <th scope="col" className="text-left p-2">{t('license.id.label')}</th>
            <th scope="col" className="text-left p-2">{t('license.licenseType.label')}</th>
            <th scope="col" className="text-left p-2">{t('license.licenseStatus.label')}</th>
            <th scope="col" className="text-left p-2">{t('license.issuedAt.label')}</th>
            <th scope="col" className="text-left p-2">{t('license.expiresAt.label')}</th>
            <th scope="col" className="text-left p-2">{t('license.user.label')}</th>
            <th scope="col" className="text-left p-2">{t('license.application.label')}</th>
            <th></th>
          </tr>
        </thead>
        <tbody className="border-t-2 border-black">
          {licenses.map((license) => (
          <tr key={license.id} className="odd:bg-gray-100">
            <td className="p-2">{license.id}</td>
            <td className="p-2">{license.licenseType}</td>
            <td className="p-2">{license.licenseStatus}</td>
            <td className="p-2">{license.issuedAt}</td>
            <td className="p-2">{license.expiresAt}</td>
            <td className="p-2">{license.user}</td>
            <td className="p-2">{license.application}</td>
            <td className="p-2">
              <div className="float-right whitespace-nowrap">
                <Link to={'/licenses/edit/' + license.id} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm">{t('license.list.edit')}</Link>
                <span> </span>
                <button type="button" onClick={() => confirmDelete(license.id!)} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm cursor-pointer">{t('license.list.delete')}</button>
              </div>
            </td>
          </tr>
          ))}
        </tbody>
      </table>
    </div>
    )}
  </>);
}
