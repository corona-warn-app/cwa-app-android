package de.rki.coronawarnapp.covidcertificate.test.core.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateRepoContainer
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RetrievedTestCertificate
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import java.util.Locale

data class TestCertificateContainer(
    internal val data: BaseTestCertificateData,
    internal val qrCodeExtractor: DccQrCodeExtractor,
    val isUpdatingData: Boolean = false,
) : CertificateRepoContainer {

    @delegate:Transient
    internal val testCertificateQRCode: TestCertificateQRCode? by lazy {
        runBlocking {
            data.testCertificateQrCode!!.let {
                qrCodeExtractor.extract(
                    it,
                    DccV1Parser.Mode.CERT_TEST_LENIENT
                ) as? TestCertificateQRCode
            }
        }
    }

    override val containerId: TestCertificateContainerId
        get() = TestCertificateContainerId(qrCodeHash)

    override val recycledAt: Instant?
        get() = data.recycledAt

    val registrationToken: String?
        get() = when (data) {
            is RetrievedTestCertificate -> data.registrationToken
            is GenericTestCertificateData -> null // Has none
        }

    val personIdentifier: CertificatePersonIdentifier?
        get() = testCertificateQRCode?.data?.certificate?.personIdentifier

    val certificateSeenByUser: Boolean
        get() = data.certificateSeenByUser

    val registeredAt: Instant
        get() = data.registeredAt

    val isCertificateRetrievalPending: Boolean
        get() = data.certificateReceivedAt == null

    /**
     * Retrieved Test certificate container at beginning does not have QR Code. Until QR Code exist UUID is provided
     * and in this is case it is not yet considered as certificate
     */
    override val qrCodeHash: String
        get() = data.testCertificateQrCode?.toSHA256() ?: data.identifier

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    fun toTestCertificate(
        valueSet: TestCertificateValueSets? = null,
        certificateState: State,
        userLocale: Locale = Locale.getDefault(),
    ): TestCertificate? {
        if (isCertificateRetrievalPending) return null

        val header = testCertificateQRCode?.data?.header ?: return null
        val certificate = testCertificateQRCode?.data?.certificate ?: return null
        val testCertificate = certificate.test

        return object : TestCertificate {
            override val state: State get() = certificateState

            override val containerId: TestCertificateContainerId
                get() = this@TestCertificateContainer.containerId

            override val rawCertificate: TestDccV1
                get() = certificate

            override val personIdentifier: CertificatePersonIdentifier
                get() = certificate.personIdentifier

            override val firstName: String?
                get() = certificate.nameData.firstName

            override val lastName: String?
                get() = certificate.nameData.lastName

            override val fullName: String
                get() = certificate.nameData.fullName

            override val fullNameFormatted: String
                get() = certificate.nameData.fullNameFormatted

            override val fullNameStandardizedFormatted: String
                get() = certificate.nameData.fullNameStandardizedFormatted

            override val dateOfBirthFormatted: String
                get() = certificate.dateOfBirthFormatted

            override val targetDisease: String
                get() = valueSet?.getDisplayText(testCertificate.targetId) ?: testCertificate.targetId

            override val testType: String
                get() = valueSet?.getDisplayText(testCertificate.testType) ?: testCertificate.testType
            override val testResult: String
                get() = valueSet?.getDisplayText(testCertificate.testResult) ?: testCertificate.testResult
            override val testName: String?
                get() = testCertificate.testName?.let { valueSet?.getDisplayText(it) ?: it }
            override val testNameAndManufacturer: String?
                get() = testCertificate.testNameAndManufacturer?.let { valueSet?.getDisplayText(it) ?: it }
            override val sampleCollectedAt: Instant?
                get() = testCertificate.sampleCollectedAt
            override val sampleCollectedAtFormatted: String
                get() = testCertificate.sampleCollectedAtFormatted
            override val testCenter: String?
                get() = testCertificate.testCenter

            override val certificateIssuer: String
                get() = testCertificate.certificateIssuer
            override val certificateCountry: String
                get() = Locale(userLocale.language, testCertificate.certificateCountry.uppercase())
                    .getDisplayCountry(userLocale)
            override val qrCodeHash: String by lazy { this@TestCertificateContainer.qrCodeHash }

            override val uniqueCertificateIdentifier: String
                get() = testCertificate.uniqueCertificateIdentifier

            override val headerIssuer: String
                get() = header.issuer
            override val headerIssuedAt: Instant
                get() = header.issuedAt
            override val headerExpiresAt: Instant
                get() = header.expiresAt

            override val qrCodeToDisplay: CoilQrCode = displayQrCode(data.testCertificateQrCode!!)

            override val isUpdatingData: Boolean
                get() = this@TestCertificateContainer.isUpdatingData

            override val registeredAt: Instant
                get() = data.registeredAt

            override val isCertificateRetrievalPending: Boolean
                get() = this@TestCertificateContainer.isCertificateRetrievalPending

            override val dccData: DccData<out DccV1.MetaData>
                get() = testCertificateQRCode!!.data

            override val notifiedInvalidAt: Instant?
                get() = data.notifiedInvalidAt

            override val notifiedBlockedAt: Instant?
                get() = data.notifiedBlockedAt

            override val notifiedRevokedAt: Instant?
                get() = data.notifiedRevokedAt

            override val lastSeenStateChange: State?
                get() = data.lastSeenStateChange

            override val lastSeenStateChangeAt: Instant?
                get() = data.lastSeenStateChangeAt

            override val isNew: Boolean
                get() = !certificateSeenByUser && !isCertificateRetrievalPending

            override val recycledAt: Instant?
                get() = data.recycledAt

            override fun toString(): String = "TestCertificate($containerId)"
        }
    }
}
