type Unit = "second" | "minute" | "hour" | "day" | "month" | "year";

export const formatAmount = (raw: number | undefined, t: any): string => {
  if (raw === undefined) return "";
  return new Intl.NumberFormat(t("locale"), {
    style: "currency",
    currency: "EUR",
    minimumFractionDigits: 2,
  }).format(raw);
}

export const formatIban = (raw: string | undefined): string => {
  if (raw === undefined || raw === "") return "";
  const visibleLength = 2;
  const clean = raw.replace(/\s+/g, "").toUpperCase();
  const visible = clean.slice(-visibleLength);
  const masked = "•".repeat(clean.length - visibleLength);
  return (masked + visible).replace(/(.{4})/g, "$1 ").trim();
};

export const formatBic = (raw: string | undefined): string => {
  if (raw === undefined || raw === "") return "";
  return raw.replace(/\s+/g, "").toUpperCase();
}

export const formatDate = (raw: string | undefined, t: any): string => {
  if (raw === undefined || raw === "") return "";
  return new Date(raw).toLocaleString(t("locale"), {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

export const formatRelativeDate = (
    iso: string,
    locale: string = "en-US"
): string => {
  const timeFormat = new Intl.RelativeTimeFormat(locale, {
    numeric: "always",
    style: "short",
  });

  const diffSeconds = Math.round((new Date(iso).getTime() - Date.now()) / 1000);

  const abs = Math.abs(diffSeconds);

  const divisions: [Unit, number][] = [
    ["year", 60 * 60 * 24 * 365],
    ["month", 60 * 60 * 24 * 30],
    ["day", 60 * 60 * 24],
    ["hour", 60 * 60],
    ["minute", 60],
    ["second", 1],
  ];

  for (const [unit, seconds] of divisions) {
    if (abs >= seconds) {
      const value = Math.round(diffSeconds / seconds);
      return timeFormat.format(value, unit);
    }
  }

  return timeFormat.format(0, "second");
}
