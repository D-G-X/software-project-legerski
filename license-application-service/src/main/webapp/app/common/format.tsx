type Unit = "second" | "minute" | "hour" | "day" | "month" | "year";

const BLUE_STATUSES = [
  "PAYMENT_RECEIVED",
  "SUBMITTED",
  "IN_BALLOT",
  "UNDER_REVIEW",
];

const GREEN_STATUSES = ["SELECTED", "APPROVED"];

const RED_STATUSES = ["REJECTED", "NOT_SELECTED"];

const GRAY_STATUSES = ["DRAFT", "EXPIRED", "CANCELLED"];

const ORANGE_STATUSES = ["DOCUMENTS_SUBMITTED", "VERIFICATION_PENDING", "AWAITING_PAYMENT"];


export const getApplicationStatusColor = (status: string | undefined | null): string => {
    if (!status || status == "") return "gray";

    if (BLUE_STATUSES.includes(status)) return "blue";
    if (GREEN_STATUSES.includes(status)) return "green";
    if (RED_STATUSES.includes(status)) return "red";
    if (GRAY_STATUSES.includes(status)) return "gray";
    if (ORANGE_STATUSES.includes(status)) return "orange";
    return "gray";
}
export const formatApplicationStatusLabel = (rawFixed: string, rawVariable: string | undefined | null): string => {
  if (!rawVariable || rawFixed == '') return rawFixed;
  return rawFixed + rawVariable.toLowerCase().replace(/_([a-z])/g, (_, c) => c.toUpperCase()) // Camel case
}

export const formatAmount = (raw: number | undefined | null, t: any): string | null => {
  if (!raw) return null;
  return new Intl.NumberFormat(t("locale"), {
    style: "currency",
    currency: "EUR",
    minimumFractionDigits: 2,
  }).format(raw);
};

export const formatIban = (raw?: string): string => {
  if (!raw) return "";

  const visible = 2;
  const clean = raw.replace(/\s+/g, "").toUpperCase();

  if (clean.length <= visible * 2) {
    return clean.replace(/(.{4})/g, "$1 ").trim();
  }

  const start = clean.slice(0, visible);
  const end = clean.slice(-visible);
  const masked = "•".repeat(clean.length - visible * 2);

  return `${start}${masked}${end}`
      .replace(/(.{4})/g, "$1 ")
      .trim();
};

export const formatBic = (raw: string | undefined): string => {
  if (raw === undefined || raw === "") return "";
  return raw.replace(/\s+/g, "").toUpperCase();
};

const parseUtcTimestamp = (raw: string | number): Date => {
  if (typeof raw === "number") return new Date(raw);

  const s = raw.trim();

  // already ISO format
  if (/[zZ]$|[+-]\d{2}:\d{2}$/.test(s)) return new Date(s);

  // convert to ISO format
  const m = s.match(
      /^(\d{4}-\d{2}-\d{2})[ T](\d{2}:\d{2}:\d{2})(?:\.(\d+))?$/
  );

  // JS supports milliseconds only
  if (m) {
    const [, date, time, frac] = m;
    const ms = (frac ?? "0").padEnd(3, "0").slice(0, 3);
    return new Date(`${date}T${time}.${ms}Z`);
  }

  // fallback
  return new Date(s);
};

export const formatDateLong = (raw: string | number | undefined, t: any): string => {
  if (raw === undefined || raw === "") return "";

  const date = parseUtcTimestamp(raw)

  return date.toLocaleString(t("locale"), {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
};

export const formatDateShort = (raw: string | number | undefined, t: any): string => {
  if (raw === undefined || raw === "") return "";

  const date = parseUtcTimestamp(raw)

  return date.toLocaleString(t("locale"), {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
};

export const formatRelativeDate = (rawIso: string | undefined, rawLocale: string = "en-US"): string => {
  if (rawIso === undefined || rawIso === "") return "";

  const date = parseUtcTimestamp(rawIso)

  const timeFormat = new Intl.RelativeTimeFormat(rawLocale, {
    numeric: "always",
    style: "short",
  });

  const diffSeconds = Math.round(
      (date.getTime() - Date.now()) / 1000
  );

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
      return timeFormat.format(
          Math.round(diffSeconds / seconds),
          unit
      );
    }
  }

  return timeFormat.format(0, "second");
};
