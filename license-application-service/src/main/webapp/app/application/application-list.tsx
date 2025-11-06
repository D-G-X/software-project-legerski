import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';
import { handleServerError } from 'app/common/utils';
import { ApplicationDTO } from 'app/application/application-model';
import axios from 'axios';
import useDocumentTitle from 'app/common/use-document-title';


export default function ApplicationList() {
  const { t } = useTranslation();
  useDocumentTitle(t('application.list.headline'));

  const [applications, setApplications] = useState<ApplicationDTO[]>([]);
  const navigate = useNavigate();

  const getAllApplications = async () => {
    try {
      const response = await axios.get('/api/applications');
      setApplications(response.data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  const confirmDelete = async (id: number) => {
    if (!confirm(t('delete.confirm'))) {
      return;
    }
    try {
      await axios.delete('/api/applications/' + id);
      navigate('/applications', {
            state: {
              msgInfo: t('application.delete.success')
            }
          });
      getAllApplications();
    } catch (error: any) {
      if (error?.response?.data?.code === 'REFERENCED') {
        const messageParts = error.response.data.message.split(',');
        navigate('/applications', {
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
    getAllApplications();
  }, []);

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('application.list.headline')}</h1>
      <div>
        <Link to="/applications/add" className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2">{t('application.list.createNew')}</Link>
      </div>
    </div>
    {!applications || applications.length === 0 ? (
    <div>{t('application.list.empty')}</div>
    ) : (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr>
            <th scope="col" className="text-left p-2">{t('application.id.label')}</th>
            <th scope="col" className="text-left p-2">{t('application.licenseType.label')}</th>
            <th scope="col" className="text-left p-2">{t('application.cadastralReference.label')}</th>
            <th scope="col" className="text-left p-2">{t('application.appliedAt.label')}</th>
            <th scope="col" className="text-left p-2">{t('application.changedAt.label')}</th>
            <th scope="col" className="text-left p-2">{t('application.status.label')}</th>
            <th scope="col" className="text-left p-2">{t('application.paymentStatus.label')}</th>
            <th scope="col" className="text-left p-2">{t('application.user.label')}</th>
            <th></th>
          </tr>
        </thead>
        <tbody className="border-t-2 border-black">
          {applications.map((application) => (
          <tr key={application.id} className="odd:bg-gray-100">
            <td className="p-2">{application.id}</td>
            <td className="p-2">{application.licenseType}</td>
            <td className="p-2">{application.cadastralReference}</td>
            <td className="p-2">{application.appliedAt}</td>
            <td className="p-2">{application.changedAt}</td>
            <td className="p-2">{application.status}</td>
            <td className="p-2">{application.paymentStatus}</td>
            <td className="p-2">{application.user}</td>
            <td className="p-2">
              <div className="float-right whitespace-nowrap">
                <Link to={'/applications/edit/' + application.id} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm">{t('application.list.edit')}</Link>
                <span> </span>
                <button type="button" onClick={() => confirmDelete(application.id!)} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm cursor-pointer">{t('application.list.delete')}</button>
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
