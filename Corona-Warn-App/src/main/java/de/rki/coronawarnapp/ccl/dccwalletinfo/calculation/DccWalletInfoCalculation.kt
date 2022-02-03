package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.google.gson.Gson
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
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class DccWalletInfoCalculation @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    @BaseGson private val gson: Gson,
    private val jsonFunctionsWrapper: JsonFunctionsWrapper,
) {

    private var boosterRulesNode: JsonNode = NullNode.instance

    fun init(boosterRules: List<DccValidationRule>) {
        boosterRulesNode = gson.toJson(boosterRules).toJsonNode()
    }

    fun getDccWalletInfo(
        dccList: List<CwaCovidCertificate>
    ): DccWalletInfo {

        val output: JsonNode
        runBlocking {
            output = jsonFunctionsWrapper.evaluateFunction(
                FUNCTION_NAME,
                getDccWalletInfoInput(dccList = dccList).toJsonNode()
            )
        }
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
