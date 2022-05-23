package de.rki.coronawarnapp.presencetracing.warning.download.server

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest

internal class TraceWarningServerTest : BaseTest() {

    @MockK lateinit var unencryptedApi: TraceWarningUnencryptedApiV1
    @MockK lateinit var encryptedApi: TraceWarningEncryptedApiV2

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        with(encryptedApi) {
            coEvery { downloadKeyFileForHour(any(), any()) } returns Response.success("testdata-hour".toResponseBody())
            coEvery { getWarningPackageIds(any()) } returns DiscoveryResult(1, 1)
        }

        with(unencryptedApi) {
            coEvery { downloadKeyFileForHour(any(), any()) } returns Response.success("testdata-hour".toResponseBody())
            coEvery { getWarningPackageIds(any()) } returns DiscoveryResult(1, 1)
        }
    }

    @Test
    fun `downloadPackage UNENCRYPTED`() = runTest {
        server().downloadPackage(TraceWarningApi.Mode.UNENCRYPTED, LocationCode("DE"), 1L)
        coVerify(exactly = 1) {
            unencryptedApi.downloadKeyFileForHour(any(), any())
        }

        coVerify(exactly = 0) {
            encryptedApi.downloadKeyFileForHour(any(), any())
        }
    }

    @Test
    fun `downloadPackage ENCRYPTED`() = runTest {
        server().downloadPackage(TraceWarningApi.Mode.ENCRYPTED, LocationCode("DE"), 1L)
        coVerify(exactly = 0) {
            unencryptedApi.downloadKeyFileForHour(any(), any())
        }

        coVerify(exactly = 1) {
            encryptedApi.downloadKeyFileForHour(any(), any())
        }
    }

    @Test
    fun `getAvailableIds UNENCRYPTED`() = runTest {
        server().getAvailableIds(TraceWarningApi.Mode.UNENCRYPTED, LocationCode("DE"))
        coVerify(exactly = 1) {
            unencryptedApi.getWarningPackageIds(any())
        }

        coVerify(exactly = 0) {
            encryptedApi.getWarningPackageIds(any())
        }
    }

    @Test
    fun `getAvailableIds ENCRYPTED`() = runTest {
        server().getAvailableIds(TraceWarningApi.Mode.ENCRYPTED, LocationCode("DE"))
        coVerify(exactly = 0) {
            unencryptedApi.getWarningPackageIds(any())
        }

        coVerify(exactly = 1) {
            encryptedApi.getWarningPackageIds(any())
        }
    }

    private fun server() = TraceWarningServer(
        { unencryptedApi },
        { encryptedApi }
    )
}
