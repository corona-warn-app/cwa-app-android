package de.rki.coronawarnapp.covidcertificate.test.core.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateRepoContainer
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RetrievedTestCertificate
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import org.joda.time.Instant
import java.util.Locale

data class TestCertificateContainer(
    internal val data: BaseTestCertificateData,
    internal val qrCodeExtractor: DccQrCodeExtractor,
    val isUpdatingData: Boolean = false,
) : CertificateRepoContainer {

    @delegate:Transient
    private val testCertificateQRCode: TestCertificateQRCode by lazy {
        data.testCertificateQrCode!!.let {
            qrCodeExtractor.extract(
                it,
                DccV1Parser.Mode.CERT_TEST_STRICT
            ) as TestCertificateQRCode
        }
    }

    override val containerId: TestCertificateContainerId
        get() = TestCertificateContainerId(data.identifier)

    val registrationToken: String?
        get() = when (data) {
            is RetrievedTestCertificate -> data.registrationToken
            is GenericTestCertificateData -> null // Has none
        }

    val personIdentifier: CertificatePersonIdentifier
        get() = testCertificateQRCode.data.certificate.personIdentifier

    val certificateSeenByUser: Boolean
        get() = when (data) {
            is RetrievedTestCertificate -> data.certificateSeenByUser
            is GenericTestCertificateData -> true // Immediately available
        }

    val registeredAt: Instant
        get() = data.registeredAt

    val isCertificateRetrievalPending: Boolean
        get() = data.certificateReceivedAt == null

    val certificateId: String?
        get() {
            if (isCertificateRetrievalPending) return null
            return testCertificateQRCode.uniqueCertificateIdentifier
        }

    fun toTestCertificate(
        valueSet: TestCertificateValueSets? = null,
        userLocale: Locale = Locale.getDefault(),
    ): TestCertificate? {
        if (isCertificateRetrievalPending) return null

        val header = testCertificateQRCode.data.header
        val certificate = testCertificateQRCode.data.certificate
        val testCertificate = certificate.test

        return object : TestCertificate {
            override val containerId: TestCertificateContainerId
                get() = this@TestCertificateContainer.containerId

            override val rawCertificate: TestDccV1
                get() = certificate

            override val personIdentifier: CertificatePersonIdentifier
                get() = certificate.personIdentifier

            override val firstName: String?
                get() = certificate.nameData.firstName

            override val lastName: String
                get() = certificate.nameData.lastName

            override val fullName: String
                get() = certificate.nameData.fullName

            override val dateOfBirthFormatted: String
                get() = certificate.dateOfBirthFormatted

            override val targetName: String
                get() = valueSet?.getDisplayText(testCertificate.targetId) ?: testCertificate.targetId
            override val testType: String
                get() = valueSet?.getDisplayText(testCertificate.testType) ?: testCertificate.testType
            override val testResult: String
                get() = valueSet?.getDisplayText(testCertificate.testResult) ?: testCertificate.testResult
            override val testName: String?
                get() = testCertificate.testName?.let { valueSet?.getDisplayText(it) ?: it }
            override val testNameAndManufacturer: String?
                get() = testCertificate.testNameAndManufacturer?.let { valueSet?.getDisplayText(it) ?: it }
            override val sampleCollectedAt: Instant
                get() = testCertificate.sampleCollectedAt
            override val sampleCollectedAtFormatted: String
                get() = testCertificate.sampleCollectedAtFormatted
            override val testCenter: String?
                get() = testCertificate.testCenter

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

            override val isUpdatingData: Boolean
                get() = this@TestCertificateContainer.isUpdatingData

            override val registeredAt: Instant
                get() = data.registeredAt

            override val isCertificateRetrievalPending: Boolean
                get() = this@TestCertificateContainer.isCertificateRetrievalPending
        }
    }
}
