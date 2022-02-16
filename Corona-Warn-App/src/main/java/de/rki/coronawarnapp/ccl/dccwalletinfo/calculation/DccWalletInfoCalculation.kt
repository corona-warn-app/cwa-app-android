package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.google.gson.Gson
import de.rki.coronawarnapp.ccl.configuration.model.CclInputParameters
import de.rki.coronawarnapp.ccl.configuration.model.getDefaultInputParameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclCertificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cose
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cwt
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfoInput
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTime
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.BaseJackson
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import javax.inject.Inject

class DccWalletInfoCalculation @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    @BaseGson private val gson: Gson,
    private val cclJsonFunctions: CclJsonFunctions,
    private val dispatcherProvider: DispatcherProvider
) {

    private var boosterRulesNode: JsonNode = NullNode.instance

    fun init(boosterRules: List<DccValidationRule>) {
        boosterRulesNode = gson.toJson(boosterRules).toJsonNode()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getDccWalletInfo(
        dccList: List<CwaCovidCertificate>,
        dateTime: DateTime = DateTime.now()
    ): DccWalletInfo = withContext(dispatcherProvider.IO) {
        val output = cclJsonFunctions.evaluateFunction(
            "getDccWalletInfo",
            getDccWalletInfoInput(
                dccList = dccList,
                boosterNotificationRules = boosterRulesNode,
                defaultInputParameters = getDefaultInputParameters(dateTime)
            ).toJsonNode()
        )

        mapper.treeToValue(output, DccWalletInfo::class.java)
    }

    @VisibleForTesting
    internal fun getDccWalletInfoInput(
        dccList: List<CwaCovidCertificate>,
        boosterNotificationRules: JsonNode,
        defaultInputParameters: CclInputParameters,
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
        return map {
            CclCertificate(
                barcodeData = it.qrCodeToDisplay.content,
                cose = Cose(it.dccData.kid),
                cwt = Cwt(
                    iss = it.headerIssuer,
                    iat = it.headerIssuedAt.seconds,
                    exp = it.headerExpiresAt.seconds
                ),
                hcert = it.dccData.certificateJson.toJsonNode(),
                validityState = it.getState().toCclState()
            )
        }
    }

    private fun DccWalletInfoInput.toJsonNode(): JsonNode = mapper.valueToTree(this)

    private fun String.toJsonNode(): JsonNode = mapper.readTree(this)
}
