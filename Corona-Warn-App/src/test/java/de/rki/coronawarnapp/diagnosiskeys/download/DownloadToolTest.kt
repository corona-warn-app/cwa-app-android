package de.rki.coronawarnapp.diagnosiskeys.download

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class DownloadToolTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    @Test
    fun `etag from header is stored as checksum`() {
        TODO()
    }

    @Test
    fun `if the etag is missing we take the file checksum`() {
        TODO()
    }
}
