package de.rki.coronawarnapp.ccl.dccwalletinfo.storage

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class DccWalletInfoRepository @Inject constructor() {
    val dccWalletInfo: Flow<Set<DccWalletInfo>> = emptyFlow()
}
