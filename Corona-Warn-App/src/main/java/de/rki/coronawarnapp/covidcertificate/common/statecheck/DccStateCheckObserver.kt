package de.rki.coronawarnapp.covidcertificate.common.statecheck

import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccStateCheckObserver @Inject constructor(
    dscRepository: DscRepository,
    dccWalletInfoRepository: DccWalletInfoRepository,
    revocationRepository: RevocationRepository,
) {

    val dccStateValidity: Flow<DccStateValidity> = combine(
        dscRepository.dscSignatureList,
        dccWalletInfoRepository.blockedCertificateQrCodeHashes,
        revocationRepository.revocationList
    ) { dscSignatureList, blockedQrCodeHashes, revocationList ->
        DccStateValidity(
            dscSignatureList = dscSignatureList,
            blockedQrCodeHashes = blockedQrCodeHashes,
            revocationList = revocationList
        )
    }.distinctUntilChanged()

    suspend fun dccStateValidity() = dccStateValidity.first()
}

data class DccStateValidity(
    val dscSignatureList: DscSignatureList,
    val blockedQrCodeHashes: Set<String>,
    val revocationList: List<RevocationEntryCoordinates>
)
