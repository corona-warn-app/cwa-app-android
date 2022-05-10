package de.rki.coronawarnapp.bugreporting.debuglog.internal

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

class DebugLogStorageCheckTest : BaseTest() {

    @MockK lateinit var targetPath: File
    @MockK lateinit var logWriter: LogWriter

    private var ourTime: Long = 5001L

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { targetPath.usableSpace } returns 250 * 1000 * 1024L
        every { targetPath.parentFile } returns null
        every { targetPath.exists() } returns true
        coEvery { logWriter.write(any()) } just Runs
    }

    private fun createInstance() = DebugLogStorageCheck(
        targetPath = targetPath,
        timeProvider = { ourTime },
        logWriter = logWriter
    )

    @Test
    fun `normal not low storage case`() = runTest {
        val instance = createInstance()
        instance.isLowStorage() shouldBe false

        verify { logWriter wasNot Called }
    }

    @Test
    fun `on errors we print it but do expect low storage`() = runTest {
        val unexpectedException = IllegalThreadStateException("ಠ_ಠ")
        every { targetPath.usableSpace } throws unexpectedException

        val logSlot = slot<String>()
        coEvery { logWriter.write(capture(logSlot)) } just Runs

        val instance = createInstance()
        instance.isLowStorage() shouldBe true

        logSlot.captured.apply {
            this shouldContain "ಠ_ಠ"
            this shouldContain "IllegalThreadStateException"
        }
    }

    @Test
    fun `low storage default is 200MB`() = runTest {
        every { targetPath.usableSpace } returns 199 * 1000 * 1024L
        val instance = createInstance()
        instance.isLowStorage() shouldBe true

        ourTime += 60 * 1000L
        instance.isLowStorage() shouldBe true

        // We only write the warning once
        coVerify(exactly = 1) { logWriter.write(any()) }
    }

    @Test
    fun `target path does not exists`() = runTest {
        val parentPath = mockk<File>()
        every { parentPath.exists() } returns true
        every { parentPath.parentFile } returns null
        every { parentPath.usableSpace } returns 250 * 1000 * 1024L

        every { targetPath.exists() } returns false
        every { targetPath.parentFile } returns parentPath
        every { targetPath.usableSpace } returns 0L

        val instance = createInstance()
        instance.isLowStorage() shouldBe false

        verify { logWriter wasNot Called }
    }

    @Test
    fun `checks happen at most every 5 seconds`() = runTest {
        val instance = createInstance()
        instance.isLowStorage() shouldBe false

        every { targetPath.usableSpace } returns 1024L

        instance.isLowStorage() shouldBe false

        verify(exactly = 1) { targetPath.usableSpace }

        ourTime += 5000L

        instance.isLowStorage() shouldBe true

        every { targetPath.usableSpace } returns 250 * 1000 * 1024L

        instance.isLowStorage() shouldBe true

        verify(exactly = 2) { targetPath.usableSpace }
    }
}
