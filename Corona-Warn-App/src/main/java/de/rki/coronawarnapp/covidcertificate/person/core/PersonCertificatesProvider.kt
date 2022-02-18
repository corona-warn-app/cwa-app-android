package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.dcc.firstGroupWithPerson
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
    vaccinationRepository: VaccinationRepository,
    testCertificateRepository: TestCertificateRepository,
    recoveryCertificateRepository: RecoveryCertificateRepository,
    dccWalletInfoRepository: DccWalletInfoRepository,
    @AppScope private val appScope: CoroutineScope,
) {
    init {
        Timber.tag(TAG).d("PersonCertificatesProvider init(%s)", this)
    }

    val personCertificates: Flow<Set<PersonCertificates>> = combine(
        vaccinationRepository.vaccinationInfos,
        testCertificateRepository.cwaCertificates,
        recoveryCertificateRepository.cwaCertificates,
        personCertificatesSettings.currentCwaUser.flow,
        dccWalletInfoRepository.personWallets
    ) { vaccPersons, tests, recoveries, cwaUser, personWallets ->

        val personWalletsGroup = personWallets.associateBy { it.personGroupKey } // TODO: does it matter?
        val vaccinations = vaccPersons.flatMap { it.vaccinationCertificates }.toSet()
        val allCerts: Set<CwaCovidCertificate> = (vaccinations + tests + recoveries)

        val allCertsByPerson = allCerts.groupByPerson()

        if (allCertsByPerson.firstGroupWithPerson(cwaUser).isEmpty()) {
            Timber.tag(TAG).v("Resetting cwa user")
            personCertificatesSettings.currentCwaUser.update { null }
        }

        allCertsByPerson.map { certs ->
            val firstPersonIdentifier = certs.first().personIdentifier
            Timber.tag(TAG).v("PersonCertificates for %s with %d certs.", firstPersonIdentifier, certs.size)

            val dccWalletInfo =
                personWalletsGroup[firstPersonIdentifier.groupingKey]?.dccWalletInfo // TODO: don't compare like this

            // TODO: booster badge & vaccination repository should be updated in (EXPOSUREAPP-11724)
            val badgeCount = certs.filter { it.hasNotificationBadge }.count() +
                vaccPersons.boosterBadgeCount(firstPersonIdentifier, dccWalletInfo?.boosterNotification)

            Timber.tag(TAG).d("Badge count of %s =%s", firstPersonIdentifier.codeSHA256, badgeCount)

            PersonCertificates(
                certificates = certs.toCertificateSortOrder(),
                isCwaUser = certs.any { it.personIdentifier.isTheSamePerson(cwaUser) },
                // TODO: this could lead to multiple PersonCertificates where isCwaUser == true
                badgeCount = badgeCount,
                dccWalletInfo = dccWalletInfo
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
        personCertificatesSettings.currentCwaUser.update { personIdentifier }
    }

    val personsBadgeCount: Flow<Int> = personCertificates
        .map { persons -> persons.sumOf { it.badgeCount } }

    private fun Set<VaccinatedPerson>.boosterBadgeCount(
        personIdentifier: CertificatePersonIdentifier,
        boosterNotification: BoosterNotification?
    ): Int {
        if (boosterNotification == null) {
            return 0
        }
        val vaccinatedPerson = singleOrNull { it.identifier == personIdentifier }
        return when (hasBoosterRuleNotYetSeen(vaccinatedPerson, boosterNotification)) {
            true -> 1
            else -> 0
        }
    }

    private fun hasBoosterRuleNotYetSeen(
        vaccinatedPerson: VaccinatedPerson?,
        boosterNotification: BoosterNotification
    ) = vaccinatedPerson?.data?.lastSeenBoosterRuleIdentifier != boosterNotification.identifier

    companion object {
        private val TAG = PersonCertificatesProvider::class.simpleName!!
    }
}
