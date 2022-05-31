package de.rki.coronawarnapp.bugreporting.debuglog.upload.server

import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadOtp
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class LogUploadServerTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    @MockK private lateinit var timeStamper: TimeStamper
    @MockK private lateinit var uploadApiV1: LogUploadApiV1
    private val uploadOtp = LogUploadOtp(
        otp = "1",
        expirationDate = Instant.EPOCH.plus(Duration.ofDays(1))
    )
    private val snapshot = LogSnapshotter.Snapshot(
        path = File(testDir, "snapshot.zip")
    )
    private val uploadResponse = LogUploadApiV1.UploadResponse(
        id = "123",
        hash = null,
        errorCode = null
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { uploadApiV1.uploadLog(any(), any()) } returns uploadResponse
        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(1234)
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    fun createInstance() = LogUploadServer(
        timeStamper = timeStamper,
        uploadApiProvider = { uploadApiV1 }
    )

    @Test
    fun `log upload`() = runTest {
        val instance = createInstance()

        instance.uploadLog(uploadOtp = uploadOtp, snapshot = snapshot) shouldBe LogUpload(
            id = "123",
            uploadedAt = Instant.ofEpochMilli(1234)
        )
    }
}
