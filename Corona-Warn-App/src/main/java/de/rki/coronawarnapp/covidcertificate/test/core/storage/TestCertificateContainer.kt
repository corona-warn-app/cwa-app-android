package de.rki.coronawarnapp.covidcertificate.test.core.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale

data class TestCertificateContainer(
    internal val data: StoredTestCertificateData,
    private val qrCodeExtractor: DccQrCodeExtractor,
    val isUpdatingData: Boolean = false,
) : StoredTestCertificateData by data {

    @delegate:Transient
    private val testCertificateQRCode: TestCertificateQRCode by lazy {
        data.testCertificateQrCode!!.let {
            qrCodeExtractor.extract(
                it,
                DccV1Parser.Mode.CERT_TEST_STRICT
            ) as TestCertificateQRCode
        }
    }

    val isPublicKeyRegistered: Boolean
        get() = data.publicKeyRegisteredAt != null

    val isCertificateRetrievalPending: Boolean
        get() = data.certificateReceivedAt == null

    val certificateId: String?
        get() {
            if (isCertificateRetrievalPending) return null
            return testCertificateQRCode.uniqueCertificateIdentifier
        }

    fun toTestCertificate(
        valueSet: TestCertificateValueSets?,
        userLocale: Locale = Locale.getDefault(),
    ): TestCertificate? {
        if (isCertificateRetrievalPending) return null

        val header = testCertificateQRCode.data.header
        val certificate = testCertificateQRCode.data.certificate
        val testCertificate = certificate.tests!!.single() // todo

        return object : TestCertificate {
            override val personIdentifier: CertificatePersonIdentifier
                get() = certificate.personIdentifier

            override val firstName: String?
                get() = certificate.nameData.firstName

            override val lastName: String
                get() = certificate.nameData.lastName

            override val fullName: String
                get() = certificate.nameData.fullName

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
            override val testNameAndManufacturer: String?
                get() = testCertificate.testNameAndManufactor?.let { valueSet?.getDisplayText(it) ?: it }
            override val sampleCollectedAt: Instant
                get() = testCertificate.sampleCollectedAt
            override val testResultAt: Instant?
                get() = testCertificate.testResultAt
            override val testCenter: String
                get() = testCertificate.testCenter

            override val isUpdatingData: Boolean
                get() = this@TestCertificateContainer.isUpdatingData

            override val registeredAt: Instant
                get() = this@TestCertificateContainer.registeredAt

            override val isCertificateRetrievalPending: Boolean
                get() = this@TestCertificateContainer.isCertificateRetrievalPending

            override val certificateIssuer: String
                get() = header.issuer
            override val certificateCountry: String
                get() = Locale(userLocale.language, testCertificate.certificateCountry.uppercase())
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
