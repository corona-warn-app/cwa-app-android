package de.rki.coronawarnapp.bugreporting.debuglog.upload

import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.UploadHistory
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.LogUploadServer
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadAuthorizer
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadOtp
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference
import java.io.IOException

class SnapshotUploaderTest : BaseTest() {

    @MockK lateinit var snapshotter: LogSnapshotter
    @MockK lateinit var uploadServer: LogUploadServer
    @MockK lateinit var authorizer: LogUploadAuthorizer
    @MockK lateinit var bugReportingSettings: BugReportingSettings
    @MockK lateinit var snapshot: LogSnapshotter.Snapshot

    private val logUploadOtp = LogUploadOtp(
        otp = "otp",
        expirationDate = Instant.EPOCH
    )

    private val uploadHistoryPref = mockFlowPreference(UploadHistory())

    private val expectedLogUpload = LogUpload(
        id = "123e4567-e89b-12d3-a456-426652340000",
        uploadedAt = Instant.parse("2020-08-20T23:00:00.000Z")
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { bugReportingSettings.uploadHistory } returns uploadHistoryPref

        coEvery { authorizer.getAuthorizedOTP(otp = any()) } returns logUploadOtp
        coEvery { snapshotter.snapshot() } returns snapshot
        coEvery { uploadServer.uploadLog(logUploadOtp, snapshot) } returns expectedLogUpload

        every { snapshot.delete() } returns true
    }

    private fun createInstance() = SnapshotUploader(
        snapshotter = snapshotter,
        uploadServer = uploadServer,
        authorizer = authorizer,
        bugReportingSettings = bugReportingSettings
    )

    @Test
    fun `upload a snapshot`() = runTest {
        val instance = createInstance()
        instance.uploadSnapshot() shouldBe expectedLogUpload

        uploadHistoryPref.value shouldBe UploadHistory(logs = listOf(expectedLogUpload))
    }

    @Test
    fun `snapshots are deleted on errors too`() = runTest {
        coEvery { uploadServer.uploadLog(logUploadOtp, snapshot) } throws IOException()

        val instance = createInstance()

        shouldThrow<IOException> {
            instance.uploadSnapshot()
        }
        verify { snapshot.delete() }
    }

    @Test
    fun `upload history is capped at 10`() = runTest {
        val existingEntries = (1..10L).map { LogUpload(id = "$it", Instant.ofEpochMilli(it)) }
        uploadHistoryPref.update { UploadHistory(logs = existingEntries) }

        val instance = createInstance()
        instance.uploadSnapshot() shouldBe expectedLogUpload

        uploadHistoryPref.value shouldBe UploadHistory(
            logs = existingEntries.subList(
                1,
                10
            ) + listOf(expectedLogUpload)
        )
    }
}
