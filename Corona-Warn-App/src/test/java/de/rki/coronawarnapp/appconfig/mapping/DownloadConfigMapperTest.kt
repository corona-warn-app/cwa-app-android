package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.KeyDownloadParameters
import io.kotest.matchers.shouldBe
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DownloadConfigMapperTest : BaseTest() {
    private fun createInstance() = KeyDownloadParametersMapper()

    @Test
    fun `parse etag missmatch for days`() {
        val builder = KeyDownloadParameters.KeyDownloadParametersAndroid.newBuilder().apply {
            KeyDownloadParameters.DayPackageMetadata.newBuilder().apply {
                etag = "\"GoodMorningEtag\""
                region = "EUR"
                date = "2020-11-09"
            }.let { addRevokedDayPackages(it) }
        }

        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setAndroidKeyDownloadParameters(builder)
            .build()

        createInstance().map(rawConfig).apply {
            revokedDayPackages.first().apply {
                etag shouldBe "\"GoodMorningEtag\""
                region shouldBe LocationCode("EUR")
                day shouldBe LocalDate.parse("2020-11-09")
            }
        }
    }

    @Test
    fun `parse etag missmatch for hours`() {
        val builder = KeyDownloadParameters.KeyDownloadParametersAndroid.newBuilder().apply {
            KeyDownloadParameters.HourPackageMetadata.newBuilder().apply {
                etag = "\"GoodMorningEtag\""
                region = "EUR"
                date = "2020-11-09"
                hour = 8
            }.let { addRevokedHourPackages(it) }
        }

        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .setAndroidKeyDownloadParameters(builder)
            .build()

        createInstance().map(rawConfig).apply {
            revokedHourPackages.first().apply {
                etag shouldBe "\"GoodMorningEtag\""
                region shouldBe LocationCode("EUR")
                day shouldBe LocalDate.parse("2020-11-09")
                hour shouldBe LocalTime.parse("08:00")
            }
        }
    }

    @Test
    fun `if the protobuf data structures are null we return defaults`() {
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .build()

        createInstance().map(rawConfig).apply {
            revokedDayPackages shouldBe emptyList()
            revokedHourPackages shouldBe emptyList()
            overallDownloadTimeout shouldBe Duration.standardMinutes(8)
            individualDownloadTimeout shouldBe Duration.standardSeconds(60)
        }
    }
}
