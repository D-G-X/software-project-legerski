import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router';
import { handleServerError, setYupDefaults } from 'app/common/utils';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { ApplicationDTO } from 'app/application/application-model';
import axios from 'axios';
import InputRow from 'app/common/input-row/input-row';
import useDocumentTitle from 'app/common/use-document-title';
import * as yup from 'yup';


function getSchema() {
  setYupDefaults();
  return yup.object({
    licenseType: yup.string().emptyToNull().max(255).required(),
    cadastralReference: yup.string().emptyToNull().max(20),
    appliedAt: yup.string().emptyToNull().offsetDateTime(),
    changedAt: yup.string().emptyToNull().offsetDateTime(),
    status: yup.string().emptyToNull().max(255),
    paymentStatus: yup.string().emptyToNull().max(255),
    user: yup.string().emptyToNull().uuid()
  });
}

export default function ApplicationEdit() {
  const { t } = useTranslation();
  useDocumentTitle(t('application.edit.headline'));

  const navigate = useNavigate();
  const [userValues, setUserValues] = useState<Record<string,string>>({});
  const params = useParams();
  const currentId = +params.id!;

  const useFormResult = useForm({
    resolver: yupResolver(getSchema()),
  });

  const prepareForm = async () => {
    try {
      const userValuesResponse = await axios.get('/api/applications/userValues');
      setUserValues(userValuesResponse.data);
      const data = (await axios.get('/api/applications/' + currentId)).data;
      useFormResult.reset(data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  useEffect(() => {
    prepareForm();
  }, []);

  const updateApplication = async (data: ApplicationDTO) => {
    window.scrollTo(0, 0);
    try {
      await axios.put('/api/applications/' + currentId, data);
      navigate('/applications', {
            state: {
              msgSuccess: t('application.update.success')
            }
          });
    } catch (error: any) {
      handleServerError(error, navigate, useFormResult.setError, t);
    }
  };

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('application.edit.headline')}</h1>
      <div>
        <Link to="/applications" className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-4 rounded px-5 py-2">{t('application.edit.back')}</Link>
      </div>
    </div>
    <form onSubmit={useFormResult.handleSubmit(updateApplication)} noValidate>
      <InputRow useFormResult={useFormResult} object="application" field="id" disabled={true} type="number" />
      <InputRow useFormResult={useFormResult} object="application" field="licenseType" required={true} />
      <InputRow useFormResult={useFormResult} object="application" field="cadastralReference" />
      <InputRow useFormResult={useFormResult} object="application" field="appliedAt" />
      <InputRow useFormResult={useFormResult} object="application" field="changedAt" />
      <InputRow useFormResult={useFormResult} object="application" field="status" />
      <InputRow useFormResult={useFormResult} object="application" field="paymentStatus" />
      <InputRow useFormResult={useFormResult} object="application" field="user" type="select" options={userValues} />
      <input type="submit" value={t('application.edit.headline')} className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2 cursor-pointer mt-6" />
    </form>
  </>);
}
