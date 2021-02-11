package de.rki.coronawarnapp.contactdiary.storage.internal.converters

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactDiaryRoomConvertersTest : BaseTest() {

    @Test
    fun `test person encounter duration classification`() {
        ContactDiaryRoomConverters().apply {
            toContactDurationClassification(null) shouldBe null
            toContactDurationClassification(
                DurationClassification.MORE_THAN_15_MINUTES.key
            ) shouldBe DurationClassification.MORE_THAN_15_MINUTES

            fromContactDurationClassification(null) shouldBe null
            fromContactDurationClassification(
                DurationClassification.LESS_THAN_15_MINUTES
            ) shouldBe DurationClassification.LESS_THAN_15_MINUTES.key
        }
    }
}
