package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dummyDccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData

class DccWalletInfoCalculation {
    fun getDccWalletInfo(
        cclConfiguration: CCLConfiguration,
        dccList: List<DccData<*>>
    ): DccWalletInfo {
        return dummyDccWalletInfo
    }
}
