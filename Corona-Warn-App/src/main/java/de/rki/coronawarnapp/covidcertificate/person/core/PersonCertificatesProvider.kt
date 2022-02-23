package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

// Aggregate the certificates and sort them
@Reusable
class PersonCertificatesProvider @Inject constructor(
    private val personCertificatesSettings: PersonCertificatesSettings,
    certificatesProvider: CertificateProvider,
    dccWalletInfoRepository: DccWalletInfoRepository,
    @AppScope private val appScope: CoroutineScope,
) {
    val personCertificates: Flow<Set<PersonCertificates>> = combine(
        certificatesProvider.certificateContainer,
        personCertificatesSettings.currentCwaUser,
        dccWalletInfoRepository.personWallets,
        personCertificatesSettings.personsSettings
    ) { certificateContainer, cwaUser, personWallets, personSettings ->

        val allCerts = certificateContainer.allCwaCertificates
        val vaccPersons = certificateContainer.vaccinationInfos

        val personWalletsGroup = personWallets.associateBy { it.personGroupKey }

        val personCertificatesMap = allCerts.groupBy {
            it.personIdentifier
        }

        if (!personCertificatesMap.containsKey(cwaUser)) {
            Timber.tag(TAG).v("Resetting cwa user")
            personCertificatesSettings.removeCurrentCwaUser()
        }

        personCertificatesMap.entries.map { (personIdentifier, certs) ->
            Timber.tag(TAG).v("PersonCertificates for %s with %d certs.", personIdentifier, certs.size)

            val dccWalletInfo = personWalletsGroup[personIdentifier.groupingKey]?.dccWalletInfo

            val hasBooster = vaccPersons.boosterBadgeCount(personIdentifier, dccWalletInfo?.boosterNotification)
            val hasDccReissuance = personSettings[personIdentifier]?.showDccReissuanceBadge ?: false
            val badgeCount = certs.count { it.hasNotificationBadge } + hasBooster.toInt() + hasDccReissuance.toInt()
            Timber.tag(TAG).d("Badge count of %s =%s", personIdentifier.codeSHA256, badgeCount)

            PersonCertificates(
                certificates = certs.toCertificateSortOrder(),
                isCwaUser = personIdentifier == cwaUser,
                badgeCount = badgeCount,
                dccWalletInfo = dccWalletInfo,
                hasBooster = hasBooster,
                hasDccReissuance = hasDccReissuance
            )
        }.toSet()
    }.shareLatest(scope = appScope)

    /**
     * Set the current cwa user with regards to listed persons in the certificates tab.
     * After calling this [personCertificates] will emit new values.
     * Setting it to null deletes it.
     */
    fun setCurrentCwaUser(personIdentifier: CertificatePersonIdentifier?) {
        Timber.d("setCurrentCwaUser(personIdentifier=%s)", personIdentifier)
        personCertificatesSettings.setCurrentCwaUser(personIdentifier)
    }

    val personsBadgeCount: Flow<Int> = personCertificates.map { persons -> persons.sumOf { it.badgeCount } }

    private fun Set<VaccinatedPerson>.boosterBadgeCount(
        personIdentifier: CertificatePersonIdentifier,
        boosterNotification: BoosterNotification?
    ): Boolean {
        if (boosterNotification == null) return false
        val vaccinatedPerson = singleOrNull { it.identifier == personIdentifier }
        return hasBoosterRuleNotYetSeen(vaccinatedPerson, boosterNotification)
    }

    private fun hasBoosterRuleNotYetSeen(
        vaccinatedPerson: VaccinatedPerson?,
        boosterNotification: BoosterNotification
    ) = vaccinatedPerson?.data?.lastSeenBoosterRuleIdentifier != boosterNotification.identifier

    private fun Boolean?.toInt(): Int = if (this == true) 1 else 0

    companion object {
        private val TAG = tag<PersonCertificatesProvider>()
    }
}
