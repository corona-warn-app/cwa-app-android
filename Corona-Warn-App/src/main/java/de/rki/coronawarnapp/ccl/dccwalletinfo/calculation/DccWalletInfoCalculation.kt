package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.google.gson.Gson
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclCertificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cose
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cwt
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfoInput
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTime
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.BaseJackson
import de.rki.jfn.JsonFunctions
import javax.inject.Inject

class DccWalletInfoCalculation @Inject constructor(
    @BaseGson val gson: Gson,
    @BaseJackson val mapper: ObjectMapper
) {

    private lateinit var jsonFunctions: JsonFunctions
    private var boosterRulesNode: JsonNode = NullNode.instance

    fun init(
        cclConfiguration: CCLConfiguration,
        boosterRules: List<DccValidationRule>
    ) {
        jsonFunctions = JsonFunctions()
        //boosterRulesNode = gson.toJson(boosterRules).toJsonNode()
        cclConfiguration.logic.jfnDescriptors.forEach {
            jsonFunctions.registerFunction(it.name, it.definition.toJsonNode())
        }
    }

    fun getDccWalletInfo(
        dccList: List<CwaCovidCertificate>
    ): DccWalletInfo {

        val input = getDccWalletInfoInput(dccList = dccList).toJsonNode()
        val output = jsonFunctions.evaluateFunction(
            FUNCTION_NAME,
            input
        )

        return mapper.treeToValue(output, DccWalletInfo::class.java)
    }

    @VisibleForTesting
    internal fun getDccWalletInfoInput(
        dccList: List<CwaCovidCertificate>,
        boosterNotificationRules: JsonNode = boosterRulesNode,
        defaultInputParameters: CclInputParameters = getDefaultInputParameters(),
    ) = DccWalletInfoInput(
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
        boosterNotificationRules = boosterNotificationRules
    )

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
                hcert = it.dccData.certificateJson.toJsonNode(),
                validityState = it.getState().toCclState()
            )
        }
    }

    private fun Any.toJsonNode(): JsonNode = mapper.valueToTree(this)

    private fun String.toJsonNode(): JsonNode = mapper.readTree(this)
}

private const val FUNCTION_NAME = "getDccWalletInfo"
