package de.rki.coronawarnapp.ccl.dccwalletinfo

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfoWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DccWalletInfoProvider @Inject constructor() {

    val dccWalletInfos: Flow<Set<DccWalletInfoWrapper>> = flowOf(setOf())
}
