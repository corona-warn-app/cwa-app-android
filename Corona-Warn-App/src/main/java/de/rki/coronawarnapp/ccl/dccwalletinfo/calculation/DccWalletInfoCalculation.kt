package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dummyDccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import javax.inject.Inject

class DccWalletInfoCalculation @Inject constructor() {
    fun getDccWalletInfo(
        cclConfiguration: CCLConfiguration,
        dccList: List<DccData<*>>
    ): DccWalletInfo {
        val defaultInputParameters = getDefaultInputParameters()
        return dummyDccWalletInfo
    }
}
