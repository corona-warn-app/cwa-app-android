package de.rki.coronawarnapp.contactdiary.ui.durationpicker

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContactDiaryDurationPickerFragmentTest {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `check hours array`() {
        ContactDiaryDurationPickerFragment.hoursArray.count() shouldBe 24
        for (i in 0..9) {
            ContactDiaryDurationPickerFragment.hoursArray[i] shouldBe "0$i"
        }
        for (i in 10..23) {
            ContactDiaryDurationPickerFragment.hoursArray[i] shouldBe "$i"
        }
    }

    @Test
    fun `check minutes array`() {
        ContactDiaryDurationPickerFragment.minutesArray.count() shouldBe 4
        ContactDiaryDurationPickerFragment.minutesArray[0] shouldBe "00"
        for (i in 1..3) {
            ContactDiaryDurationPickerFragment.minutesArray[i] shouldBe "${i * 15}"
        }
    }

    @Test
    fun `check duration`() {
        ContactDiaryDurationPickerFragment.getDuration(0, 0).toContactDiaryFormat() shouldBe "00:00"
        ContactDiaryDurationPickerFragment.getDuration(1, 0).toContactDiaryFormat() shouldBe "01:00"
        ContactDiaryDurationPickerFragment.getDuration(23, 3).toContactDiaryFormat() shouldBe "23:45"
        ContactDiaryDurationPickerFragment.getDuration(9, 2).toContactDiaryFormat() shouldBe "09:30"
        ContactDiaryDurationPickerFragment.getDuration(10, 1).toContactDiaryFormat() shouldBe "10:15"
    }
}
