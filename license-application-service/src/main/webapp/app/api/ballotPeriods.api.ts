import axios from "axios";

export interface BallotPeriod {
  ballot_period_id: number;
  start_date: string;
  end_date: string;
}

export const getBallotPeriodById = async (id: number) => {
  const token = localStorage.getItem("accessToken");

  const response = await axios.get(
    `/ballot-periods/${id}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  return response.data;
};
