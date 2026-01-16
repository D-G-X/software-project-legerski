import {AuthContext} from "app/common/auth/AuthContext";
import {getDateWithDelta, toISOStringFromDateInput} from "app/common/utils";
import {isValidDateString} from "app/common/validationRules";
import {useCreateBallotPeriod} from "app/services/ballot-periods/ballot-periods";
import React, {useContext, useState} from "react";
import {useNavigate} from "react-router";
import {useTranslation} from "react-i18next";
import {useGlobalLoader} from "app/common/GlobalLoader";

export default function BallotConfig() {
  const {show, hide} = useGlobalLoader();
  const {t} = useTranslation();
  const navigate = useNavigate();
  const auth = useContext(AuthContext);

  const [form, setForm] = useState({
    ballot_start_date: getDateWithDelta(1),
    ballot_end_date: getDateWithDelta(2),
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const {name, value} = e.target;

    setForm((prevForm) => ({
      ...prevForm,
      [name]: value,
    }));
  };

  const cancelChanges = () => {
    navigate(`/ballot-dashboard`);
  };

  const {mutateAsync: createBallotPeriod, isPending} = useCreateBallotPeriod({
    axios: {
      headers: {
        Authorization: `Bearer ${auth?.accessToken}`,
      },
    },
  });

  const handleSubmit = async () => {
    const {ballot_start_date, ballot_end_date} = form;

    if (!ballot_start_date || !ballot_end_date) {
      alert(t("ballotConfig.alerts.requiredDates"));
      return;
    }

    if (
        !isValidDateString(ballot_start_date) ||
        !isValidDateString(ballot_end_date)
    ) {
      alert(t("ballotConfig.alerts.invalidDate"));
      return;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const startDate = new Date(ballot_start_date);
    const endDate = new Date(ballot_end_date);

    if (startDate <= today) {
      alert(t("ballotConfig.alerts.startDateAfterToday"));
      return;
    }
    if (startDate.getTime() === endDate.getTime()) {
      alert(t("ballotConfig.alerts.sameStartEndDate"));
      return;
    }
    if (startDate > endDate) {
      alert(t("ballotConfig.alerts.startDateAfterEndDate"));
      return;
    }

    const maxEndDate = new Date(startDate);
    maxEndDate.setMonth(maxEndDate.getMonth() + 6);

    if (endDate > maxEndDate) {
      alert(t("ballotConfig.alerts.endDateTooLate"));
      return;
    }

    try {
      show();
      const response = await createBallotPeriod({
        data: {
          start_date: toISOStringFromDateInput(ballot_start_date),
          end_date: toISOStringFromDateInput(ballot_end_date),
        },
      });

      switch (response.status) {
        case 201:
        case 200:
          alert(t("ballotConfig.alerts.success"));
          navigate("/");
          break;

        default:
          alert(t("ballotConfig.alerts.creationFailed"));
          break;
      }

      return true;
    } catch (error: any) {
      const status = error?.response?.status;

      switch (status) {
        case 409:
          alert(t("ballotConfig.alerts.overlappingPeriod"));
          break;

        case 400:
          alert(t("ballotConfig.alerts.badRequest"));
          break;

        case 401:
          alert(t("ballotConfig.alerts.unauthorized"));
          break;

        default:
          alert(
              error?.response?.data?.message ??
              t("ballotConfig.alerts.creationFailed")
          );
          break;
      }

      return false;
    } finally {
      hide();
    }
  };
  return (
      <div className="container mx-auto px-4 md:px-6">
        <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
          <div className="text-center">
            <div className="text-4xl font-bold text-mallorca-purple w-full mb-10 text-center">
              {t("ballotConfig.pageTitle")}
            </div>

            <div className="py-5 px-3 border rounded-2xl mb-10">
              <div className="text-md text-mallorca-purple gap-10 flex mb-10">
                <label
                    className="w-[50%] items-center py-2"
                    htmlFor="ballot_start_date"
                >
                  {t("ballotConfig.formFields.startDate")}
                </label>
                <input
                    id="ballot_start_date"
                    name="ballot_start_date"
                    type="date"
                    value={form.ballot_start_date}
                    onChange={handleChange}
                    min={getDateWithDelta(1)}
                    className="accent-matte-grey checked:bg-black p-2 rounded mr-2 w-[50%]"
                />
              </div>

              <div className="text-md text-mallorca-purple gap-10 flex ">
                <label
                    className="w-[50%] items-center py-2"
                    htmlFor="ballot_end_date"
                >
                  {t("ballotConfig.formFields.endDate")}
                </label>
                <input
                    id="ballot_end_date"
                    name="ballot_end_date"
                    type="date"
                    value={form.ballot_end_date}
                    onChange={handleChange}
                    min={getDateWithDelta(2)}
                    className="accent-matte-grey checked:bg-black p-2 rounded mr-2 w-[50%]"
                />
              </div>
            </div>

            <div className={`pt-5 flex justify-between px-5 gap-10`}>
              <button
                  onClick={handleSubmit}
                  disabled={isPending}
                  className="px-15 py-1 border-2 border-mallorca-purple bg-mallorca-purple rounded-lg text-white disabled:opacity-50"
              >
                {isPending
                    ? t("ballotConfig.buttons.publishing")
                    : t("ballotConfig.buttons.publish")}
              </button>
              <button
                  onClick={cancelChanges}
                  className="px-15 py-1 bg-white border-2 border-mallorca-purple rounded-lg text-mallorca-purple"
              >
                {t("ballotConfig.buttons.cancel")}
              </button>
            </div>
          </div>
        </div>
      </div>
  );
}
