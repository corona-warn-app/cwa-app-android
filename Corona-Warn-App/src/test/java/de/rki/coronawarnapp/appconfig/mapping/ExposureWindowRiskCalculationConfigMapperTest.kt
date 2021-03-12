package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ExposureWindowRiskCalculationConfigMapperTest : BaseTest() {

    private fun createInstance() = ExposureWindowRiskCalculationConfigMapper()
    private val defaultRiskParams = RiskCalculationParametersOuterClass.RiskCalculationParameters.getDefaultInstance()
    private val defaultKeysMapping = AppConfigAndroid.DiagnosisKeysDataMapping.getDefaultInstance()

    private val transmissionRiskValueMapping =
        RiskCalculationParametersOuterClass.TransmissionRiskValueMapping.newBuilder()
            .setTransmissionRiskLevel(3)
            .setTransmissionRiskValue(0.6)
    private val riskParams = RiskCalculationParametersOuterClass.RiskCalculationParameters.newBuilder()
        .addTransmissionRiskValueMapping(transmissionRiskValueMapping)

    @Test
    fun `throws when riskCalculationParameters are missing`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .build()

        rawConfig.run {
            hasRiskCalculationParameters() shouldBe false
            hasDiagnosisKeysDataMapping() shouldBe false
        }

        val error = shouldThrow<ApplicationConfigurationInvalidException> {
            createInstance().map(rawConfig)
        }
        error.message shouldContain "Risk Calculation Parameters"
    }

    @Test
    fun `throws when diagnosisKeysDataMapping is missing`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setRiskCalculationParameters(defaultRiskParams)
            .build()

        rawConfig.run {
            hasRiskCalculationParameters() shouldBe true
            hasDiagnosisKeysDataMapping() shouldBe false
        }

        val error = shouldThrow<ApplicationConfigurationInvalidException> {
            createInstance().map(rawConfig)
        }
        error.message shouldContain "Diagnosis Keys Data Mapping"
    }

    @Test
    fun `throws when transmissionRiskValueMappingList is empty`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setRiskCalculationParameters(defaultRiskParams)
            .setDiagnosisKeysDataMapping(defaultKeysMapping)
            .build()

        rawConfig.run {
            hasRiskCalculationParameters() shouldBe true
            hasDiagnosisKeysDataMapping() shouldBe true
            riskCalculationParameters.transmissionRiskValueMappingList.isEmpty() shouldBe true
        }

        val error = shouldThrow<ApplicationConfigurationInvalidException> {
            createInstance().map(rawConfig)
        }
        error.message shouldContain "Transmission Risk Value Mapping List"
    }

    @Test
    fun `Mapping of transmissionRiskValueMappingList is correct`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setRiskCalculationParameters(riskParams)
            .setDiagnosisKeysDataMapping(defaultKeysMapping)
            .build()

        rawConfig.run {
            hasRiskCalculationParameters() shouldBe true
            hasDiagnosisKeysDataMapping() shouldBe true
            riskCalculationParameters.transmissionRiskValueMappingCount shouldBe 1

            riskCalculationParameters.transmissionRiskValueMappingList.first().run {
                transmissionRiskLevel shouldBe 3
                transmissionRiskValue shouldBe 0.6
            }
        }
    }
}
