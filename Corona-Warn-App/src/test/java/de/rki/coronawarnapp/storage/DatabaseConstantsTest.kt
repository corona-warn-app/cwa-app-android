package de.rki.coronawarnapp.storage

import org.junit.Assert
import org.junit.Test

class DatabaseConstantsTest {

    @Test
    fun allDatabaseConstants() {
        Assert.assertEquals(DATABASE_NAME, "coronawarnapp-db")
    }
}
