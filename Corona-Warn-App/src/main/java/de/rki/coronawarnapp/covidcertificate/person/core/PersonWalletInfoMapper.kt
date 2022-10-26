package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PersonWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.model.PersonSettings

internal fun List<CwaCovidCertificate>.findSettings(
    personsSettings: Map<CertificatePersonIdentifier, PersonSettings>,
    identifier: CertificatePersonIdentifier,
) = personsSettings[identifier] ?: firstNotNullOfOrNull { cert ->
    personsSettings.entries.firstOrNull { entry -> cert.personIdentifier.belongsToSamePerson(entry.key) }?.value
}

internal fun List<CwaCovidCertificate>.findWalletInfo(
    wallets: Map<String, PersonWalletInfo>
) = firstNotNullOfOrNull { wallets[it.personIdentifier.groupingKey]?.dccWalletInfo } ?: firstNotNullOfOrNull { cert ->
    wallets.entries.firstOrNull { entry ->
        cert.personIdentifier.belongsToSamePerson(entry.key.toIdentifier())
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
