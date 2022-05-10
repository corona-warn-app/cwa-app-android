package de.rki.coronawarnapp.covidcertificate.signature.core.storage

import android.content.Context
import de.rki.coronawarnapp.covidcertificate.signature.core.DscDataParser
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRawData.DSC_LIST_BASE64
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class LocalDscStorageTest : BaseIOTest() {

    @MockK lateinit var context: Context

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val privateFiles = File(testDir, "files")
    private val storageDir = File(privateFiles, "dsc_storage")
    private val storagePath = File(storageDir, "dsclist")

    private val dscDataParser = DscDataParser()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.filesDir } returns privateFiles
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    @Test
    fun `read and write should work`() = runTest {
        val rawData = DSC_LIST_BASE64.decodeBase64()!!.toByteArray()

        storagePath.exists() shouldBe false
        val storage = createStorage()
        storagePath.exists() shouldBe false

        storage.save(rawData)

        storagePath.exists() shouldBe true

        val dscData = storage.load()

        dscData shouldBe dscDataParser.parse(rawData, Instant.ofEpochMilli(storagePath.lastModified()))
    }

    private fun createStorage(): LocalDscStorage {
        return LocalDscStorage(context = context, dscDataParser = dscDataParser)
    }
}
