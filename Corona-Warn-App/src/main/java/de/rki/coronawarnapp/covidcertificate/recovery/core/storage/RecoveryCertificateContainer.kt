package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale

data class RecoveryCertificateContainer(
    internal val data: StoredRecoveryCertificateData,
    private val qrCodeExtractor: DccQrCodeExtractor,
    val isUpdatingData: Boolean = false,
) : StoredRecoveryCertificate by data {

    @delegate:Transient
    private val certificateData: DccData by lazy {
        data.recoveryCertificateQrCode!!.let {
            qrCodeExtractor.extract(
                it,
                mode = Mode.CERT_REC_STRICT
            ).data
        }
    }

    val recovery: DccV1.RecoveryCertificateData
        get() = certificateData.certificate.recoveries!!.single()

    val certificateId: String
        get() = recovery.uniqueCertificateIdentifier

    fun toRecoveryCertificate(
        valueSet: TestCertificateValueSets?,
        userLocale: Locale = Locale.getDefault(),
    ): RecoveryCertificate {
        val header = certificateData.header
        val certificate = certificateData.certificate
        val recoveryCertificate = certificate.recoveries!!.single()

        return object : RecoveryCertificate {
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

            override val testedPositiveOn: LocalDate
                get() = recoveryCertificate.testedPositiveOn
            override val validFrom: LocalDate
                get() = recoveryCertificate.validFrom
            override val validUntil: LocalDate
                get() = recoveryCertificate.validUntil

            override val certificateIssuer: String
                get() = header.issuer
            override val certificateCountry: String
                get() = Locale(userLocale.language, recoveryCertificate.certificateCountry.uppercase())
                    .getDisplayCountry(userLocale)
            override val certificateId: String
                get() = recoveryCertificate.uniqueCertificateIdentifier

            override val issuer: String
                get() = header.issuer
            override val issuedAt: Instant
                get() = header.issuedAt
            override val expiresAt: Instant
                get() = header.expiresAt

            override val qrCode: QrCodeString
                get() = data.recoveryCertificateQrCode!!
        }
    }
}
