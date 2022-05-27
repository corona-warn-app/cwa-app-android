package de.rki.coronawarnapp.recyclebin.common

import de.rki.coronawarnapp.reyclebin.common.Recyclable
import de.rki.coronawarnapp.reyclebin.common.retentionTimeInRecycleBin
import io.kotest.matchers.shouldBe
import java.time.Days
import java.time.Duration
import java.time.Hours
import java.time.Instant
import java.time.Minutes
import java.time.Seconds
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RecyclableExtensionsTest : BaseTest() {

    private val now = Instant.parse("2021-10-13T12:00:00.000Z")

    private val recyclable = object : Recyclable {
        override var recycledAt: Instant? = null
    }

    @BeforeEach
    fun setup() {
        recyclable.recycledAt = null
    }

    @Test
    fun `Not recycled item returns zero duration`() {
        recyclable.recycledAt shouldBe null
        recyclable.retentionTimeInRecycleBin(now) shouldBe Duration.ZERO
    }

    @Test
    fun `Retention time calculation from recycledAt until now`() {
        recyclable.recycledAt = Instant.parse("2021-10-13T11:59:59.999Z")
        recyclable.retentionTimeInRecycleBin(now) shouldBe Duration.ofMillis(1)

        recyclable.recycledAt = now.minus(Seconds.ONE.toStandardDuration())
        recyclable.retentionTimeInRecycleBin(now).toStandardSeconds().seconds shouldBe 1

        recyclable.recycledAt = now.minus(Minutes.ONE.toStandardDuration())
        recyclable.retentionTimeInRecycleBin(now).toStandardMinutes().minutes shouldBe 1

        recyclable.recycledAt = now.minus(Hours.ONE.toStandardDuration())
        recyclable.retentionTimeInRecycleBin(now).toStandardHours().hours shouldBe 1

        recyclable.recycledAt = now.minus(Days.ONE.toStandardDuration())
        recyclable.retentionTimeInRecycleBin(now).toStandardDays().days shouldBe 1
    }
}
