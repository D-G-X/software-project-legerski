import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';
import { handleServerError } from 'app/common/utils';
import { AppUserDTO } from 'app/app-user/app-user-model';
import axios from 'axios';
import useDocumentTitle from 'app/common/use-document-title';


export default function AppUserList() {
  const { t } = useTranslation();
  useDocumentTitle(t('appUser.list.headline'));

  const [appUsers, setAppUsers] = useState<AppUserDTO[]>([]);
  const navigate = useNavigate();

  const getAllAppUsers = async () => {
    try {
      const response = await axios.get('/api/appUsers');
      setAppUsers(response.data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  const confirmDelete = async (id: string) => {
    if (!confirm(t('delete.confirm'))) {
      return;
    }
    try {
      await axios.delete('/api/appUsers/' + id);
      navigate('/appUsers', {
            state: {
              msgInfo: t('appUser.delete.success')
            }
          });
      getAllAppUsers();
    } catch (error: any) {
      if (error?.response?.data?.code === 'REFERENCED') {
        const messageParts = error.response.data.message.split(',');
        navigate('/appUsers', {
              state: {
                msgError: t(messageParts[0]!, { id: messageParts[1]! })
              }
            });
        return;
      }
      handleServerError(error, navigate);
    }
  };

  useEffect(() => {
    getAllAppUsers();
  }, []);

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('appUser.list.headline')}</h1>
      <div>
        <Link to="/appUsers/add" className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2">{t('appUser.list.createNew')}</Link>
      </div>
    </div>
    {!appUsers || appUsers.length === 0 ? (
    <div>{t('appUser.list.empty')}</div>
    ) : (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr>
            <th scope="col" className="text-left p-2">{t('appUser.id.label')}</th>
            <th></th>
          </tr>
        </thead>
        <tbody className="border-t-2 border-black">
          {appUsers.map((appUser) => (
          <tr key={appUser.id} className="odd:bg-gray-100">
            <td className="p-2">{appUser.id}</td>
            <td className="p-2">
              <div className="float-right whitespace-nowrap">
                <Link to={'/appUsers/edit/' + appUser.id} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm">{t('appUser.list.edit')}</Link>
                <span> </span>
                <button type="button" onClick={() => confirmDelete(appUser.id!)} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm cursor-pointer">{t('appUser.list.delete')}</button>
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
