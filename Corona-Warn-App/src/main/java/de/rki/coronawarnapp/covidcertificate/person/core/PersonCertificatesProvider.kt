package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.person.model.PersonSettings
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
    ) { certificateContainer, cwaUser, personWallets, personsSettings ->

        val wallets = personWallets.associateBy { it.personGroupKey }
        val groupedCerts = certificateContainer.allCwaCertificates.groupByPerson().filterNot { it.isEmpty() }

        if (cwaUser != null && groupedCerts.findCertificatesForPerson(cwaUser).isEmpty()) {
            Timber.tag(TAG).v("Resetting cwa user")
            personCertificatesSettings.removeCurrentCwaUser()
        }

        groupedCerts.map { certs ->
            val sortedCerts = certs.toCertificateSortOrder()
            val identifier = certs.identifier
            val dccWalletInfo = sortedCerts.findWalletInfo(wallets)
            val settings = sortedCerts.findSettings(personsSettings, identifier)

            Timber.tag(TAG).v(
                "Person [code=%s, certsCount=%d, walletExist=%s, settings=%s]",
                identifier.codeSHA256,
                certs.size,
                dccWalletInfo != null,
                settings
            )

            val hasBoosterBadge = settings.hasBoosterBadge(dccWalletInfo?.boosterNotification)
            val hasDccReissuanceBadge = settings.hasReissuanceBadge(dccWalletInfo)
            val hasNewAdmissionStateBadge = settings.hasAdmissionStateChangedBadge()
            val badgeCount = certs.count { it.hasNotificationBadge } +
                hasBoosterBadge.toInt() +
                hasDccReissuanceBadge.toInt() +
                hasNewAdmissionStateBadge.toInt()

            Timber.tag(TAG).d("Person [code=%s, badgeCount=%s]", identifier.codeSHA256, badgeCount)

            PersonCertificates(
                certificates = sortedCerts,
                isCwaUser = certs.any { it.personIdentifier.belongsToSamePerson(cwaUser) },
                badgeCount = badgeCount,
                dccWalletInfo = dccWalletInfo,
                hasBoosterBadge = hasBoosterBadge,
                hasDccReissuanceBadge = hasDccReissuanceBadge,
                hasNewAdmissionState = hasNewAdmissionStateBadge
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
     * Find specific person by [CertificatePersonIdentifier.groupingKey]
     * @param groupKey [String]
     */
    fun findPersonByIdentifierCode(groupKey: String): Flow<PersonCertificates?> =
        personCertificates.map { persons ->
            persons.find { it.personIdentifier.belongsToSamePerson(groupKey.toIdentifier()) }
        }

    private fun PersonSettings?.hasBoosterBadge(boosterNotification: BoosterNotification?): Boolean {
        if (boosterNotification == null || !boosterNotification.visible) return false
        return hasNotSeenBoosterRuleYet(this, boosterNotification)
    }

    private fun hasNotSeenBoosterRuleYet(
        personSettings: PersonSettings?,
        boosterNotification: BoosterNotification
    ) = personSettings?.lastSeenBoosterRuleIdentifier != boosterNotification.identifier

    private fun PersonSettings?.hasReissuanceBadge(info: DccWalletInfo?): Boolean {
        if (this == null || info == null) return false
        return showDccReissuanceBadge && info.hasReissuance
    }

    private fun PersonSettings?.hasAdmissionStateChangedBadge(): Boolean {
        return this?.showAdmissionStateChangedBadge ?: false
    }

    private fun Boolean?.toInt(): Int = if (this == true) 1 else 0

    companion object {
        private val TAG = tag<PersonCertificatesProvider>()
    }
}
