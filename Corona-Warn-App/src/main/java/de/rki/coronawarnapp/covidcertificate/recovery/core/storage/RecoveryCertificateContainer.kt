package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateRepoContainer
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale

data class RecoveryCertificateContainer(
    internal val data: StoredRecoveryCertificateData,
    private val qrCodeExtractor: DccQrCodeExtractor,
    val isUpdatingData: Boolean = false,
) : StoredRecoveryCertificate by data, CertificateRepoContainer {

    @delegate:Transient
    internal val certificateData: DccData<RecoveryDccV1> by lazy {
        data.recoveryCertificateQrCode.let {
            (
                qrCodeExtractor.extract(
                    it,
                    mode = Mode.CERT_REC_LENIENT
                ) as RecoveryCertificateQRCode
                ).data
        }
    }

    override val containerId: RecoveryCertificateContainerId
        get() = RecoveryCertificateContainerId(certificateData.certificate.recovery.uniqueCertificateIdentifier)

    val certificateId: String
        get() = certificateData.certificate.recovery.uniqueCertificateIdentifier

    val personIdentifier: CertificatePersonIdentifier
        get() = certificateData.certificate.personIdentifier

    fun toRecoveryCertificate(
        valueSet: VaccinationValueSets? = null,
        certificateState: CwaCovidCertificate.State,
        userLocale: Locale = Locale.getDefault(),
    ): RecoveryCertificate {
        val header = certificateData.header
        val certificate = certificateData.certificate
        val recoveryCertificate = certificate.recovery

        return object : RecoveryCertificate {
            override fun getState(): CwaCovidCertificate.State = certificateState

            override val containerId: RecoveryCertificateContainerId
                get() = this@RecoveryCertificateContainer.containerId

            override val rawCertificate: RecoveryDccV1
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

            override val targetDisease: String
                get() = valueSet?.getDisplayText(recoveryCertificate.targetId) ?: recoveryCertificate.targetId

            override val testedPositiveOnFormatted: String
                get() = recoveryCertificate.testedPositiveOnFormatted

            override val validUntilFormatted: String
                get() = recoveryCertificate.validUntilFormatted

            override val validUntil: LocalDate
                get() = recoveryCertificate.validUntil

            override val validFromFormatted: String
                get() = recoveryCertificate.validFromFormatted

            override val validFrom: LocalDate
                get() = recoveryCertificate.validFrom

            override val certificateIssuer: String
                get() = recoveryCertificate.certificateIssuer

            override val certificateCountry: String
                get() = Locale(userLocale.language, recoveryCertificate.certificateCountry.uppercase())
                    .getDisplayCountry(userLocale)

            override val certificateId: String
                get() = recoveryCertificate.uniqueCertificateIdentifier

            override val headerIssuer: String
                get() = header.issuer
            override val headerIssuedAt: Instant
                get() = header.issuedAt
            override val headerExpiresAt: Instant
                get() = header.expiresAt

            override val qrCode: QrCodeString
                get() = data.recoveryCertificateQrCode

            override val dccData: DccData<out DccV1.MetaData>
                get() = certificateData
        }
    }
}
