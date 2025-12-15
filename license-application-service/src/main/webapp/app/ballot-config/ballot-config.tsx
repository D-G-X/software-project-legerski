import { getDateWithDelta } from "app/common/utils";
import { isValidDateString } from "app/common/validationRules";
import React, { useState } from "react";
import { useNavigate } from "react-router";

export default function BallotConfig() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    ballot_start_date: getDateWithDelta(1),
    ballot_end_date: getDateWithDelta(2),
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    setForm((prevForm) => ({
      ...prevForm,
      [name]: value,
    }));
  };

  const cancelChanges = () => {
    // Call fetch API to fetch new values
    navigate(`/ballot-dashboard`);
  };

  const handleSubmit = () => {
    const { ballot_start_date, ballot_end_date } = form;

    if (!ballot_start_date || !ballot_end_date) {
      alert("Both start date and end date are required.");
      return;
    }

    if (
      !isValidDateString(ballot_start_date) ||
      !isValidDateString(ballot_end_date)
    ) {
      alert("Invalid date format. Please use a valid date.");
      return;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const startDate = new Date(ballot_start_date);
    const endDate = new Date(ballot_end_date);

    if (startDate <= today) {
      alert("Start date must be later than today.");
      return;
    }
    if (startDate.getTime() === endDate.getTime()) {
      alert("Start date and end date cannot be the same.");
      return;
    }
    if (startDate > endDate) {
      alert("Start date cannot be later than end date.");
      return;
    }

    const maxEndDate = new Date(startDate);
    maxEndDate.setMonth(maxEndDate.getMonth() + 6);

    if (endDate > maxEndDate) {
      alert("End date cannot be more than 6 months after the start date.");
      return;
    }

    alert("API call has to be implemented!");
  };
  return (
    <div className="container mx-auto px-4 md:px-6">
      <div className="relative min-h-[calc(100vh-8rem)] bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="text-4xl font-bold text-mallorca-purple w-full mb-10 text-center">
            Configure New Ballot
          </div>

          <div className="py-5 px-3 border rounded-2xl mb-10">
            {/* Ballot Start Date */}
            <div className="text-md text-mallorca-purple gap-10 flex mb-10">
              <label
                className="w-[50%] items-center py-2"
                htmlFor="ballot_start_date"
              >
                Start Date:
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

            {/* Ballot End Date */}
            <div className="text-md text-mallorca-purple gap-10 flex ">
              <label
                className="w-[50%] items-center py-2"
                htmlFor="ballot_end_date"
              >
                End Date:
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
              className="px-15 py-1 border-2 border-mallorca-purple bg-mallorca-purple rounded-lg text-white"
            >
              Publish Ballot
            </button>
            <button
              onClick={cancelChanges}
              className="px-15 py-1 bg-white border-2 border-mallorca-purple rounded-lg text-mallorca-purple"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
