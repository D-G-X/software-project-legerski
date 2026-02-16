import {jsPDF} from "jspdf";

export async function downloadPdfOfficialDocument({downloadFileName, title, text, t}:
                                                  {
                                                    downloadFileName: string,
                                                    title: string,
                                                    text: string,
                                                    t: (key: string) => string
                                                  }) {

  const doc = new jsPDF("p", "mm", "a4");
  const logo = new Image();
  const logoWidth = 20;
  logo.src = '/images/logo.webp';
  await new Promise((resolve) => {
    logo.onload = resolve
  });

  const pageWidth = doc.internal.pageSize.getWidth();
  const margin = 10;

  // --- Logo ---
  doc.addImage(
      logo,
      getImageType(logo.src),
      pageWidth - logoWidth - margin, //x
      margin, //y
      logoWidth,
      (logo.height / logo.width) * logoWidth
  );
  doc.setFontSize(8);
  doc.setFont("helvetica", "bold");
  const logoText = doc.splitTextToSize(t("app.title"), logoWidth);
  doc.text(logoText, pageWidth - margin, margin + ((logo.height / logo.width) * logoWidth) + 4, {
    align: "right",
  });

  // --- Titel ---
  doc.setFontSize(18);
  const titleLine = doc.splitTextToSize(title, pageWidth - (4 * margin));
  doc.text(titleLine, 15, 45);

  // --- Text ---
  doc.setFontSize(12);
  doc.setFont("helvetica", "normal");
  const textLines = doc.splitTextToSize(text, pageWidth - (4 * margin));
  doc.text(textLines, 15, 65);

  // --- Contact Info ---
  doc.setFontSize(10);
  const addressLines = doc.splitTextToSize(getAddressText(t), (pageWidth / 2) - (4 * margin));
  doc.text(addressLines, 15, 270);
  const contactLines = doc.splitTextToSize(getContactText(t), (pageWidth / 2) - (4 * margin));
  doc.text(contactLines, 15 + (pageWidth / 2) + margin, 270);

  doc.save(`${downloadFileName}.pdf`);
}

function getAddressText(t: (key: string) => string): string {
  return `${
      t("app.contact.legalName")}\n${
      t("app.contact.legalAddress.street")}\n${
      t("app.contact.legalAddress.zipCode")} ${t("app.contact.legalAddress.city")}\n${
      t("app.contact.legalAddress.country")}\n`;
}

function getContactText(t: (key: string) => string): string {
  return `${
      t("app.contact.email.label")}: ${t("app.contact.email.address")}\n${
      t("app.contact.phone.label")}: ${t("app.contact.phone.number")}\n${
      t("app.contact.website.label")}: ${t("app.contact.website.url")}`;
}

function getImageType(fileName: string): "PNG" | "JPEG" | "WEBP" {
  fileName = fileName.toLowerCase();
  if (fileName.endsWith(".png")) return "PNG";
  if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "JPEG";
  if (fileName.endsWith(".webp")) return "WEBP";
  if (fileName.endsWith(".svg")) return "PNG"; // jsPDF converts svg to png
  return "PNG"; // fallback
}
