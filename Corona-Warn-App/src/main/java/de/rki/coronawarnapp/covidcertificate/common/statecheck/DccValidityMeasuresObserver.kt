package de.rki.coronawarnapp.covidcertificate.common.statecheck

import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidityMeasuresObserver @Inject constructor(
    @AppScope appScope: CoroutineScope,
    dscRepository: DscRepository,
    dccWalletInfoRepository: DccWalletInfoRepository,
    revocationRepository: RevocationRepository,
) {

    val dccValidityMeasures: Flow<DccValidityMeasures> = combine(
        dscRepository.dscSignatureList,
        dccWalletInfoRepository.blockedQrCodeHashes,
        revocationRepository.revocationList
    ) { dscSignatureList, blockedQrCodeHashes, revocationList ->
        DccValidityMeasures(
            dscSignatureList = dscSignatureList,
            blockedQrCodeHashes = blockedQrCodeHashes,
            revocationList = revocationList
        )
    }.distinctUntilChanged().shareLatest(scope = appScope)

    suspend fun dccValidityMeasures() = dccValidityMeasures.first()
}

data class DccValidityMeasures(
    val dscSignatureList: DscSignatureList,
    val blockedQrCodeHashes: Set<String>,
    val revocationList: List<CachedRevocationChunk>
)
