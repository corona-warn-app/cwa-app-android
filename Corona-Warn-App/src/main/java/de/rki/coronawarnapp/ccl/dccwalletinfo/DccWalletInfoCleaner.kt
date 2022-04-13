package de.rki.coronawarnapp.ccl.dccwalletinfo

import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccWalletInfoCleaner @Inject constructor(
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
) {

    suspend fun clean() = runCatching {
        val persons = personCertificatesProvider.personCertificates.first()
        val personGroupKeys = persons.map { it.personIdentifier.groupingKey }
        val dccWalletGroupKeys = dccWalletInfoRepository.personWallets.first().map { it.personGroupKey }
        val idsToClean = dccWalletGroupKeys subtract personGroupKeys.toSet()
        Timber.tag(TAG).d("Cleaning DccWalletInfo for [%d] persons", idsToClean.size)

        // Cleanup DccWalletInfo for persons who don't have certificates any longer
        // i.e all their certificates are recycled or have been deleted permanently.
        // Note: This is `NOT` affecting newly added persons who don't have DccWalletInfo yet
        dccWalletInfoRepository.delete(idsToClean.toSet())
    }.onFailure {
        Timber.tag(TAG).d(it, "clean() failed")
    }

    companion object {
        private val TAG = tag<DccWalletInfoCleaner>()
    }
}
