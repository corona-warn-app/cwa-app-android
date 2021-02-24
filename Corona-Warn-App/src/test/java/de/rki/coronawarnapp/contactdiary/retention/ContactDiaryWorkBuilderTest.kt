package de.rki.coronawarnapp.contactdiary.retention

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactDiaryWorkBuilderTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `periodic work test`() {
        val periodicWork = ContactDiaryWorkBuilder().buildPeriodicWork()

        periodicWork.workSpec.intervalDuration shouldBe 24 * 60 * 60 * 1000
    }
}
