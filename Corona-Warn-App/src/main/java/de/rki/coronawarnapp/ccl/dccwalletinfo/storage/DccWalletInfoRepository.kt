package de.rki.coronawarnapp.ccl.dccwalletinfo.storage

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PersonWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDao
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoEntity
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccWalletInfoRepository @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val dccWalletInfoDao: DccWalletInfoDao,
    @AppScope private val appScope: CoroutineScope
) {
    val personWallets: Flow<Set<PersonWalletInfo>> = dccWalletInfoDao.getAll()
        .distinctUntilChanged()
        .map { personWallets ->
            personWallets.map { personWallet ->
                PersonWalletInfo(
                    personWallet.groupKey,
                    personWallet.dccWalletInfo
                )
            }.toSet()
        }.shareLatest(
            tag = TAG,
            scope = appScope + dispatcherProvider.IO
        )

    val blockedCertificateQrCodeHashes: Flow<Set<String>> = personWallets.map { walletInfoSet ->
        walletInfoSet.map { walletInfo ->
            walletInfo
                .dccWalletInfo
                ?.certificatesRevokedByInvalidationRules
                ?.certificateRef
                ?.map { certificateRef ->
                    certificateRef.qrCodeHash()
                } ?: emptyList()
        }
            .flatten()
            .toSet()
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

    suspend fun delete(personIds: Set<String>) {
        dccWalletInfoDao.deleteBy(personIds)
    }

    fun clear() = appScope.launch {
        Timber.d("Delete all DccWalletInfo.")
        dccWalletInfoDao.deleteAll()
    }

    companion object {
        private val TAG = tag<DccWalletInfoRepository>()
    }
}
