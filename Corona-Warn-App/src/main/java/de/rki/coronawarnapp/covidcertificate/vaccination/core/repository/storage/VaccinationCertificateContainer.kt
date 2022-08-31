package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccHeader
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateRepoContainer
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale

data class VaccinationCertificateContainer(
    internal val data: StoredVaccinationCertificateData,
    private val qrCodeExtractor: DccQrCodeExtractor,
) : CertificateRepoContainer {

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    internal val certificateData: DccData<VaccinationDccV1> by lazy {
        runBlocking {
            data.vaccinationQrCode.let {
                (
                    qrCodeExtractor.extract(
                        it,
                        parserMode = DccV1Parser.Mode.CERT_VAC_LENIENT
                    ) as VaccinationCertificateQRCode
                    ).data
            }
        }
    }

    override val qrCodeHash: String
        get() = data.vaccinationQrCode.toSHA256()

    override val recycledAt: Instant?
        get() = data.recycledAt

    override val containerId: VaccinationCertificateContainerId
        get() = VaccinationCertificateContainerId(qrCodeHash)

    val header: DccHeader
        get() = certificateData.header

    val certificate: VaccinationDccV1
        get() = certificateData.certificate

    val vaccination: DccV1.VaccinationData
        get() = certificate.vaccination

    val personIdentifier: CertificatePersonIdentifier
        get() = certificate.personIdentifier

    fun toVaccinationCertificate(
        valueSet: VaccinationValueSets?,
        certificateState: State,
        userLocale: Locale = Locale.getDefault(),
    ) = object : VaccinationCertificate {
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

        override val containerId: VaccinationCertificateContainerId
            get() = this@VaccinationCertificateContainer.containerId

        override val rawCertificate: VaccinationDccV1
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

        override val vaccinatedOn: LocalDate?
            get() = vaccination.vaccinatedOn
        override val vaccinatedOnFormatted: String
            get() = vaccination.vaccinatedOnFormatted

        override val targetDisease: String
            get() = valueSet?.getDisplayText(vaccination.targetId) ?: vaccination.targetId

        override val doseNumber: Int
            get() = vaccination.doseNumber
        override val totalSeriesOfDoses: Int
            get() = vaccination.totalSeriesOfDoses

        // vp
        override val vaccineTypeName: String
            get() = valueSet?.getDisplayText(vaccination.vaccineId) ?: vaccination.vaccineId

        // ma
        override val vaccineManufacturer: String
            get() = valueSet?.getDisplayText(vaccination.marketAuthorizationHolderId)
                ?: vaccination.marketAuthorizationHolderId

        // mp
        override val medicalProductName: String
            get() = valueSet?.getDisplayText(vaccination.medicalProductId) ?: vaccination.medicalProductId

        override val certificateIssuer: String
            get() = vaccination.certificateIssuer
        override val certificateCountry: String
            get() = Locale(
                userLocale.language,
                vaccination.certificateCountry.uppercase()
            ).getDisplayCountry(userLocale)

        override val qrCodeHash: String by lazy { this@VaccinationCertificateContainer.qrCodeHash }

        override val uniqueCertificateIdentifier: String
            get() = vaccination.uniqueCertificateIdentifier

        override val headerIssuer: String
            get() = header.issuer
        override val headerIssuedAt: Instant
            get() = header.issuedAt
        override val headerExpiresAt: Instant
            get() = header.expiresAt

        override val qrCodeToDisplay: CoilQrCode = displayQrCode(data.vaccinationQrCode)

        override val dccData: DccData<out DccV1.MetaData>
            get() = certificateData

        override val isNew: Boolean get() = !data.certificateSeenByUser

        override val recycledAt: Instant?
            get() = this@VaccinationCertificateContainer.recycledAt

        override fun toString(): String = "VaccinationCertificate($containerId)"
    }
}

fun VaccinationCertificateQRCode.toVaccinationContainer(
    scannedAt: Instant,
    qrCodeExtractor: DccQrCodeExtractor,
    certificateSeenByUser: Boolean,
) = VaccinationCertificateContainer(
    data = StoredVaccinationCertificateData(
        vaccinationQrCode = this.qrCode,
        scannedAt = scannedAt,
        certificateSeenByUser = certificateSeenByUser,
    ),
    qrCodeExtractor = qrCodeExtractor,
)
