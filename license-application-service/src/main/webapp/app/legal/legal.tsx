import React from "react";
import { useTranslation } from "react-i18next";
import { FormHeader } from "../common/headingTitle";
import { Section, SubSection } from "app/common/legalSection";

export default function LegalNotice() {
  const { t } = useTranslation("legalNotice");

  const legalConfig = [
    {
      title: t("owner.title"),
      content: [
        <p key="o1">{t("owner.description")}</p>,
        <ul key="o2" className="list-disc ml-6 mb-6">
          <li>{t("owner.address")}</li>
          <li>{t("owner.email")}</li>
        </ul>,
        <p key="o3" className="mb-2">
          {t("governingLaw.description")}
        </p>,
        <ul key="o4" className="list-disc ml-6 mb-6">
          <li>{t("governingLaw.lssi")}</li>
          <li>{t("governingLaw.gdpr")}</li>
          <li>{t("governingLaw.regional")}</li>
          <li>{t("governingLaw.noAffiliation")}</li>
        </ul>,
      ],
    },
    {
      title: t("purpose.title"),
      content: [
        <p key="p1">{t("purpose.description")}</p>,
        <ul key="p2" className="list-disc ml-6 mb-6">
          <li>{t("purpose.usage")}</li>
          <li>{t("purpose.limits")}</li>
        </ul>,
      ],
    },
    {
      title: t("terms.title"),
      subsections: [
        {
          title: t("terms.acceptance.title"),
          content: <p>{t("terms.acceptance.text")}</p>,
        },
        {
          title: t("terms.obligations.title"),
          content: (
            <>
              <p className="mb-1">{t("userObligations.intro")}</p>
              <ul className="list-disc ml-6 mb-4">
                <li>{t("terms.obligations.accurate")}</li>
                <li>{t("terms.obligations.validDocs")}</li>
                <li>{t("terms.obligations.noFraud")}</li>
                <li>{t("terms.obligations.confidentiality")}</li>
                <li>{t("terms.obligations.law")}</li>
              </ul>
            </>
          ),
        },
        {
          title: t("terms.prohibited.title"),
          content: (
            <>
              <p className="mb-1">{t("prohibitedActions.intro")}</p>
              <ul className="list-disc ml-6 mb-4">
                <li>{t("terms.prohibited.unauthorized")}</li>
                <li>{t("terms.prohibited.malware")}</li>
                <li>{t("terms.prohibited.bots")}</li>
                <li>{t("terms.prohibited.misrepresentation")}</li>
              </ul>
            </>
          ),
        },
        {
          title: t("terms.ipr.title"),
          content: <p className="mb-4">{t("terms.ipr.text")}</p>,
        },
        {
          title: t("terms.disclaimer.title"),
          content: (
            <>
              <p className="mb-1">{t("liability.intro")}</p>
              <ul className="list-disc ml-6 mb-6">
                <li>{t("terms.disclaimer.errors")}</li>
                <li>{t("terms.disclaimer.interruptions")}</li>
                <li>{t("terms.disclaimer.thirdparty")}</li>
                <li>{t("terms.disclaimer.delays")}</li>
              </ul>
              <p className="mb-1">{t("terms.disclaimer.asIs")}</p>
            </>
          ),
        },
      ],
    },
    {
      title: t("privacy.title"),
      content: <p>{t("privacy.text")}</p>,
    },
    {
      title: t("links.title"),
      content: <p>{t("links.text")}</p>,
    },
    {
      title: t("modifications.title"),
      content: <p>{t("modifications.text")}</p>,
    },
    {
      title: t("jurisdiction.title"),
      content: <p>{t("jurisdiction.text")}</p>,
    },
  ];

  const privacyConfig = [
    {
      title: t("dataController.title"),
      content: (
        <>
          <p>{t("dataController.description")}</p>
          <ul className="list-disc ml-6 mb-6">
            <li>{t("dataController.address")}</li>
            <li>{t("dataController.email")}</li>
          </ul>
        </>
      ),
    },
    {
      title: t("dataCollected.title"),
      subsections: [
        {
          title: t("dataCollected.accountInfo"),
          content: (
            <ul className="list-disc ml-6 mb-4">
              <li>{t("dataCollected.fullName")}</li>
              <li>{t("dataCollected.email")}</li>
              {/* <li>{t("dataCollected.phone")}</li> */}
              <li>{t("dataCollected.password")}</li>
            </ul>
          ),
        },
        {
          title: t("dataCollected.propertyInfo"),
          content: (
            <ul className="list-disc ml-6 mb-4">
              <li>{t("dataCollected.propertyAddress")}</li>
              <li>{t("dataCollected.cadastralRef")}</li>
              <li>{t("dataCollected.licenseForms")}</li>
              <li>{t("dataCollected.idCopy")}</li>
            </ul>
          ),
        },
        {
          title: t("dataCollected.uploadedDocs"),
          content: (
            <ul className="list-disc ml-6 mb-4">
              <li>{t("dataCollected.idProof")}</li>
              <li>{t("dataCollected.propertyProof")}</li>
            </ul>
          ),
        },
        {
          title: t("dataCollected.technicalData"),
          content: (
            <ul className="list-disc ml-6 mb-4">
              <li>{t("dataCollected.ipAddress")}</li>
              <li>{t("dataCollected.browserDevice")}</li>
              <li>{t("dataCollected.loginActivity")}</li>
              <li>{t("dataCollected.usageAnalytics")}</li>
            </ul>
          ),
        },
        {
          title: t("dataCollected.paymentInfo"),
          content: (
            <>
              <ul className="list-disc ml-6 mb-6">
                <li>{t("dataCollected.accountHolder")}</li>
                <li>{t("dataCollected.ibanBic")}</li>
                <li>{t("dataCollected.paymentProcessor")}</li>
              </ul>
              <p className="mb-4">{t("security.paymentInfo")}</p>
            </>
          ),
        },
      ],
    },
    {
      title: t("processingPurpose.title"),
      content: (
        <>
          <p className="mb-2">{t("processingPurpose.description")}</p>
          <ul className="list-disc ml-6 mb-6">
            <li>{t("processingPurpose.createAccounts")}</li>
            <li>{t("processingPurpose.processLicenses")}</li>
            <li>{t("processingPurpose.verifyIdentity")}</li>
            <li>{t("processingPurpose.sendNotifications")}</li>
            <li>{t("processingPurpose.ballotAlgorithm")}</li>
            <li>{t("processingPurpose.generateReceipts")}</li>
            <li>{t("processingPurpose.ensureSecurity")}</li>
          </ul>
          <p className="mb-2">{t("processingPurpose.basisTitle")}</p>
          <ul className="list-disc ml-6 mb-6">
            <li>{t("processingPurpose.gdprContract")}</li>
            <li>{t("processingPurpose.gdprLegal")}</li>
            <li>{t("processingPurpose.gdprConsent")}</li>
            <li>{t("processingPurpose.gdprPublicInterest")}</li>
          </ul>
        </>
      ),
    },
    {
      title: t("dataStorage.title"),
      content: (
        <>
          <p className="mb-4">{t("dataStorage.description")}</p>
          <ul className="list-disc ml-6 mb-6">
            <li>{t("dataRetention.activeAccounts")}</li>
            <li>{t("dataRetention.licenseDocs")}</li>
            <li>{t("dataRetention.paymentReceipts")}</li>
            <li>{t("dataRetention.logData")}</li>
          </ul>
        </>
      ),
    },
    {
      title: t("userRights.title"),
      content: (
        <>
          <p className="mb-2">{t("userRights.description")}</p>
          <ul className="list-disc ml-6 mb-6">
            <li>{t("userRights.access")}</li>
            <li>{t("userRights.correction")}</li>
            <li>{t("userRights.restriction")}</li>
            <li>{t("userRights.portability")}</li>
            <li>{t("userRights.objection")}</li>
            <li>{t("userRights.withdrawal")}</li>
          </ul>
          <p className="mb-4">{t("userRights.contact")}</p>
        </>
      ),
    },
    {
      title: t("security.title"),
      content: (
        <>
          <p className="mb-2">{t("security.description")}</p>
          <ul className="list-disc ml-6 mb-6">
            <li>{t("security.accessControl")}</li>
            <li>{t("security.securityAssessments")}</li>
            <li>{t("security.cookiesPolicy")}</li>
          </ul>
          <p className="mb-2">{t("securityMeasures.noSystem")}</p>
        </>
      ),
    },
  ];

  const cookiesConfig = [
    {
      title: t("cookies.whatAreCookies"),
      content: <p className="mb-4">{t("cookies.definition")}</p>,
    },
    {
      title: t("cookies.typesTitle"),
      subsections: [
        {
          title: t("cookies.essentialTitle"),
          content: t("cookies.essential"),
        },
      ],
    },
    {
      title: t("cookies.consentTitle"),
      content: <p className="mb-2">{t("cookies.consentText")}</p>,
    },
  ];

  return (
    <div className="grow justify-center px-20 font-inter pt-8 overflow-y-scroll">
      {/* Legal Section */}
      <div className=" bg-white">
        {/* LEGAL NOTICE */}
        <FormHeader
          heading={t("legalNoticeTitle")}
          subHeading={t("lastUpdated")}
        />

        {legalConfig.map((section, idx) => (
          <Section
            key={idx}
            title={idx + 1 + ".  " + section.title}
            children={
              <>
                {section.content && section.content}
                {section.subsections &&
                  section.subsections.map((sub, i) => (
                    <SubSection
                      key={i}
                      title={idx + 1 + "." + (i + 1) + "  " + sub.title}
                      children={sub.content}
                    />
                  ))}
              </>
            }
          />
        ))}
      </div>

      {/* Privacy Policy Section*/}
      <div className="my-8">
        {/* PRIVACY POLICY */}
        <FormHeader
          heading={t("privacyPolicy.title")}
          subHeading={t("privacyPolicy.lastUpdated")}
        />

        {privacyConfig.map((section, idx) => (
          <Section
            key={idx}
            title={idx + 1 + ".  " + section.title}
            children={
              <>
                {section.content && section.content}
                {section.subsections &&
                  section.subsections.map((sub, i) => (
                    <SubSection
                      key={i}
                      title={idx + 1 + "." + (i + 1) + "  " + sub.title}
                      children={sub.content}
                    />
                  ))}
              </>
            }
          />
        ))}
      </div>

      {/* Cookies Section */}
      <div className="my-8">
        {/* COOKIES POLICY */}
        <FormHeader
          heading={t("cookies.title")}
          subHeading={t("cookies.lastUpdated")}
        />

        {cookiesConfig.map((section, idx) => (
          <Section
            key={idx}
            title={idx + 1 + ".  " + section.title}
            children={
              <>
                {section.content && section.content}
                {section.subsections &&
                  section.subsections.map((sub, i) => (
                    <SubSection
                      key={i}
                      title={idx + 1 + "." + (i + 1) + "  " + sub.title}
                      children={sub.content}
                    />
                  ))}
              </>
            }
          />
        ))}
      </div>
    </div>
  );
}
