package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateRef
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateReissuance
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.ReissuanceDivision
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
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
    init {
        Timber.tag(TAG).d("PersonCertificatesProvider init(%s)", this)
    }

    val personCertificates: Flow<Set<PersonCertificates>> = combine(
        certificatesProvider.certificateContainer,
        personCertificatesSettings.currentCwaUser.flow,
        dccWalletInfoRepository.personWallets
    ) { certificateContainer, cwaUser, personWallets ->

        val vaccPersons = certificateContainer.vaccinationInfos
        val personWalletsGroup = personWallets.associateBy { it.personGroupKey }
        val groupedCerts = certificateContainer.allCwaCertificates.groupByPerson()

        if (groupedCerts.findCertificatesForPerson(cwaUser).isEmpty()) {
            Timber.tag(TAG).v("Resetting cwa user")
            personCertificatesSettings.currentCwaUser.update { null }
        }

        groupedCerts.map { certs ->
            val firstPersonIdentifier = certs.first().personIdentifier
            Timber.tag(TAG).v("PersonCertificates for %s with %d certs.", firstPersonIdentifier, certs.size)

            val dccWalletInfo =
                personWalletsGroup[firstPersonIdentifier.groupingKey]?.dccWalletInfo

            // TODO: booster badge & vaccination repository should be updated in (EXPOSUREAPP-11724)
            val badgeCount = certs.filter { it.hasNotificationBadge }.count() +
                vaccPersons.boosterBadgeCount(firstPersonIdentifier, dccWalletInfo?.boosterNotification)

            Timber.tag(TAG).d("Badge count of %s =%s", firstPersonIdentifier.codeSHA256, badgeCount)

            // dummy reissuance data so that we can start working on the tile
            // TODO: remove once we get actual reissuance data from CCL
            val dummyCertificateReissuance = CertificateReissuance(
                reissuanceDivision = ReissuanceDivision(
                    visible = true,
                    titleText = SingleText(
                        type = "string",
                        localizedText = mapOf("de" to "Title Text"),
                        parameters = listOf()
                    ),
                    subtitleText = SingleText(
                        type = "string",
                        localizedText = mapOf("de" to "Subtitle Text"),
                        parameters = listOf()
                    ),
                    longText = SingleText(
                        type = "string",
                        localizedText = mapOf("de" to "Long Text"),
                        parameters = listOf()
                    ),
                    faqAnchor = "dcc_admission_state"
                ),
                certificateToReissue = Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = certs.first().qrCodeToDisplay.content,
                    )
                ),
                accompanyingCertificates = listOf()
            )

            PersonCertificates(
                certificates = certs.toCertificateSortOrder(),
                isCwaUser = certs.any { it.personIdentifier.belongsToSamePerson(cwaUser) },
                badgeCount = badgeCount,
                dccWalletInfo = dccWalletInfo?.copy(
                    certificateReissuance = dummyCertificateReissuance
                )
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
