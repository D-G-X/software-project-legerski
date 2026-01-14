import { TFunction } from "i18next";

// Compute ballot status from start/end dates
export function getBallotStatus(
  startDate: Date,
  endDate: Date,
  now: Date = new Date(),
  t: TFunction
) {
  if (
    !(startDate instanceof Date) ||
    isNaN(startDate.getTime()) ||
    !(endDate instanceof Date) ||
    isNaN(endDate.getTime())
  ) {
    return t("ballotStatus.upcoming");
  }

  if (now >= startDate && now <= endDate) return t("ballotStatus.active");
  if (now > endDate) return t("ballotStatus.completed");
  return t("ballotStatus.upcoming");
}

// Return a Date offset by `days` after `endDate` (default 7 days)
export function getDrawDate(endDate: Date, days = 7): Date {
  const d = new Date(endDate);
  d.setDate(d.getDate() + days);
  return d;
}

// Return tailwind classes for a given raw status string
export function getStatusClass(status: string): string {
  switch (status) {
    case "Completed":
      return "text-green-800 bg-green-200";
    case "On Going":
    case "Active":
      return "text-orange-800 bg-orange-200";
    case "Upcoming":
      return "text-blue-800 bg-blue-200";
    default:
      return "text-gray-800 bg-gray-200";
  }
}
