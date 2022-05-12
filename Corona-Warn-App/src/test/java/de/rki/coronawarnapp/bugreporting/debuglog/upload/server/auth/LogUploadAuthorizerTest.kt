package de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.LogUploadConfig
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File
import java.util.UUID

class LogUploadAuthorizerTest : BaseIOTest() {

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)

    @MockK private lateinit var authApiV1: LogUploadAuthApiV1
    @MockK private lateinit var deviceAttestation: DeviceAttestation
    @MockK private lateinit var configProvider: AppConfigProvider
    @MockK private lateinit var attestationResult: DeviceAttestation.Result
    @MockK private lateinit var configData: ConfigData
    @MockK private lateinit var logUploadConfig: LogUploadConfig
    @MockK private lateinit var safetyNetRequirements: SafetyNetRequirements

    private val attestationRequestSlot = slot<DeviceAttestation.Request>()

    private val uploadResponse = LogUploadAuthApiV1.AuthResponse(
        expirationDate = Instant.EPOCH.toString()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { configData.logUpload } returns logUploadConfig
        every { logUploadConfig.safetyNetRequirements } returns safetyNetRequirements

        coEvery { authApiV1.authOTP(any()) } returns uploadResponse
        coEvery { deviceAttestation.attest(capture(attestationRequestSlot)) } returns attestationResult
        attestationResult.apply {
            every { requirePass(safetyNetRequirements) } just Runs
            every { accessControlProtoBuf } returns PpacAndroid.PPACAndroid.getDefaultInstance()
        }
        coEvery { configProvider.currentConfig } returns flowOf(configData)
    }

    @AfterEach
    fun teardown() {
        testDir.deleteRecursively()
    }

    fun createInstance() = LogUploadAuthorizer(
        authApiProvider = { authApiV1 },
        deviceAttestation = deviceAttestation,
        configProvider = configProvider
    )

    @Test
    fun `otp generation`() = runTest {
        val expectedOtp = UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894")
        val instance = createInstance()

        instance.getAuthorizedOTP(otp = expectedOtp).apply {
            otp shouldBe expectedOtp.toString()
            expirationDate shouldBe Instant.EPOCH
        }

        attestationRequestSlot.captured.configData shouldBe configData
        attestationRequestSlot.captured.checkDeviceTime shouldBe false
    }
}
