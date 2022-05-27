package de.rki.coronawarnapp.recyclebin.common

import de.rki.coronawarnapp.reyclebin.common.Recyclable
import de.rki.coronawarnapp.reyclebin.common.retentionTimeInRecycleBin
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Duration
import java.time.Instant

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

        recyclable.recycledAt = now.minus(Duration.ofSeconds(1))
        recyclable.retentionTimeInRecycleBin(now).seconds shouldBe 1

        recyclable.recycledAt = now.minus(Duration.ofMinutes(1))
        recyclable.retentionTimeInRecycleBin(now).toMinutes() shouldBe 1

        recyclable.recycledAt = now.minus(Duration.ofHours(1))
        recyclable.retentionTimeInRecycleBin(now).toHours() shouldBe 1

        recyclable.recycledAt = now.minus(Duration.ofDays(1))
        recyclable.retentionTimeInRecycleBin(now).toDays() shouldBe 1
    }
}
