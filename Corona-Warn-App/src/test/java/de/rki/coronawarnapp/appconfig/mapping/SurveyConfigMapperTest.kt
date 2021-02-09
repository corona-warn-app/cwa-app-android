package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.PpddEdusParameters
import de.rki.coronawarnapp.server.protocols.internal.v2.PpddPpacParameters
import io.kotest.matchers.shouldBe
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SurveyConfigMapperTest : BaseTest() {

    private fun createInstance() = SurveyConfigMapper()
    private val surveyURL = "https://www.urltoeventdrivenusersurvey.com"
    private val otpParam = "otp"

    private val defaultSurveyConfigContainer = SurveyConfigMapper.SurveyConfigContainer()

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
    fun `does not have event driven user survey parameters return default`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .build()

        rawConfig.hasEventDrivenUserSurveyParameters() shouldBe false

        createInstance().map(rawConfig) shouldBe defaultSurveyConfigContainer
    }

    @Test
    fun `only ppac is present return default`() {
        val eventDrivenUserSurveyParameters = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersAndroid.newBuilder()
            .setPpac(ppacParameters)

        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setEventDrivenUserSurveyParameters(eventDrivenUserSurveyParameters)
            .build()

        rawConfig.eventDrivenUserSurveyParameters.hasCommon() shouldBe false
        rawConfig.eventDrivenUserSurveyParameters.hasPpac() shouldBe true

        createInstance().map(rawConfig) shouldBe defaultSurveyConfigContainer
    }

    @Test
    fun `only common is present return default`() {
        val eventDrivenUserSurveyParameters = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersAndroid.newBuilder()
            .setCommon(commonParameters)

        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setEventDrivenUserSurveyParameters(eventDrivenUserSurveyParameters)
            .build()

        rawConfig.eventDrivenUserSurveyParameters.hasCommon() shouldBe true
        rawConfig.eventDrivenUserSurveyParameters.hasPpac() shouldBe false

        createInstance().map(rawConfig) shouldBe defaultSurveyConfigContainer
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
            surveyOnHighRiskUrl shouldBe commonParameters.surveyOnHighRiskUrl.toHttpUrl()
            safetyNetRequirements.apply {
                requireEvaluationTypeBasic shouldBe ppacParameters.requireBasicIntegrity
                requireCTSProfileMatch shouldBe ppacParameters.requireCTSProfileMatch
                requireBasicIntegrity shouldBe ppacParameters.requireEvaluationTypeBasic
                requireEvaluationTypeHardwareBacked shouldBe ppacParameters.requireEvaluationTypeHardwareBacked
            }
        }
    }

    @Test
    fun `Invalid url param return default`() {
        val commonParametersInvalidUrl = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersCommon.newBuilder()
            .setOtpQueryParameterName(otpParam)
            .setSurveyOnHighRiskEnabled(true)
            .setSurveyOnHighRiskUrl("Invalid url")

        val eventDrivenUserSurveyParameters = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersAndroid.newBuilder()
            .setCommon(commonParametersInvalidUrl)
            .setPpac(ppacParameters)

        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setEventDrivenUserSurveyParameters(eventDrivenUserSurveyParameters)
            .build()

        rawConfig.eventDrivenUserSurveyParameters.hasCommon() shouldBe true
        rawConfig.eventDrivenUserSurveyParameters.hasPpac() shouldBe true

        createInstance().map(rawConfig) shouldBe defaultSurveyConfigContainer
    }

    @Test
    fun `Empty otp query param name return default`() {
        val commonParametersEmptyOtp = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersCommon.newBuilder()
            .setOtpQueryParameterName("")
            .setSurveyOnHighRiskEnabled(true)
            .setSurveyOnHighRiskUrl(surveyURL)

        val eventDrivenUserSurveyParameters = PpddEdusParameters.PPDDEventDrivenUserSurveyParametersAndroid.newBuilder()
            .setCommon(commonParametersEmptyOtp)
            .setPpac(ppacParameters)

        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setEventDrivenUserSurveyParameters(eventDrivenUserSurveyParameters)
            .build()

        rawConfig.eventDrivenUserSurveyParameters.hasCommon() shouldBe true
        rawConfig.eventDrivenUserSurveyParameters.hasPpac() shouldBe true

        createInstance().map(rawConfig) shouldBe defaultSurveyConfigContainer
    }
}
