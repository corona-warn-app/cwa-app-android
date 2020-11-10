package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.ExposureDetectionParameters.ExposureDetectionParametersAndroid
import io.kotest.matchers.shouldBe
import org.joda.time.Duration
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ExposureDetectionConfigMapperTest : BaseTest() {

    private fun createInstance() = ExposureDetectionConfigMapper()

    @Test
    fun `simple creation`() {
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setMinRiskScore(1)
            .build()
        createInstance().map(rawConfig).apply {
            exposureDetectionConfiguration shouldBe rawConfig.mapRiskScoreToExposureConfiguration()
            exposureDetectionParameters shouldBe rawConfig.androidExposureDetectionParameters
        }
    }

    @Test
    fun `detection interval can not be 0`() {
        val exposureDetectionParameters = ExposureDetectionParametersAndroid.newBuilder()
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setMinRiskScore(1)
            .setAndroidExposureDetectionParameters(exposureDetectionParameters)
            .build()
        createInstance().map(rawConfig).apply {
            minTimeBetweenDetections shouldBe Duration.standardHours(24 / 6)
            maxExposureDetectionsPerUTCDay shouldBe 6
        }
    }

    @Test
    fun `detection interval is mapped correctly`() {
        val exposureDetectionParameters = ExposureDetectionParametersAndroid.newBuilder().apply {
            maxExposureDetectionsPerInterval = 3
        }
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setMinRiskScore(1)
            .setAndroidExposureDetectionParameters(exposureDetectionParameters)
            .build()
        createInstance().map(rawConfig).apply {
            minTimeBetweenDetections shouldBe Duration.standardHours(24 / 3)
            maxExposureDetectionsPerUTCDay shouldBe 3
        }
    }

    @Test
    fun `detection timeout is mapped correctly`() {
        val exposureDetectionParameters = ExposureDetectionParametersAndroid.newBuilder().apply {
            overallTimeoutInSeconds = 10 * 60
        }
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setMinRiskScore(1)
            .setAndroidExposureDetectionParameters(exposureDetectionParameters)
            .build()
        createInstance().map(rawConfig).apply {
            overallDetectionTimeout shouldBe Duration.standardMinutes(10)
        }
    }

    @Test
    fun `detection timeout can not be 0`() {
        val exposureDetectionParameters = ExposureDetectionParametersAndroid.newBuilder().apply {
            overallTimeoutInSeconds = 0
        }
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setMinRiskScore(1)
            .setAndroidExposureDetectionParameters(exposureDetectionParameters)
            .build()
        createInstance().map(rawConfig).apply {
            overallDetectionTimeout shouldBe Duration.standardMinutes(15)
        }
    }
}
