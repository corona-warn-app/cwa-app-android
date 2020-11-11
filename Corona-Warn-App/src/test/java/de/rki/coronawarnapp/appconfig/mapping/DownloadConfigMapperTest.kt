package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.KeyDownloadParameters
import io.kotest.matchers.shouldBe
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DownloadConfigMapperTest : BaseTest() {
    private fun createInstance() = KeyDownloadParametersMapper()

    @Test
    fun `parse etag missmatch for hours`() {
        val builder = KeyDownloadParameters.KeyDownloadParametersAndroid.newBuilder().apply {
            KeyDownloadParameters.DayPackageMetadata.newBuilder().apply {
                etag = "\"GoodMorningEtag\""
                region = "EUR"
                date = "2020-11-09"
            }.let { addCachedDayPackagesToUpdateOnETagMismatch(it) }
        }

        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setAndroidKeyDownloadParameters(builder)
            .build()

        createInstance().map(rawConfig).apply {
            invalidDayETags.first().apply {
                etag shouldBe "\"GoodMorningEtag\""
                region shouldBe LocationCode("EUR")
                day shouldBe LocalDate.parse("2020-11-09")
            }
        }
    }

    @Test
    fun `parse etag missmatch for days`() {
        val builder = KeyDownloadParameters.KeyDownloadParametersAndroid.newBuilder().apply {
            KeyDownloadParameters.HourPackageMetadata.newBuilder().apply {
                etag = "\"GoodMorningEtag\""
                region = "EUR"
                date = "2020-11-09"
                hour = 8
            }.let { addCachedHourPackagesToUpdateOnETagMismatch(it) }
        }

        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setAndroidKeyDownloadParameters(builder)
            .build()

        createInstance().map(rawConfig).apply {
            invalidHourEtags.first().apply {
                etag shouldBe "\"GoodMorningEtag\""
                region shouldBe LocationCode("EUR")
                day shouldBe LocalDate.parse("2020-11-09")
                hour shouldBe LocalTime.parse("08:00")
            }
        }
    }
}
