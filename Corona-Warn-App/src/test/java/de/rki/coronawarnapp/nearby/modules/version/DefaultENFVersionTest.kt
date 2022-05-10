package de.rki.coronawarnapp.nearby.modules.version

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes.API_NOT_CONNECTED
import com.google.android.gms.common.api.CommonStatusCodes.INTERNAL_ERROR
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask

@ExperimentalCoroutinesApi
internal class DefaultENFVersionTest : BaseTest() {

    @MockK lateinit var client: ExposureNotificationClient

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    fun createInstance() = DefaultENFVersion(
        client = client
    )

    @Test
    fun `current version is newer than the required version`() {
        every { client.version } returns MockGMSTask.forValue(17000000L)

        runTest {
            createInstance().apply {
                getENFClientVersion() shouldBe 17000000L
                shouldNotThrowAny {
                    requireMinimumVersion(ENFVersion.V1_6)
                }
            }
        }
    }

    @Test
    fun `current version is older than the required version`() {
        every { client.version } returns MockGMSTask.forValue(15000000L)

        runTest {
            createInstance().apply {
                getENFClientVersion() shouldBe 15000000L

                shouldThrow<OutdatedENFVersionException> {
                    requireMinimumVersion(ENFVersion.V1_6)
                }
            }
        }
    }

    @Test
    fun `current version is equal to the required version`() {
        every { client.version } returns MockGMSTask.forValue(16000000L)

        runTest {
            createInstance().apply {
                getENFClientVersion() shouldBe ENFVersion.V1_6
                shouldNotThrowAny {
                    requireMinimumVersion(ENFVersion.V1_6)
                }
            }
        }
    }

    @Test
    fun `API_NOT_CONNECTED exceptions are not treated as failures`() {
        every { client.version } returns MockGMSTask.forError(ApiException(Status(API_NOT_CONNECTED)))

        runTest {
            createInstance().apply {
                getENFClientVersion() shouldBe null
                shouldNotThrowAny {
                    requireMinimumVersion(ENFVersion.V1_6)
                }
            }
        }
    }

    @Test
    fun `rethrows unexpected exceptions`() {
        every { client.version } returns MockGMSTask.forError(ApiException(Status(INTERNAL_ERROR)))

        runTest {
            createInstance().apply {
                getENFClientVersion() shouldBe null

                shouldThrow<ApiException> {
                    requireMinimumVersion(ENFVersion.V1_6)
                }
            }
        }
    }

    @Test
    fun `isAtLeast is true for newer version`() {
        every { client.version } returns MockGMSTask.forValue(ENFVersion.V1_7)

        runTest {
            createInstance().isAtLeast(ENFVersion.V1_6) shouldBe true
        }
    }

    @Test
    fun `isAtLeast is true for equal version`() {
        every { client.version } returns MockGMSTask.forValue(ENFVersion.V1_6)

        runTest {
            createInstance().isAtLeast(ENFVersion.V1_6) shouldBe true
        }
    }

    @Test
    fun `isAtLeast is false for older version`() {
        every { client.version } returns MockGMSTask.forValue(ENFVersion.V1_6)

        runTest {
            createInstance().isAtLeast(ENFVersion.V1_7) shouldBe false
        }
    }

    @Test
    fun `isAtLeast returns false when client not connected`() {
        every { client.version } returns MockGMSTask.forError(ApiException(Status(API_NOT_CONNECTED)))

        runTest {
            createInstance().apply {
                shouldNotThrowAny {
                    isAtLeast(ENFVersion.V1_6) shouldBe false
                    isAtLeast(ENFVersion.V1_7) shouldBe false
                }
            }
        }
    }
}
