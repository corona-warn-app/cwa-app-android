package de.rki.coronawarnapp.appconfig.internal

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ConfigDataContainerTest : BaseTest() {

    @Test
    fun `cache validity is evaluated`() {
        val now = Instant.EPOCH
        val config = ConfigDataContainer(
            serverTime = now,
            localOffset = Duration.standardHours(1),
            mappedConfig = mockk(),
            configType = ConfigData.Type.LAST_RETRIEVED,
            identifier = "localetag",
            cacheValidity = Duration.standardSeconds(300)
        )

        // isValid uses the "updatedTimestamp" with updatedAt = serverTime + serverOffset = ourTime
        // Without the offset, we would already be off by one our
        config.isValid(now) shouldBe false
        config.isValid(now.plus(Duration.standardSeconds(300))) shouldBe false
        config.isValid(now.minus(Duration.standardSeconds(300))) shouldBe false
        config.isValid(now.plus(Duration.standardSeconds(1))) shouldBe false
        config.isValid(now.minus(Duration.standardSeconds(1))) shouldBe false

        val nowWithOffset = now.plus(config.localOffset)
        config.isValid(nowWithOffset) shouldBe true

        config.isValid(nowWithOffset.plus(Duration.standardSeconds(1))) shouldBe true
        config.isValid(nowWithOffset.minus(Duration.standardSeconds(1))) shouldBe true

        config.isValid(nowWithOffset.plus(Duration.standardSeconds(299))) shouldBe true
        config.isValid(nowWithOffset.minus(Duration.standardSeconds(299))) shouldBe true

        config.isValid(nowWithOffset.minus(Duration.standardSeconds(300))) shouldBe true
        config.isValid(nowWithOffset.plus(Duration.standardSeconds(300))) shouldBe true

        config.isValid(nowWithOffset.minus(Duration.standardSeconds(301))) shouldBe false
        config.isValid(nowWithOffset.plus(Duration.standardSeconds(301))) shouldBe false
    }

    @Test
    fun `cache validity can be set to 0`() {
        val config = ConfigDataContainer(
            serverTime = Instant.EPOCH,
            localOffset = Duration.standardHours(1),
            mappedConfig = mockk(),
            configType = ConfigData.Type.LAST_RETRIEVED,
            identifier = "localetag",
            cacheValidity = Duration.standardSeconds(0)
        )
        config.isValid(Instant.EPOCH) shouldBe false
        config.isValid(Instant.EPOCH.plus(Duration.standardHours(1))) shouldBe false
        config.isValid(Instant.EPOCH.plus(Duration.standardHours(24))) shouldBe false
        config.isValid(Instant.EPOCH.plus(Duration.standardDays(14))) shouldBe false

        config.isValid(Instant.EPOCH.minus(Duration.standardHours(1))) shouldBe false
        config.isValid(Instant.EPOCH.minus(Duration.standardHours(24))) shouldBe false
        config.isValid(Instant.EPOCH.minus(Duration.standardDays(14))) shouldBe false
    }

    @Test
    fun `updated at is based on servertime and offset`() {
        val config = ConfigDataContainer(
            serverTime = Instant.EPOCH,
            localOffset = Duration.standardHours(1),
            mappedConfig = mockk(),
            configType = ConfigData.Type.LAST_RETRIEVED,
            identifier = "localetag",
            cacheValidity = Duration.standardSeconds(0)
        )
        config.updatedAt shouldBe Instant.EPOCH.plus(Duration.standardHours(1))
    }

    @Test
    fun `device time correctness is checked via localOffset`() {
        val forOffset: (Duration) -> ConfigData = {
            ConfigDataContainer(
                serverTime = Instant.EPOCH.plus(Duration.standardDays(1)),
                localOffset = it,
                mappedConfig = mockk<ConfigMapping>().apply {
                    every { isDeviceTimeCheckEnabled } returns true
                },
                configType = ConfigData.Type.LAST_RETRIEVED,
                identifier = "localetag",
                cacheValidity = Duration.standardSeconds(0)
            )
        }

        forOffset(Duration.standardHours(1)).isDeviceTimeCorrect shouldBe true

        forOffset(Duration.ZERO).isDeviceTimeCorrect shouldBe true

        forOffset(Duration.standardHours(2).minus(1)).isDeviceTimeCorrect shouldBe true
        forOffset(Duration.standardHours(-2).plus(1)).isDeviceTimeCorrect shouldBe true

        forOffset(Duration.standardHours(2)).isDeviceTimeCorrect shouldBe false
        forOffset(Duration.standardHours(-2)).isDeviceTimeCorrect shouldBe false

        forOffset(Duration.standardHours(2).plus(1)).isDeviceTimeCorrect shouldBe false
        forOffset(Duration.standardHours(-2).minus(1)).isDeviceTimeCorrect shouldBe false
    }
}
