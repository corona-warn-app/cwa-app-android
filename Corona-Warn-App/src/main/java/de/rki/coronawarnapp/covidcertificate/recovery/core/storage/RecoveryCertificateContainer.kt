package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.RecoveryCertificateQRCodeExtractor
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale

data class RecoveryCertificateContainer(
    internal val data: StoredRecoveryCertificateData,
    private val qrCodeExtractor: RecoveryCertificateQRCodeExtractor,
    val isUpdatingData: Boolean = false,
) : StoredRecoveryCertificate by data {

    @delegate:Transient
    private val certificateData: DccData<TestDccV1> by lazy {
        data.recoveryCertificateQrCode!!.let { qrCodeExtractor.extract(it).data }
    }

    val certificateId: String
        get() = certificateData.certificate.payload.uniqueCertificateIdentifier

    fun toRecoveryCertificate(
        valueSet: TestCertificateValueSets?,
        userLocale: Locale = Locale.getDefault(),
    ): RecoveryCertificate {
        val header = certificateData.header
        val certificate = certificateData.certificate
        val testCertificate = certificate.payload

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

            // TODO

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
                get() = data.recoveryCertificateQrCode!!
        }
    }
}
