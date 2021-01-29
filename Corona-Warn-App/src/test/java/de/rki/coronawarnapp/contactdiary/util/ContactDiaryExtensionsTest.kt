package de.rki.coronawarnapp.contactdiary.util

import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.sortByNameAndIdASC
import org.junit.Assert
import org.junit.jupiter.api.Test

class ContactDiaryExtensionsTest {

    @Test
    fun testFormatContactDiaryNameField() {
        Assert.assertEquals("   Granny   ".formatContactDiaryNameField(5), "Grann")
        Assert.assertEquals("   Grann y".formatContactDiaryNameField(5), "Grann")
        Assert.assertEquals("Granny   ".formatContactDiaryNameField(5), "Grann")
        Assert.assertEquals("    ".formatContactDiaryNameField(2), "  ")
    }

    @Test
    fun `upper and lowercase mix sorting for names`() {
        val testList = listOf(
            DefaultContactDiaryPerson(1, "Max Mustermann"),
            DefaultContactDiaryPerson(2, "Erika Musterfrau"),
            DefaultContactDiaryPerson(3, "erika musterfrau2")
        )

        val expectedResult = listOf(
            DefaultContactDiaryPerson(2, "Erika Musterfrau"),
            DefaultContactDiaryPerson(3, "erika musterfrau2"),
            DefaultContactDiaryPerson(1, "Max Mustermann")
        )

        // Test that lowercase "erika musterfrau2" is sorted to the 2nd position instead of the end
        Assert.assertEquals(expectedResult, testList.sortByNameAndIdASC())
    }

    @Test
    fun `sort by id when names are equal for names`() {
        val testList = listOf(
            DefaultContactDiaryPerson(1, "Max Mustermann"),
            DefaultContactDiaryPerson(3, "Erika Musterfrau"),
            DefaultContactDiaryPerson(2, "Erika Musterfrau")
        )

        val expectedResult = listOf(
            DefaultContactDiaryPerson(2, "Erika Musterfrau"),
            DefaultContactDiaryPerson(3, "Erika Musterfrau"),
            DefaultContactDiaryPerson(1, "Max Mustermann")
        )

        // Test that "Erika Musterfrau" with lower personId comes before the other one, even though it was
        // added as the last entry to the testList
        Assert.assertEquals(expectedResult, testList.sortByNameAndIdASC())
    }

    @Test
    fun `upper and lowercase mix sorting for places`() {
        val testList = listOf(
            DefaultContactDiaryLocation(1, "Berlin"),
            DefaultContactDiaryLocation(2, "At home"),
            DefaultContactDiaryLocation(3, "at home")
        )

        val expectedResult = listOf(
            DefaultContactDiaryLocation(2, "At home"),
            DefaultContactDiaryLocation(3, "at home"),
            DefaultContactDiaryLocation(1, "Berlin")
        )

        // Test that lowercase "at home" is sorted to the 2nd position instead of the end
        Assert.assertEquals(expectedResult, testList.sortByNameAndIdASC())
    }

    @Test
    fun `sort by id when names are equal for places`() {
        val testList = listOf(
            DefaultContactDiaryLocation(1, "Berlin"),
            DefaultContactDiaryLocation(3, "At home"),
            DefaultContactDiaryLocation(2, "At home")
        )

        val expectedResult = listOf(
            DefaultContactDiaryLocation(2, "At home"),
            DefaultContactDiaryLocation(3, "At home"),
            DefaultContactDiaryLocation(1, "Berlin")
        )

        // Test that "At home" with lower locationId comes before the other one, even though it was
        // added as the last entry to the testList
        Assert.assertEquals(expectedResult, testList.sortByNameAndIdASC())
    }
}
