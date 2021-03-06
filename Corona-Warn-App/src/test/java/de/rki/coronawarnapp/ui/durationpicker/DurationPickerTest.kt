package de.rki.coronawarnapp.ui.durationpicker

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DurationPickerTest {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `check hours array`() {
        DurationPicker.hoursArray.count() shouldBe 24
        for (i in 0..9) {
            DurationPicker.hoursArray[i] shouldBe "0$i"
        }
        for (i in 10..23) {
            DurationPicker.hoursArray[i] shouldBe "$i"
        }
    }

    @Test
    fun `check minutes array`() {
        DurationPicker.minutesArray.count() shouldBe 4
        DurationPicker.minutesArray[0] shouldBe "00"
        for (i in 1..3) {
            DurationPicker.minutesArray[i] shouldBe "${i * 15}"
        }
    }

    @Test
    fun `check duration`() {
        DurationPicker.getDuration(0, 0).toContactDiaryFormat() shouldBe "00:00"
        DurationPicker.getDuration(1, 0).toContactDiaryFormat() shouldBe "01:00"
        DurationPicker.getDuration(23, 3).toContactDiaryFormat() shouldBe "23:45"
        DurationPicker.getDuration(9, 2).toContactDiaryFormat() shouldBe "09:30"
        DurationPicker.getDuration(10, 1).toContactDiaryFormat() shouldBe "10:15"
    }
}
