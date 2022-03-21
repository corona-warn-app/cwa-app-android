package de.rki.coronawarnapp.covidcertificate.person.core

import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MigrationCheck @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val cwaSettings: CWASettings,
    private val dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger
) {

    fun shouldShowMigrationInfo(persons: Set<PersonCertificates>): Boolean {
        Timber.tag(TAG).d("shouldShowMigrationInfo(persons=%s)", persons.size)
        if (cwaSettings.wasCertificateGroupingMigrationAcknowledged) return false

        Timber.tag(TAG).d("shouldShowMigrationInfo wasn't called before")
        cwaSettings.wasCertificateGroupingMigrationAcknowledged = true

        val numberOfPersons = persons.size

        val numberOfPersonsWithLegacyGrouping = persons
            .flatMap { it.certificates }
            .map { it.personIdentifier.getLegacyGroupingKey() }
            .toSet()
            .size

        val merged = numberOfPersons != numberOfPersonsWithLegacyGrouping
        if (merged) {
            Timber.tag(TAG).d("Some persons have been merged -> recalculating DccWalletInfo")
            appScope.launch {
                runCatching {
                    dccWalletInfoUpdateTrigger.triggerNow()
                }.onFailure { Timber.tag(TAG).d(it, "recalculating failed") }
            }
        } else {
            Timber.tag(TAG).d("No merge of persons")
        }
        return merged
    }

    private fun CertificatePersonIdentifier.getLegacyGroupingKey(): String {
        val lastName = lastNameStandardized.trim()
        val firstName = firstNameStandardized?.trim()
        return "$dateOfBirthFormatted#$lastName#$firstName".condense()
    }

    private fun String.condense() = this.replace("\\s+".toRegex(), " ").replace("<+".toRegex(), "<")

    companion object {
        private val TAG = tag<MigrationCheck>()
    }
}
