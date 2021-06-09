package de.rki.coronawarnapp.coronatest.type.common

import de.rki.coronawarnapp.covidcertificate.test.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import de.rki.coronawarnapp.vaccination.core.CertificatePersonIdentifier
import de.rki.coronawarnapp.vaccination.core.personIdentifier
import de.rki.coronawarnapp.vaccination.core.qrcode.QrCodeString
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale

data class TestCertificateContainer(
    internal val data: StoredTestCertificateData,
    private val qrCodeExtractor: TestCertificateQRCodeExtractor,
    val isUpdatingData: Boolean = false,
) : StoredTestCertificateData by data {

    @delegate:Transient
    private val certificateData: TestCertificateData by lazy {
        data.testCertificateQrCode!!.let { qrCodeExtractor.extract(it).testCertificateData }
    }

    val isPublicKeyRegistered: Boolean
        get() = data.publicKeyRegisteredAt != null

    val isCertificateRetrievalPending: Boolean
        get() = data.certificateReceivedAt == null

    val certificateId: String?
        get() {
            if (isCertificateRetrievalPending) return null
            return certificateData.certificate.testCertificateData.single().uniqueCertificateIdentifier
        }

    fun toTestCertificate(
        valueSet: TestCertificateValueSets?,
        userLocale: Locale = Locale.getDefault(),
    ): TestCertificate? {
        if (isCertificateRetrievalPending) return null

        val header = certificateData.header
        val certificate = certificateData.certificate
        val testCertificate = certificate.testCertificateData.single()

        return object : TestCertificate {
            override val personIdentifier: CertificatePersonIdentifier
                get() = certificate.personIdentifier

            override val firstName: String?
                get() = certificate.nameData.givenName
            override val lastName: String
                get() = certificate.nameData.familyName ?: certificate.nameData.familyNameStandardized

            override val dateOfBirth: LocalDate
                get() = certificate.dateOfBirth

            override val targetName: String
                get() = valueSet?.getDisplayText(testCertificate.targetId) ?: testCertificate.targetId
            override val testType: String
                get() = valueSet?.getDisplayText(testCertificate.testType) ?: testCertificate.testType
            override val testResult: String
                get() = valueSet?.getDisplayText(testCertificate.testResult) ?: testCertificate.testResult
            override val testName: String?
                get() = testCertificate.testName?.let { valueSet?.getDisplayText(it) ?: it }
            override val testNameAndManufactor: String?
                get() = testCertificate.testNameAndManufactor?.let { valueSet?.getDisplayText(it) ?: it }
            override val sampleCollectedAt: Instant
                get() = testCertificate.sampleCollectedAt
            override val testResultAt: Instant?
                get() = testCertificate.testResultAt
            override val testCenter: String
                get() = testCertificate.testCenter

            override val certificateIssuer: String
                get() = header.issuer
            override val certificateCountry: String
                get() = Locale(userLocale.language, testCertificate.countryOfTest.uppercase())
                    .getDisplayCountry(userLocale)
            override val certificateId: String
                get() = testCertificate.uniqueCertificateIdentifier

            override val issuer: String
                get() = header.issuer
            override val issuedAt: Instant
                get() = header.issuedAt
            override val expiresAt: Instant
                get() = header.expiresAt

            override val qrCode: QrCodeString
                get() = data.testCertificateQrCode!!
        }
    }
}
