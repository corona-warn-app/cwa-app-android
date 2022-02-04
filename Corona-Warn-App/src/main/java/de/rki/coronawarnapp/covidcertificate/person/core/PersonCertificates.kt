package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

data class PersonCertificates(
    val certificates: List<CwaCovidCertificate>,
    val isCwaUser: Boolean = false,
    val badgeCount: Int = 0,
    val dccWalletInfo: DccWalletInfo? = null
) {
    val personIdentifier: CertificatePersonIdentifier?
        get() = certificates.firstOrNull()?.personIdentifier

    // PersonDetails
    val highestPriorityCertificate: CwaCovidCertificate? by lazy {
        certificates.firstOrNull { certificate ->
            certificate.qrCodeHash == dccWalletInfo?.mostRelevantCertificate?.certificateRef?.qrCodeHash()
        } ?: certificates.findFallbackDcc()
    }

    // PersonOverview
    val overviewCertificates: List<VerificationCertificate> by lazy {
        dccWalletInfo?.verification?.certificates.orEmpty().mapNotNull { certRef ->
            certificates.firstOrNull { it.qrCodeHash == certRef.certificateRef.qrCodeHash() }?.let {
                VerificationCertificate(
                    cwaCertificate = it,
                    buttonText = certRef.buttonText
                )
            }
        }.take(2).ifEmpty {
            when (val cert = certificates.findFallbackDcc()) {
                null -> emptyList()
                else -> listOf(VerificationCertificate(cert))
            }
        }
    }
}

data class VerificationCertificate(
    val cwaCertificate: CwaCovidCertificate,
    val buttonText: CCLText? = null
)
