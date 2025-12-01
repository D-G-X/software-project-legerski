import React from "react";
import { useTranslation } from "react-i18next";
import LegalNoticeLayout from "./LegalNoticeLayout";

export default function LegalNotice() {
    const { t } = useTranslation("legalNotice");

    return (
        <LegalNoticeLayout>
            {/* LEGAL NOTICE */}
            <h1 className="text-3xl font-bold mb-6">{t("legalNoticeTitle")}</h1>
            <p className="text-sm mb-8">{t("lastUpdated")}</p>

            {/* 1 */}
            <h2 className="text-xl font-semibold mb-4">1. {t("owner.title")}</h2>
            <p>{t("owner.description")}</p>
            <ul className="list-disc ml-6 mb-6">
                <li>{t("owner.address")}</li>
                <li>{t("owner.email")}</li>
            </ul>
            <p className="mb-2">{t("governingLaw.description")}</p>
            <ul className="list-disc ml-6 mb-6">
                <li>{t("governingLaw.lssi")}</li>
                <li>{t("governingLaw.gdpr")}</li>
                <li>{t("governingLaw.regional")}</li>
                <li>{t("governingLaw.noAffiliation")}</li>
            </ul>

            {/* 2 */}
            <h2 className="text-xl font-semibold mb-4">2. {t("purpose.title")}</h2>
            <p className="mb-2">{t("purpose.description")}</p>
            <ul className="list-disc ml-6 mb-6">
                <li>{t("purpose.register")}</li>
                <li>{t("purpose.apply")}</li>
                <li>{t("purpose.upload")}</li>
                <li>{t("purpose.track")}</li>
                <li>{t("purpose.payments")}</li>
                <li>{t("purpose.settings")}</li>
            </ul>
            <p className="mb-6">{t("portalObjective.description")}</p>

            {/* 3 */}
            <h2 className="text-xl font-semibold mb-4">3. {t("terms.title")}</h2>

            <h3 className="font-semibold mt-4">3.1 {t("terms.acceptance.title")}</h3>
            <p className="mb-4">{t("terms.acceptance.text")}</p>

            <h3 className="font-semibold mt-4">3.2 {t("terms.obligations.title")}</h3>
            <p className="mb-1">{t("userObligations.intro")}</p>
            <ul className="list-disc ml-6 mb-4">
                <li> {t("terms.obligations.accurate")}</li>
                <li> {t("terms.obligations.validDocs")}</li>
                <li> {t("terms.obligations.noFraud")}</li>
                <li> {t("terms.obligations.confidentiality")}</li>
                <li> {t("terms.obligations.law")}</li>
            </ul>

            <h3 className="font-semibold mt-4">3.3 {t("terms.prohibited.title")}</h3>
            <p className="mb-1">{t("prohibitedActions.intro")}</p>
            <ul className="list-disc ml-6 mb-4">
                <li> {t("terms.prohibited.unauthorized")}</li>
                <li> {t("terms.prohibited.malware")}</li>
                <li> {t("terms.prohibited.bots")}</li>
                <li> {t("terms.prohibited.misrepresentation")}</li>
            </ul>

            <h3 className="font-semibold mt-4">3.4 {t("terms.ipr.title")}</h3>
            <p className="mb-4">{t("terms.ipr.text")}</p>

            <h3 className="font-semibold mt-4">3.5 {t("terms.disclaimer.title")}</h3>
            <p className="mb-1">{t("liability.intro")}</p>
            <ul className="list-disc ml-6 mb-6">
                <li> {t("terms.disclaimer.errors")}</li>
                <li> {t("terms.disclaimer.interruptions")}</li>
                <li> {t("terms.disclaimer.thirdparty")}</li>
                <li> {t("terms.disclaimer.delays")}</li>
            </ul>
            <p className="mb-1">{t("terms.disclaimer.asIs")}</p>

            {/* 4 */}
            <h2 className="text-xl font-semibold mb-4 mt-6">4. {t("privacy.title")}</h2>
            <p>{t("privacy.text")}</p>

            {/* 5 */}
            <h2 className="text-xl font-semibold mb-4 mt-6">5. {t("links.title")}</h2>
            <p>{t("links.text")}</p>

            {/* 6 */}
            <h2 className="text-xl font-semibold mb-4 mt-6">6. {t("modifications.title")}</h2>
            <p>{t("modifications.text")}</p>

            {/* 7 */}
            <h2 className="text-xl font-semibold mb-4 mt-6">7. {t("jurisdiction.title")}</h2>
            <p>{t("jurisdiction.text")}</p>

            {/* PRIVACY POLICY */}
            <h1 className="text-3xl font-bold mb-6 mt-10">{t("privacyPolicy.title")}</h1>
            <p className="text-sm mb-6">{t("privacyPolicy.lastUpdated")}</p>
            <h2 className="text-xl font-semibold mb-4 mt-6">1. {t("dataController.title")}</h2>
            <p>{t("dataController.description")}</p>
            <ul className="list-disc ml-6 mb-6">
                <li>{t("dataController.address")}</li>
                <li>{t("dataController.email")}</li>
            </ul>

            <h2 className="text-xl font-semibold mb-4 mt-6">2. {t("dataCollected.title")}</h2>

            <p className="mb-2">2.1 {t("dataCollected.accountInfo")}</p>
            <ul className="list-disc ml-6 mb-4">
                <li>{t("dataCollected.fullName")}</li>
                <li>{t("dataCollected.email")}</li>
                <li>{t("dataCollected.phone")}</li>
                <li>{t("dataCollected.password")}</li>
            </ul>

            <p className="mb-2">2.2 {t("dataCollected.propertyInfo")}</p>
            <ul className="list-disc ml-6 mb-4">
                <li>{t("dataCollected.propertyAddress")}</li>
                <li>{t("dataCollected.cadastralRef")}</li>
                <li>{t("dataCollected.licenseForms")}</li>
                <li>{t("dataCollected.idCopy")}</li>
            </ul>

            <p className="mb-2">2.3 {t("dataCollected.uploadedDocs")}</p>
            <ul className="list-disc ml-6 mb-4">
                <li>{t("dataCollected.idProof")}</li>
                <li>{t("dataCollected.propertyProof")}</li>
            </ul>

            <p className="mb-2">2.4 {t("dataCollected.technicalData")}</p>
            <ul className="list-disc ml-6 mb-4">
                <li>{t("dataCollected.ipAddress")}</li>
                <li>{t("dataCollected.browserDevice")}</li>
                <li>{t("dataCollected.loginActivity")}</li>
                <li>{t("dataCollected.usageAnalytics")}</li>
            </ul>

            <p className="mb-2">2.5 {t("dataCollected.paymentInfo")}</p>
            <ul className="list-disc ml-6 mb-6">
                <li>{t("dataCollected.accountHolder")}</li>
                <li>{t("dataCollected.ibanBic")}</li>
                <li>{t("dataCollected.paymentProcessor")}</li>
            </ul>
            <p className="mb-4">{t("security.paymentInfo")}</p>

            {/* 9. Purpose of Processing */}
            <h2 className="text-xl font-semibold mb-4 mt-6">3. {t("processingPurpose.title")}</h2>
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

            {/* 4. Data Storage Duration */}
            <h2 className="text-xl font-semibold mb-4 mt-6">4. {t("dataStorage.title")}</h2>
            <p className="mb-4">{t("dataStorage.description")}</p>
            <ul className="list-disc ml-6 mb-6">
                <li>{t("dataRetention.activeAccounts")}</li>
                <li>{t("dataRetention.licenseDocs")}</li>
                <li>{t("dataRetention.paymentReceipts")}</li>
                <li>{t("dataRetention.logData")}</li>
            </ul>

            {/* 5. User Rights */}
            <h2 className="text-xl font-semibold mb-4 mt-6">5. {t("userRights.title")}</h2>
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

            {/* 6. Security Measures */}
            <h2 className="text-xl font-semibold mb-4 mt-6">6. {t("security.title")}</h2>
            <p className="mb-2">{t("security.description")}</p>
            <ul className="list-disc ml-6 mb-6">
                <li>{t("security.accessControl")}</li>
                <li>{t("security.securityAssessments")}</li>
                <li>{t("security.cookiesPolicy")}</li>
            </ul>
            <p className="mb-2">{t("securityMeasures.noSystem")}</p>


            {/*<p className="mb-4">{t("security.dataRetention")}</p>*/}
            {/*<p className="mb-4">{t("security.noSystem")}</p>*/}

            {/* COOKIES */}
            <h1 className="text-3xl font-bold mb-6 mt-10">{t("cookies.title")}</h1>
            <p className="text-sm mb-6">{t("cookies.lastUpdated")}</p>

            <h2 className="text-xl font-semibold mb-4">1. {t("cookies.whatAreCookies")}</h2>
            <p className="mb-4">{t("cookies.definition")}</p>

            <h2 className="text-xl font-semibold mb-4">2. {t("cookies.typesTitle")}</h2>
            <p className="mb-2">2.1 {t("cookies.essential")}</p>

            <h2 className="text-xl font-semibold mb-4">3. {t("cookies.consentTitle")}</h2>
            <p className="mb-2">{t("cookies.consentText")}</p>
        </LegalNoticeLayout>
    );
}
