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
                DurationClassification.MORE_THAN_10_MINUTES.key
            ) shouldBe DurationClassification.MORE_THAN_10_MINUTES

            fromContactDurationClassification(null) shouldBe null
            fromContactDurationClassification(
                DurationClassification.LESS_THAN_10_MINUTES
            ) shouldBe DurationClassification.LESS_THAN_10_MINUTES.key
        }
    }

    @Test
    fun `test mapping of legacy values`() {
        ContactDiaryRoomConverters().run {
            toContactDurationClassification("LessThan15Minutes") shouldBe DurationClassification.LESS_THAN_10_MINUTES
            toContactDurationClassification("MoreThan15Minutes") shouldBe DurationClassification.MORE_THAN_10_MINUTES
        }
    }
}
