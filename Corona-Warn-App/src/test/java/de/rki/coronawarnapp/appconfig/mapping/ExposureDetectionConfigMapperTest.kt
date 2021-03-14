package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.ExposureDetectionParameters.ExposureDetectionParametersAndroid
import io.kotest.matchers.shouldBe
import org.joda.time.Duration
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ExposureDetectionConfigMapperTest : BaseTest() {

    private fun createInstance() = ExposureDetectionConfigMapper()

    @Test
    fun `simple creation`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .build()
        createInstance().map(rawConfig).apply {
            exposureDetectionParameters shouldBe null
        }
    }

    @Test
    fun `detection interval 0 defaults to sane delay`() {
        val exposureDetectionParameters = ExposureDetectionParametersAndroid.newBuilder()
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setExposureDetectionParameters(exposureDetectionParameters)
            .build()
        createInstance().map(rawConfig).apply {
            minTimeBetweenDetections shouldBe Duration.standardDays(1)
            maxExposureDetectionsPerUTCDay shouldBe 0
        }
    }

    @Test
    fun `detection interval is mapped correctly`() {
        val exposureDetectionParameters = ExposureDetectionParametersAndroid.newBuilder().apply {
            maxExposureDetectionsPerInterval = 3
        }
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setExposureDetectionParameters(exposureDetectionParameters)
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
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setExposureDetectionParameters(exposureDetectionParameters)
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
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setExposureDetectionParameters(exposureDetectionParameters)
            .build()
        createInstance().map(rawConfig).apply {
            overallDetectionTimeout shouldBe Duration.standardMinutes(15)
        }
    }

    @Test
    fun `if protobuf is missing the datastructure we return defaults`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .build()
        createInstance().map(rawConfig).apply {
            overallDetectionTimeout shouldBe Duration.standardMinutes(15)
            minTimeBetweenDetections shouldBe Duration.standardHours(24 / 6)
            maxExposureDetectionsPerUTCDay shouldBe 6
        }
    }
}
