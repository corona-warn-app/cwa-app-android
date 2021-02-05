package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.PpddEdusParameters
import de.rki.coronawarnapp.server.protocols.internal.v2.PpddPpacParameters
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SurveyConfigMapperTest : BaseTest() {

    private fun createInstance() = SurveyConfigMapper()
    private val surveyURL = "Imagine an URL ;)"
    private val otpParam = "otp"

    private val commonParameters = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersCommon.newBuilder()
        .setOtpQueryParameterName(otpParam)
        .setSurveyOnHighRiskEnabled(true)
        .setSurveyOnHighRiskUrl(surveyURL)

    private val ppacParameters = PpddPpacParameters.PPDDPrivacyPreservingAccessControlParametersAndroid.newBuilder()
        .setRequireBasicIntegrity(true)
        .setRequireCTSProfileMatch(true)
        .setRequireEvaluationTypeBasic(true)
        .setRequireEvaluationTypeHardwareBacked(true)

    @Test
    fun `does not have event driven user survey parameters`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .build()

        rawConfig.hasEventDrivenUserSurveyParameters() shouldBe false

        createInstance().map(rawConfig) shouldBe SurveyConfigMapper.SurveyConfigContainer()
    }

    @Test
    fun `only ppac is present`() {
        val eventDrivenUserSurveyParameters = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersAndroid.newBuilder()
            .setPpac(ppacParameters)

        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setEventDrivenUserSurveyParameters(eventDrivenUserSurveyParameters)
            .build()

        val defaultSurveyConfigContainer = SurveyConfigMapper.SurveyConfigContainer()

        rawConfig.eventDrivenUserSurveyParameters.hasCommon() shouldBe false
        rawConfig.eventDrivenUserSurveyParameters.hasPpac() shouldBe true

        createInstance().map(rawConfig).apply {
            otpQueryParameterName shouldBe defaultSurveyConfigContainer.otpQueryParameterName
            surveyOnHighRiskEnabled shouldBe defaultSurveyConfigContainer.surveyOnHighRiskEnabled
            surveyOnHighRiskUrl shouldBe defaultSurveyConfigContainer.surveyOnHighRiskUrl
            safetyNetRequirements shouldNotBe defaultSurveyConfigContainer.safetyNetRequirements
            safetyNetRequirements.apply {
                requireEvaluationTypeBasic shouldBe ppacParameters.requireBasicIntegrity
                requireCTSProfileMatch shouldBe ppacParameters.requireCTSProfileMatch
                requireBasicIntegrity shouldBe ppacParameters.requireEvaluationTypeBasic
                requireEvaluationTypeHardwareBacked shouldBe ppacParameters.requireEvaluationTypeHardwareBacked
            }
        }
    }

    @Test
    fun `only common is present`() {
        val eventDrivenUserSurveyParameters = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersAndroid.newBuilder()
            .setCommon(commonParameters)

        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setEventDrivenUserSurveyParameters(eventDrivenUserSurveyParameters)
            .build()

        val defaultSafetyNetRequirementsContainer = SafetyNetRequirementsContainer()

        rawConfig.eventDrivenUserSurveyParameters.hasCommon() shouldBe true
        rawConfig.eventDrivenUserSurveyParameters.hasPpac() shouldBe false

        createInstance().map(rawConfig).apply {
            otpQueryParameterName shouldBe commonParameters.otpQueryParameterName
            surveyOnHighRiskEnabled shouldBe commonParameters.surveyOnHighRiskEnabled
            surveyOnHighRiskUrl shouldBe commonParameters.surveyOnHighRiskUrl
            safetyNetRequirements shouldBe defaultSafetyNetRequirementsContainer
        }
    }

    @Test
    fun `everything is present`() {
        val eventDrivenUserSurveyParameters = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersAndroid.newBuilder()
            .setCommon(commonParameters)
            .setPpac(ppacParameters)

        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setEventDrivenUserSurveyParameters(eventDrivenUserSurveyParameters)
            .build()

        rawConfig.eventDrivenUserSurveyParameters.hasCommon() shouldBe true
        rawConfig.eventDrivenUserSurveyParameters.hasPpac() shouldBe true

        createInstance().map(rawConfig).apply {
            otpQueryParameterName shouldBe commonParameters.otpQueryParameterName
            surveyOnHighRiskEnabled shouldBe commonParameters.surveyOnHighRiskEnabled
            surveyOnHighRiskUrl shouldBe commonParameters.surveyOnHighRiskUrl
            safetyNetRequirements.apply {
                requireEvaluationTypeBasic shouldBe ppacParameters.requireBasicIntegrity
                requireCTSProfileMatch shouldBe ppacParameters.requireCTSProfileMatch
                requireBasicIntegrity shouldBe ppacParameters.requireEvaluationTypeBasic
                requireEvaluationTypeHardwareBacked shouldBe ppacParameters.requireEvaluationTypeHardwareBacked
            }
        }
    }
}
