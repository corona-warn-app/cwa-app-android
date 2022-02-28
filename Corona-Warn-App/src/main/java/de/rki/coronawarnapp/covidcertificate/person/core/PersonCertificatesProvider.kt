package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.dcc.findCertificatesForPerson
import de.rki.coronawarnapp.util.dcc.groupByPerson
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

        val personWalletsGroup = personWallets.associateBy { it.personGroupKey }
        val groupedCerts = certificateContainer.allCwaCertificates.groupByPerson()

        if (cwaUser != null && groupedCerts.findCertificatesForPerson(cwaUser).isEmpty()) {
            Timber.tag(TAG).v("Resetting cwa user")
            personCertificatesSettings.removeCurrentCwaUser()
        }

        groupedCerts.map { certs ->
            val firstPersonIdentifier = certs.first().personIdentifier
            Timber.tag(TAG).v("PersonCertificates for %s with %d certs.", firstPersonIdentifier, certs.size)

            val dccWalletInfo =
                personWalletsGroup[firstPersonIdentifier.groupingKey]?.dccWalletInfo

            // TODO: booster badge & vaccination repository should be updated in (EXPOSUREAPP-11724)
            val hasBooster = vaccPersons.hasBoosterBadge(firstPersonIdentifier, dccWalletInfo?.boosterNotification)
            val hasDccReissuance = personSettings[firstPersonIdentifier]?.showDccReissuanceBadge ?: false
            val badgeCount = certs.count { it.hasNotificationBadge } + hasBooster.toInt() + hasDccReissuance.toInt()
            Timber.tag(TAG).d("Badge count of %s =%s", firstPersonIdentifier.codeSHA256, badgeCount)

            PersonCertificates(
                certificates = certs.toCertificateSortOrder(),
                isCwaUser = certs.any { it.personIdentifier.belongsToSamePerson(cwaUser) },
                badgeCount = badgeCount,
                dccWalletInfo = dccWalletInfo,
                hasBoosterBadge = hasBooster,
                hasDccReissuanceBadge = hasDccReissuance
            )
        }.toSet()
    }.shareLatest(scope = appScope)

    /**
     * Set the current cwa user with regards to listed persons in the certificates tab.
     * After calling this [personCertificates] will emit new values.
     * Setting it to null deletes it.
     */
    suspend fun setCurrentCwaUser(personIdentifier: CertificatePersonIdentifier?) {
        Timber.d("setCurrentCwaUser(personIdentifier=%s)", personIdentifier)
        personCertificatesSettings.setCurrentCwaUser(personIdentifier)
    }

    val personsBadgeCount: Flow<Int> = personCertificates.map { persons -> persons.sumOf { it.badgeCount } }

    /**
     * Find specific person by [CertificatePersonIdentifier.codeSHA256]
     * @param personIdentifierCode [String]
     */
    fun findPersonByIdentifierCode(personIdentifierCode: String): Flow<PersonCertificates?> =
        personCertificates.map { persons ->
            persons.find { it.personIdentifier?.codeSHA256 == personIdentifierCode }
        }

    private fun Set<VaccinatedPerson>.hasBoosterBadge(
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
