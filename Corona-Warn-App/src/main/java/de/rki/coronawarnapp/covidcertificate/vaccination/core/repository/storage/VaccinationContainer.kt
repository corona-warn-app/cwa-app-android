package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateRepoContainer
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
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
    internal val certificateData: DccData<VaccinationDccV1>? by lazy {
        preParsedData ?: (
            try {
                qrCodeExtractor.extract(
                    vaccinationQrCode,
                    mode = DccV1Parser.Mode.CERT_VAC_LENIENT
                ) as? VaccinationCertificateQRCode
            } catch (e: InvalidVaccinationCertificateException) {
                null
            }
            )?.data
    }

    override val containerId: VaccinationCertificateContainerId?
        get() = certificateData?.containerId

    fun toVaccinationCertificate(
        valueSet: VaccinationValueSets?,
        certificateState: State,
        userLocale: Locale = Locale.getDefault(),
    ): VaccinationCertificate? = when (val certData = certificateData) {
        null -> null
        else -> object : VaccinationCertificate {
            override fun getState(): State = certificateState

            override val notifiedExpiresSoonAt: Instant?
                get() = this@VaccinationContainer.notifiedExpiresSoonAt

            override val notifiedExpiredAt: Instant?
                get() = this@VaccinationContainer.notifiedExpiredAt

            override val notifiedInvalidAt: Instant?
                get() = this@VaccinationContainer.notifiedInvalidAt

            override val lastSeenStateChange: State?
                get() = this@VaccinationContainer.lastSeenStateChange

            override val lastSeenStateChangeAt: Instant?
                get() = this@VaccinationContainer.lastSeenStateChangeAt

            override val containerId: VaccinationCertificateContainerId
                get() = VaccinationCertificateContainerId(certData.certificateId)

            override val rawCertificate: VaccinationDccV1
                get() = certData.certificate

            override val personIdentifier: CertificatePersonIdentifier
                get() = certData.personIdentifier

            override val firstName: String?
                get() = certData.certificate.nameData.firstName

            override val lastName: String
                get() = certData.certificate.nameData.lastName

            override val fullName: String
                get() = certData.certificate.nameData.fullName

            override val fullNameFormatted: String
                get() = certData.certificate.nameData.fullNameFormatted

            override val fullNameStandardizedFormatted: String
                get() = certData.certificate.nameData.fullNameStandardizedFormatted

            override val dateOfBirthFormatted: String
                get() = certData.certificate.dateOfBirthFormatted

            override val vaccinatedOn: LocalDate
                get() = certData.vaccination.vaccinatedOn
            override val vaccinatedOnFormatted: String
                get() = certData.vaccination.vaccinatedOnFormatted

            override val targetDisease: String
                get() = valueSet?.getDisplayText(certData.vaccination.targetId)
                    ?: certData.vaccination.targetId

            override val doseNumber: Int
                get() = certData.vaccination.doseNumber
            override val totalSeriesOfDoses: Int
                get() = certData.vaccination.totalSeriesOfDoses

            // vp
            override val vaccineTypeName: String
                get() = valueSet?.getDisplayText(certData.vaccination.vaccineId)
                    ?: certData.vaccination.vaccineId

            // ma
            override val vaccineManufacturer: String
                get() = valueSet?.getDisplayText(certData.vaccination.marketAuthorizationHolderId)
                    ?: certData.vaccination.marketAuthorizationHolderId

            // mp
            override val medicalProductName: String
                get() = valueSet?.getDisplayText(certData.vaccination.medicalProductId)
                    ?: certData.vaccination.medicalProductId

            override val certificateIssuer: String
                get() = certData.vaccination.certificateIssuer
            override val certificateCountry: String
                get() = Locale(
                    userLocale.language,
                    certData.vaccination.certificateCountry.uppercase()
                ).getDisplayCountry(userLocale)

            override val certificateId: String
                get() = certData.certificateId

            override val headerIssuer: String
                get() = certData.header.issuer
            override val headerIssuedAt: Instant
                get() = certData.header.issuedAt
            override val headerExpiresAt: Instant
                get() = certData.header.expiresAt

            override val qrCodeToDisplay: CoilQrCode = displayQrCode(vaccinationQrCode)

            override val dccData: DccData<out DccV1.MetaData>
                get() = certData

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
}

val DccData<out VaccinationDccV1>.vaccination
    get() = this.certificate.vaccination

val DccData<out VaccinationDccV1>.certificateId
    get() = this.vaccination.uniqueCertificateIdentifier

val DccData<out VaccinationDccV1>.personIdentifier
    get() = this.certificate.personIdentifier

val DccData<out VaccinationDccV1>.containerId
    get() = VaccinationCertificateContainerId(certificateId)

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
