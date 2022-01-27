package de.rki.coronawarnapp.ccl.dccwalletinfo

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfoWrapper
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DccWalletInfoProvider @Inject constructor(
    private val dccWalletInfoRepository: DccWalletInfoRepository
) {

    val dccWalletInfos: Flow<Set<DccWalletInfoWrapper>> = flowOf(setOf())
}
