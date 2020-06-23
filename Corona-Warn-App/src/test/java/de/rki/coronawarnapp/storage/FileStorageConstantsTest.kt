package de.rki.coronawarnapp.storage

import org.junit.Assert
import org.junit.Test

class FileStorageConstantsTest {

    @Test
    fun allFileStorageConstants() {
        Assert.assertEquals(FileStorageConstants.DAYS_TO_KEEP, 14)
        Assert.assertEquals(FileStorageConstants.FREE_SPACE_THRESHOLD, 200)
        Assert.assertEquals(FileStorageConstants.KEY_EXPORT_DIRECTORY_NAME, "key-export")
    }
}