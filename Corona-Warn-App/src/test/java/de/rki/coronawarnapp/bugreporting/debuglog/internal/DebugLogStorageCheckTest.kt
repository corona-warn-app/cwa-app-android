package de.rki.coronawarnapp.bugreporting.debuglog.internal

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

class DebugLogStorageCheckTest : BaseTest() {

    @MockK lateinit var targetPath: File
    @MockK lateinit var logWriter: LogWriter

    private var currentTime: Long = 5001L

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { targetPath.usableSpace } returns 250 * 1000 * 1024L
        every { targetPath.parentFile } returns null
        every { targetPath.exists() } returns true
        every { logWriter.write(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
    }

    private fun createInstance() = DebugLogStorageCheck(
        targetPath = targetPath,
        timeProvider = { currentTime },
        logWriter = logWriter
    )

    @Test
    fun `normal not low storage case`() {
        val instance = createInstance()
        instance.isLowStorage() shouldBe false

        verify { logWriter wasNot Called }
    }

    @Test
    fun `on errors we print it but do expect low storage`() {
        val unexpectedError = Exception("ಠ_ಠ")
        every { targetPath.usableSpace } throws unexpectedError

        val logSlot = slot<LogLine>()
        every { logWriter.write(capture(logSlot)) } just Runs

        val instance = createInstance()
        instance.isLowStorage() shouldBe true

        logSlot.captured.throwable shouldBe unexpectedError
    }

    @Test
    fun `low storage default is 200MB`() {
        every { targetPath.usableSpace } returns 199 * 1000 * 1024L
        val instance = createInstance()
        instance.isLowStorage() shouldBe true

        currentTime += 60 * 1000L
        instance.isLowStorage() shouldBe true

        // We only write the warning once
        verify(exactly = 1) { logWriter.write(any()) }
    }

    @Test
    fun `target path does not exists`() {
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
    fun `checks happen at most every 5 seconds`() {
        val instance = createInstance()
        instance.isLowStorage() shouldBe false

        every { targetPath.usableSpace } returns 1024L

        instance.isLowStorage() shouldBe false

        verify(exactly = 1) { targetPath.usableSpace }

        currentTime += 5000L

        instance.isLowStorage() shouldBe true

        every { targetPath.usableSpace } returns 250 * 1000 * 1024L

        instance.isLowStorage() shouldBe true

        verify(exactly = 2) { targetPath.usableSpace }
    }
}
