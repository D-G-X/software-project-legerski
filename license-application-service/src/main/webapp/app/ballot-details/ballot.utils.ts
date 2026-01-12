export function getBallotYear(startDate: string): string {
  return new Date(startDate).getFullYear().toString();
}

export function getApplicationPeriod(start: string, end: string): string {
  const s = new Date(start).toLocaleDateString("en-GB");
  const e = new Date(end).toLocaleDateString("en-GB");
  return `${s} - ${e}`;
}

export function getStatus(endDate: string): "active" | "upcoming" | "closed" {
  const now = new Date();
  const end = new Date(endDate);

  if (now < end) return "active";
  return "closed";
}

export function getDrawingDate(endDate: string): string {
  const d = new Date(endDate);
  d.setMonth(d.getMonth() + 1);
  return d.toLocaleDateString("en-GB");
}
