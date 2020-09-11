package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.util.HashExtensions.hashToMD5
import de.rki.coronawarnapp.util.HashExtensions.toMD5
import de.rki.coronawarnapp.util.HashExtensions.toSHA1
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class HashExtensionsTest : BaseIOTest() {

    private val testInput = "The Cake Is A Lie"
    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    @BeforeEach
    fun setup() {
        testDir.mkdirs()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()

        testDir.deleteRecursively()
    }

    @Test
    fun `hash string to MD5`() {
        testInput.toMD5() shouldBe "e42997e37d8d70d4927b0b396254c179"
    }

    @Test
    fun `hash string to SHA256`() {
        testInput.toSHA256() shouldBe "3afc82e0c5df81d1733fe0c289538a1a1f7a5038d5c261860a5c83952f4bcb61"
    }

    @Test
    fun `hash string to SHA1`() {
        testInput.toSHA1() shouldBe "4d57f806e5f714ebdb5a74a12fda9523fae21d76"
    }

    @Test
    fun `hash file to md5`() {
        val fileName = "FileToMD5.txt"
        val testFile = File(testDir, fileName)
        try {
            testFile.printWriter().use { out ->
                out.println("This is a test")
            }
            testFile.hashToMD5() shouldBe "ff22941336956098ae9a564289d1bf1b"
        } finally {
            testFile.delete()
        }
    }
}
