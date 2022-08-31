package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.main.CWASettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MigrationCheck @Inject constructor(private val cwaSettings: CWASettings) {

    fun shouldShowMigrationInfo(persons: Set<PersonCertificates>): Boolean {
        if (cwaSettings.wasCertificateGroupingMigrationAcknowledged) return false
        cwaSettings.wasCertificateGroupingMigrationAcknowledged = true

        val numberOfPersons = persons.size

        val numberOfPersonsWithLegacyGrouping = persons
            .flatMap { it.certificates }
            .map { it.personIdentifier.getLegacyGroupingKey() }
            .toSet()
            .size

        return numberOfPersons != numberOfPersonsWithLegacyGrouping
    }

    private fun CertificatePersonIdentifier.getLegacyGroupingKey(): String {
        val lastName = lastNameStandardized?.trim()
        val firstName = firstNameStandardized?.trim()
        return "$dateOfBirthFormatted#$lastName#$firstName".condense()
    }

    private fun String.condense() = this.replace("\\s+".toRegex(), " ").replace("<+".toRegex(), "<")
}
