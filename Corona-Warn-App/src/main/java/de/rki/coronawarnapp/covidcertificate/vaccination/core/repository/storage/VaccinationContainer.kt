package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccHeader
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
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

@Keep
data class VaccinationContainer internal constructor(
    @SerializedName("vaccinationQrCode") val vaccinationQrCode: QrCodeString,
    @SerializedName("scannedAt") val scannedAt: Instant,
    @SerializedName("notifiedExpiresSoonAt") val notifiedExpiresSoonAt: Instant? = null,
    @SerializedName("notifiedExpiredAt") val notifiedExpiredAt: Instant? = null,
    @SerializedName("notifiedInvalidAt") val notifiedInvalidAt: Instant? = null,
    @SerializedName("notifiedBlockedAt") val notifiedBlockedAt: Instant? = null,
    @SerializedName("lastSeenStateChange") val lastSeenStateChange: State? = null,
    @SerializedName("lastSeenStateChangeAt") val lastSeenStateChangeAt: Instant? = null,
    @SerializedName("certificateSeenByUser") val certificateSeenByUser: Boolean = true,
    @SerializedName("recycledAt") override val recycledAt: Instant? = null,
) : CertificateRepoContainer {

    // Either set by [ContainerPostProcessor] or via [toVaccinationContainer]
    @Transient lateinit var qrCodeExtractor: DccQrCodeExtractor
    @Transient internal var preParsedData: DccData<VaccinationDccV1>? = null

    // Otherwise GSON unsafes reflection to create this class, and sets the LAZY to null
    @Suppress("unused")
    constructor() : this("", Instant.EPOCH)

    @delegate:Transient
    internal val certificateData: DccData<VaccinationDccV1> by lazy {
        runBlocking {
            preParsedData ?: (
                qrCodeExtractor.extract(
                    vaccinationQrCode,
                    parserMode = DccV1Parser.Mode.CERT_VAC_LENIENT
                ) as VaccinationCertificateQRCode
                )
                .data
        }
    }

    override val qrCodeHash: String
        get() = vaccinationQrCode.toSHA256()

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
        override fun getState(): State = certificateState

        override val notifiedExpiresSoonAt: Instant?
            get() = this@VaccinationContainer.notifiedExpiresSoonAt

        override val notifiedExpiredAt: Instant?
            get() = this@VaccinationContainer.notifiedExpiredAt

        override val notifiedInvalidAt: Instant?
            get() = this@VaccinationContainer.notifiedInvalidAt

        override val notifiedBlockedAt: Instant?
            get() = this@VaccinationContainer.notifiedBlockedAt

        override val lastSeenStateChange: State?
            get() = this@VaccinationContainer.lastSeenStateChange

        override val lastSeenStateChangeAt: Instant?
            get() = this@VaccinationContainer.lastSeenStateChangeAt

        override val containerId: VaccinationCertificateContainerId
            get() = this@VaccinationContainer.containerId

        override val rawCertificate: VaccinationDccV1
            get() = certificate

        override val personIdentifier: CertificatePersonIdentifier
            get() = certificate.personIdentifier

        override val firstName: String?
            get() = certificate.nameData.firstName

        override val lastName: String
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

        override val qrCodeHash: String by lazy { this@VaccinationContainer.qrCodeHash }

        override val uniqueCertificateIdentifier: String
            get() = vaccination.uniqueCertificateIdentifier

        override val headerIssuer: String
            get() = header.issuer
        override val headerIssuedAt: Instant
            get() = header.issuedAt
        override val headerExpiresAt: Instant
            get() = header.expiresAt

        override val qrCodeToDisplay: CoilQrCode = displayQrCode(vaccinationQrCode)

        override val dccData: DccData<out DccV1.MetaData>
            get() = certificateData

        override val hasNotificationBadge: Boolean
            get() {
                val state = getState()
                return (state !is State.Valid && state != lastSeenStateChange) || isNew
            }

        override val isNew: Boolean get() = !certificateSeenByUser

        override val recycledAt: Instant?
            get() = this@VaccinationContainer.recycledAt

        override fun toString(): String = "VaccinationCertificate($containerId)"
    }
}

fun VaccinationCertificateQRCode.toVaccinationContainer(
    scannedAt: Instant,
    qrCodeExtractor: DccQrCodeExtractor,
    certificateSeenByUser: Boolean,
) = VaccinationContainer(
    vaccinationQrCode = this.qrCode,
    scannedAt = scannedAt,
    certificateSeenByUser = certificateSeenByUser,
).apply {
    this.qrCodeExtractor = qrCodeExtractor
    preParsedData = data
}
