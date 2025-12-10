export const formatAmount = (raw: number | undefined, t: (k:string)=>string): string => {
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

export const formatDate = (raw: string | undefined, t: (k:string)=>string): string => {
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