package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateRepoContainer
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale

data class RecoveryCertificateContainer(
    internal val data: StoredRecoveryCertificateData,
    private val qrCodeExtractor: DccQrCodeExtractor,
    val isUpdatingData: Boolean = false,
) : CertificateRepoContainer {

    @delegate:Transient
    internal val certificateData: DccData<RecoveryDccV1> by lazy {
        runBlocking {
            data.recoveryCertificateQrCode.let {
                (
                    qrCodeExtractor.extract(
                        it,
                        parserMode = Mode.CERT_REC_LENIENT
                    ) as RecoveryCertificateQRCode
                    ).data
            }
        }
    }

    override val qrCodeHash: String
        get() = data.recoveryCertificateQrCode.toSHA256()

    override val containerId: RecoveryCertificateContainerId
        get() = RecoveryCertificateContainerId(qrCodeHash)

    val personIdentifier: CertificatePersonIdentifier
        get() = certificateData.certificate.personIdentifier

    override val recycledAt: Instant? = data.recycledAt

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    fun toRecoveryCertificate(
        valueSet: VaccinationValueSets? = null,
        certificateState: State,
        userLocale: Locale = Locale.getDefault(),
    ): RecoveryCertificate {
        val header = certificateData.header
        val certificate = certificateData.certificate
        val recoveryCertificate = certificate.recovery

        return object : RecoveryCertificate {
            override val state: State get() = certificateState

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

            override val containerId: RecoveryCertificateContainerId
                get() = this@RecoveryCertificateContainer.containerId

            override val rawCertificate: RecoveryDccV1
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
                get() = valueSet?.getDisplayText(recoveryCertificate.targetId) ?: recoveryCertificate.targetId

            override val testedPositiveOnFormatted: String
                get() = recoveryCertificate.testedPositiveOnFormatted

            override val testedPositiveOn: LocalDate?
                get() = recoveryCertificate.testedPositiveOn

            override val validUntilFormatted: String
                get() = recoveryCertificate.validUntilFormatted

            override val validUntil: LocalDate?
                get() = recoveryCertificate.validUntil

            override val validFromFormatted: String
                get() = recoveryCertificate.validFromFormatted

            override val validFrom: LocalDate?
                get() = recoveryCertificate.validFrom

            override val certificateIssuer: String
                get() = recoveryCertificate.certificateIssuer

            override val certificateCountry: String
                get() = Locale(userLocale.language, recoveryCertificate.certificateCountry.uppercase())
                    .getDisplayCountry(userLocale)

            override val qrCodeHash: String by lazy { this@RecoveryCertificateContainer.qrCodeHash }

            override val uniqueCertificateIdentifier: String
                get() = recoveryCertificate.uniqueCertificateIdentifier

            override val headerIssuer: String
                get() = header.issuer
            override val headerIssuedAt: Instant
                get() = header.issuedAt
            override val headerExpiresAt: Instant
                get() = header.expiresAt

            override val qrCodeToDisplay: CoilQrCode = displayQrCode(data.recoveryCertificateQrCode)

            override val dccData: DccData<out DccV1.MetaData>
                get() = certificateData

            override val isNew: Boolean
                get() = !data.certificateSeenByUser

            override val recycledAt: Instant?
                get() = data.recycledAt

            override fun toString(): String = "RecoveryCertificate($containerId)"
        }
    }
}
