package de.rki.coronawarnapp.ccl.dccwalletinfo.storage

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PersonWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDao
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoEntity
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccWalletInfoRepository @Inject constructor(
    private val dccWalletInfoDao: DccWalletInfoDao,
    @AppScope private val appScope: CoroutineScope
) {
    val dccWalletInfo: Flow<Set<PersonWalletInfo>> = dccWalletInfoDao.getAll()
        .map { personWallets ->
            personWallets.map { personWallet ->
                PersonWalletInfo(
                    personWallet.groupKey,
                    personWallet.dccWalletInfo
                )
            }.toSet()
        }

    suspend fun save(
        personIdentifier: CertificatePersonIdentifier,
        dccWalletInfo: DccWalletInfo
    ) {
        dccWalletInfoDao.insert(
            DccWalletInfoEntity(
                groupKey = personIdentifier.groupingKey,
                dccWalletInfo = dccWalletInfo
            )
        )
    }

    fun clear() = appScope.launch {
        Timber.d("Delete all DccWalletInfo.")
        dccWalletInfoDao.deleteAll()
    }
}
