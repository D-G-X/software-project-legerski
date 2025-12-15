import React, { useState } from "react";
import { useNavigate } from "react-router";

export default function BallotConfig() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    ballot_start_date: "",
    ballot_end_date: "",
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
    // validations to check the date values

    // submit API call
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
                value={form.ballot_start_date}
                onChange={handleChange}
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
