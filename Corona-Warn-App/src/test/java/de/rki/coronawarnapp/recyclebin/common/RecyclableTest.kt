package de.rki.coronawarnapp.recyclebin.common

import de.rki.coronawarnapp.reyclebin.common.Recyclable
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class RecyclableTest : BaseTest() {

    private val recycledAtInstant = Instant.parse("2021-10-11T19:46:31.528Z")

    @Test
    fun `is recycled`() {
        val recycledObject = object : Recyclable {
            override val recycledAt: Instant
                get() = recycledAtInstant
        }

        with(recycledObject) {
            recycledAt shouldBe recycledAt
            isRecycled shouldBe true
            isNotRecycled shouldBe false
        }
    }

    @Test
    fun `is not recycled`() {
        val recycledObject = object : Recyclable {
            override val recycledAt: Instant?
                get() = null
        }

        with(recycledObject) {
            recycledAt shouldBe null
            isRecycled shouldBe false
            isNotRecycled shouldBe true
        }
    }

    @Test
    fun `Check days of retention for recycle bin`() {
        Recyclable.RETENTION_DAYS.toDays() shouldBe 30
    }
}
