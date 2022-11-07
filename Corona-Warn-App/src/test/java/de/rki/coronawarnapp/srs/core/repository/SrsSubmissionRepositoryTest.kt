package de.rki.coronawarnapp.srs.core.repository

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsTransformer
import de.rki.coronawarnapp.srs.core.AndroidIdProvider
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.srs.core.playbook.SrsPlaybook
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.task.ExposureKeyHistoryCalculations
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.time.Instant
import java.util.UUID

internal class SrsSubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var playbook: SrsPlaybook
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var tekCalculations: ExposureKeyHistoryCalculations
    @MockK lateinit var tekStorage: TEKHistoryStorage
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var checkInsRepo: CheckInRepository
    @MockK lateinit var checkInsTransformer: CheckInsTransformer
    @MockK lateinit var deviceAttestation: DeviceAttestation
    @MockK lateinit var srsSubmissionSettings: SrsSubmissionSettings
    @MockK lateinit var androidIdProvider: AndroidIdProvider

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun submit() {
    }

    @Test
    fun `currentOtp is still valid  - current time equals expiry time`() = runTest {
        val srsOtp = SrsOtp(
            uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
            expiresAt = Instant.parse("2022-11-07T12:10:10Z")
        )
        coEvery { srsSubmissionSettings.getOtp() } returns srsOtp
        instance().currentOtp(Instant.parse("2022-11-07T12:10:10Z")) shouldBe srsOtp
    }

    @Test
    fun `currentOtp is still valid  - current time is less than expiry time`() = runTest {
        val srsOtp = SrsOtp(
            uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
            expiresAt = Instant.parse("2023-11-07T12:10:10Z")
        )
        coEvery { srsSubmissionSettings.getOtp() } returns srsOtp
        instance().currentOtp(Instant.parse("2022-11-07T12:10:10Z")) shouldBe srsOtp
    }

    @Test
    fun `currentOtp is not valid  - current time is greater than expiry time`() = runTest {
        val srsOtp = SrsOtp(
            uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
            expiresAt = Instant.parse("2021-11-07T12:10:10Z")
        )
        coEvery { srsSubmissionSettings.getOtp() } returns srsOtp
        instance().currentOtp(Instant.parse("2022-11-07T12:10:10Z")).apply {
            this shouldNotBe srsOtp
            expiresAt shouldNotBe Instant.MIN
        }
    }

    @Test
    fun `No current Otp`() = runTest {
        coEvery { srsSubmissionSettings.getOtp() } returns null
        instance().currentOtp(Instant.parse("2022-11-07T12:10:10Z")).apply {
            this shouldNotBe null
            expiresAt shouldNotBe Instant.MIN
        }
    }

    @Test
    fun `attest`() {
    }

    private fun instance() = SrsSubmissionRepository(
        playbook = playbook,
        appConfigProvider = appConfigProvider,
        tekCalculations = tekCalculations,
        tekStorage = tekStorage,
        checkInsRepo = checkInsRepo,
        checkInsTransformer = checkInsTransformer,
        deviceAttestation = deviceAttestation,
        srsSubmissionSettings = srsSubmissionSettings,
        androidIdProvider = androidIdProvider,
        timeStamper = timeStamper
    )
}
