import {
    formatAmount,
    formatBic,
    formatDateLong,
    formatDateShort,
    formatIban,
    formatRelativeDate,
    formatStatusLabel,
    getApplicationStatusColor,
    getDocumentStatusColor,
    getLicenseStatusColor,
    getPaymentStatusColor,
} from "./utils";

const t = () => "en-US";

describe("formatTests", () => {
  beforeAll(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2025-01-01T00:00:00Z"));
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  test("getApplicationStatusColor", () => {
    expect(getApplicationStatusColor(undefined)).toBe("gray");
    expect(getApplicationStatusColor("")).toBe("gray");
    expect(getApplicationStatusColor("PAYMENT_RECEIVED")).toBe("blue");
    expect(getApplicationStatusColor("SELECTED")).toBe("green");
    expect(getApplicationStatusColor("REJECTED")).toBe("red");
    expect(getApplicationStatusColor("DRAFT")).toBe("gray");
    expect(getApplicationStatusColor("AWAITING_PAYMENT")).toBe("orange");
    expect(getApplicationStatusColor("UNKNOWN")).toBe("gray");
  });

  test("getLicenseStatusColor", () => {
    expect(getLicenseStatusColor(undefined)).toBe("gray");
    expect(getLicenseStatusColor("ACTIVE")).toBe("green");
    expect(getLicenseStatusColor("SUSPENDED")).toBe("red");
    expect(getLicenseStatusColor("EXPIRED")).toBe("gray");
    expect(getLicenseStatusColor("FOO")).toBe("gray");
  });

  test("getDocumentStatusColor", () => {
    expect(getDocumentStatusColor(undefined)).toBe("gray");
    expect(getDocumentStatusColor("VERIFIED")).toBe("green");
    expect(getDocumentStatusColor("REJECTED")).toBe("red");
    expect(getDocumentStatusColor("OTHER")).toBe("gray");
  });

  test("getPaymentStatusColor", () => {
    expect(getPaymentStatusColor(undefined)).toBe("gray");
    expect(getPaymentStatusColor("UNPAID")).toBe("blue");
    expect(getPaymentStatusColor("PAID")).toBe("green");
    expect(getPaymentStatusColor("OTHER")).toBe("gray");
  });

  test("formatStatusLabel", () => {
    expect(formatStatusLabel("A", undefined)).toBe("A");
    expect(formatStatusLabel("", "FOO")).toBe("");
    expect(formatStatusLabel("A", "FOO_BAR")).toBe("AfooBar");
  });

  test("formatAmount", () => {
    expect(formatAmount(undefined, t)).toBeNull();
    expect(formatAmount(1, t)).toBe("€1.00");
  });

  test("formatIban", () => {
    expect(formatIban()).toBe("");
    expect(formatIban("DE12")).toBe("DE12");
    expect(formatIban("DE123456")).toBe("DE•• ••56");
  });

  test("formatBic", () => {
    expect(formatBic(undefined)).toBe("");
    expect(formatBic("deut de ff")).toBe("DEUTDEFF");
  });

  test("formatDateLong", () => {
    expect(formatDateLong(undefined, t)).toBe("");
    expect(formatDateLong("2025-01-01 00:00:00", t)).toContain("01/01/2025");
  });

  test("formatDateShort", () => {
    expect(formatDateShort(undefined, t)).toBe("");
    expect(formatDateShort(1735689600000, t)).toBe("01/01/2025");
  });

  test("formatRelativeDate", () => {
    expect(formatRelativeDate(undefined)).toBe("");
    expect(formatRelativeDate("2025-01-01T00:00:10Z")).toBe("in 10 sec.");
    expect(formatRelativeDate("2024-12-31T23:59:50Z")).toBe("10 sec. ago");
  });
});