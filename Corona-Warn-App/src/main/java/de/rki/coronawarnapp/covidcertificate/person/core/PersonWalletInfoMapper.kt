package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PersonWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.model.PersonSettings

internal fun findSettingsBestGuess(
    personsSettings: Map<CertificatePersonIdentifier, PersonSettings>,
    personIdentifier: CertificatePersonIdentifier,
    sortedCertificates: List<CwaCovidCertificate>
) = personsSettings[personIdentifier]
    ?: sortedCertificates.firstNotNullOfOrNull { personsSettings[it.personIdentifier] }

internal fun findWalletInfoBestGuess(
    certificates: List<CwaCovidCertificate>,
    personWallets: Map<String, PersonWalletInfo>
) = certificates.firstNotNullOfOrNull {
    personWallets[it.personIdentifier.groupingKey]?.dccWalletInfo
} ?: certificates.firstNotNullOfOrNull { cert ->
    personWallets.entries.firstOrNull { entry ->
        cert.personIdentifier.belongsToSamePerson(
            entry.key.toIdentifier()
        )
    }?.value?.dccWalletInfo
}

internal fun String.toIdentifier(): CertificatePersonIdentifier =
    split("#").run {
        CertificatePersonIdentifier(
            dateOfBirthFormatted = get(0),
            lastNameStandardized = getOrNull(1),
            firstNameStandardized = getOrNull(2),
        )
    }
