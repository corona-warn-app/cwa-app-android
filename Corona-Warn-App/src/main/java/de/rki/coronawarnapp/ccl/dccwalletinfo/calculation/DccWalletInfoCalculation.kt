package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dummyDccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.jfn.JsonFunctions
import javax.inject.Inject

class DccWalletInfoCalculation @Inject constructor(
    private val jsonFunctions: JsonFunctions
) {
    fun getDccWalletInfo(
        cclConfiguration: CCLConfiguration,
        dccList: List<DccData<*>>
    ): DccWalletInfo {
        val defaultInputParameters = getDefaultInputParameters()

        // register
        //jsonFunctions.registerFunction(FUNCTION_NAME, logic)
        //jsonFunctions.evaluateFunction(FUNCTION_NAME, data)
        return dummyDccWalletInfo
    }
}

private const val FUNCTION_NAME = "getDCCWalletInfo"
