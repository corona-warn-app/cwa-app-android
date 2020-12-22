package de.rki.coronawarnapp.contactdiary.util

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
}
