package de.rki.coronawarnapp.recyclebin.common

import de.rki.coronawarnapp.reyclebin.common.Recyclable
import de.rki.coronawarnapp.reyclebin.common.retentionDaysInRecycleBin
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
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
    fun `Not recycled returns 0`() {
        recyclable.recycledAt shouldBe null
        recyclable.retentionDaysInRecycleBin(now) shouldBe 0
    }

    @Test
    fun `Days in retention calculation with ms precision`() {
        recyclable.recycledAt = Instant.parse("2021-10-11T12:00:00.001Z")
        recyclable.retentionDaysInRecycleBin(now) shouldBe 1

        recyclable.recycledAt = Instant.parse("2021-10-11T12:00:00.000Z")
        recyclable.retentionDaysInRecycleBin(now) shouldBe 2
    }
}
