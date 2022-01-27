package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclCertificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cose
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cwt
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfoInput
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTime
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dummyDccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.jfn.JsonFunctions
import javax.inject.Inject

class DccWalletInfoCalculation @Inject constructor() {

    lateinit var jsonFunctions: JsonFunctions
    lateinit var boosterRulesNode: JsonNode

    fun init(
        cclConfiguration: CCLConfiguration,
        boosterRules: List<DccValidationRule>
    ) {
        jsonFunctions = JsonFunctions()
        boosterRulesNode = boosterRules.toJsonNode()
        cclConfiguration.logic.jfnDescriptors.forEach {
            jsonFunctions.registerFunction(it.name, it.definition.toJsonNode())
        }
    }

    fun getDccWalletInfo(
        dccList: List<CwaCovidCertificate>
    ): DccWalletInfo {

        val input = getDccWalletInfoInput(dccList)
        val output = jsonFunctions.evaluateFunction(
            FUNCTION_NAME,
            input.toJsonNode()
        )

        // TODO convert output
        return dummyDccWalletInfo
    }

    private fun getDccWalletInfoInput(
        dccList: List<CwaCovidCertificate>
    ): DccWalletInfoInput {
        val defaultInputParameters = getDefaultInputParameters()
        return DccWalletInfoInput(
            os = defaultInputParameters.os,
            language = defaultInputParameters.language,
            now = SystemTime(
                timestamp = defaultInputParameters.now.timestamp,
                localDate = defaultInputParameters.now.localDate,
                localDateTime = defaultInputParameters.now.localDateTime,
                localDateTimeMidnight = defaultInputParameters.now.localDateTimeMidnight,
                utcDate = defaultInputParameters.now.utcDate,
                utcDateTime = defaultInputParameters.now.utcDateTime,
                utcDateTimeMidnight = defaultInputParameters.now.utcDateTimeMidnight,
            ),
            certificates = dccList.toCclCertificateList(),
            boosterNotificationRules = boosterRulesNode
        )
    }

    private fun List<CwaCovidCertificate>.toCclCertificateList(): List<CclCertificate> {
        return filter {
            it.getState() != CwaCovidCertificate.State.Recycled
        }.map {
            CclCertificate(
                barcodeData = it.qrCodeToDisplay.content,
                cose = Cose(it.dccData.kid),
                cwt = Cwt(
                    iss = it.headerIssuer,
                    iat = it.headerIssuedAt.millis / 1000,
                    exp = it.headerExpiresAt.millis / 1000
                ),
                hcert = it.dccData.certificateJson,
                validityState = it.getState().toCllState()
            )
        }
    }

    private fun CwaCovidCertificate.State.toCllState(): CclCertificate.Validity = when (this) {
        CwaCovidCertificate.State.Blocked -> CclCertificate.Validity.BLOCKED
        is CwaCovidCertificate.State.Expired -> CclCertificate.Validity.EXPIRED
        is CwaCovidCertificate.State.ExpiringSoon -> CclCertificate.Validity.EXPIRING_SOON
        is CwaCovidCertificate.State.Invalid -> CclCertificate.Validity.INVALID
        is CwaCovidCertificate.State.Valid -> CclCertificate.Validity.VALID
        else -> throw IllegalStateException("State not supported")
    }

    private fun Any.toJsonNode(): JsonNode = ObjectMapper().valueToTree(this)
}

private const val FUNCTION_NAME = "getDCCWalletInfo"
