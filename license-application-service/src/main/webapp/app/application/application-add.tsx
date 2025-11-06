import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router';
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

export default function ApplicationAdd() {
  const { t } = useTranslation();
  useDocumentTitle(t('application.add.headline'));

  const navigate = useNavigate();
  const [userValues, setUserValues] = useState<Record<string,string>>({});

  const useFormResult = useForm({
    resolver: yupResolver(getSchema()),
  });

  const prepareRelations = async () => {
    try {
      const userValuesResponse = await axios.get('/api/applications/userValues');
      setUserValues(userValuesResponse.data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  useEffect(() => {
    prepareRelations();
  }, []);

  const createApplication = async (data: ApplicationDTO) => {
    window.scrollTo(0, 0);
    try {
      await axios.post('/api/applications', data);
      navigate('/applications', {
            state: {
              msgSuccess: t('application.create.success')
            }
          });
    } catch (error: any) {
      handleServerError(error, navigate, useFormResult.setError, t);
    }
  };

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('application.add.headline')}</h1>
      <div>
        <Link to="/applications" className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-4 rounded px-5 py-2">{t('application.add.back')}</Link>
      </div>
    </div>
    <form onSubmit={useFormResult.handleSubmit(createApplication)} noValidate>
      <InputRow useFormResult={useFormResult} object="application" field="licenseType" required={true} />
      <InputRow useFormResult={useFormResult} object="application" field="cadastralReference" />
      <InputRow useFormResult={useFormResult} object="application" field="appliedAt" />
      <InputRow useFormResult={useFormResult} object="application" field="changedAt" />
      <InputRow useFormResult={useFormResult} object="application" field="status" />
      <InputRow useFormResult={useFormResult} object="application" field="paymentStatus" />
      <InputRow useFormResult={useFormResult} object="application" field="user" type="select" options={userValues} />
      <input type="submit" value={t('application.add.headline')} className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2 cursor-pointer mt-6" />
    </form>
  </>);
}
