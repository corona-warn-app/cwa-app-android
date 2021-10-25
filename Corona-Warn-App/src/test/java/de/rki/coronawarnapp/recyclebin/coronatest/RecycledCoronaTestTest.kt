package de.rki.coronawarnapp.recyclebin.coronatest

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.toRecycledCoronaTest
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RecycledCoronaTestTest: BaseTest() {

    @Test
    fun `Create RecycledCoronaTest from CoronaTest`() {
        val now = Instant.parse("2021-10-13T12:00:00.000Z")
        val coronaTestMockk = mockk<CoronaTest>()

        coronaTestMockk.toRecycledCoronaTest(recycledAt = now).run {
            recycledAt shouldBe now
            coronaTest shouldBe coronaTestMockk
        }
    }
}
