package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.util.security.VerificationKeys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Cache
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseIOTest
import java.io.IOException

class StatisticsServerTest : BaseIOTest() {

    @MockK lateinit var api: StatisticsApiV1
    @MockK lateinit var verificationKeys: VerificationKeys
    @MockK lateinit var cache: Cache

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns false
        every { cache.evictAll() } just Runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = StatisticsServer(
        api = { api },
        verificationKeys = verificationKeys,
        cache = cache
    )

    @Test
    fun `successful download`() = runBlockingTest {
        coEvery { api.getStatistics() } returns Response.success(STATS_ZIP.toResponseBody())

        val server = createInstance()

        val rawStatistics = server.getRawStatistics()
        rawStatistics shouldBe STATS_PROTO

        verify(exactly = 1) { verificationKeys.hasInvalidSignature(any(), any()) }
    }

    @Test
    fun `data is faulty`() = runBlockingTest {
        coEvery { api.getStatistics() } returns Response.success("123ABC".decodeHex().toResponseBody())

        val server = createInstance()

        shouldThrow<IOException> {
            server.getRawStatistics()
        }
    }

    @Test
    fun `verification fails`() = runBlockingTest {
        coEvery { api.getStatistics() } returns Response.success(STATS_ZIP.toResponseBody())
        every { verificationKeys.hasInvalidSignature(any(), any()) } returns true

        val server = createInstance()

        shouldThrow<InvalidStatisticsSignatureException> {
            server.getRawStatistics()
        }
    }

    @Test
    fun `clear clears cache`() {
        createInstance().clear()
        verify { cache.evictAll() }
    }

    companion object {
        private val STATS_ZIP =
            (
                "504b03041400080808008d4a2f520000000000000000000000000a0000006578706f72742e736967018b0074ff0a88010a380a" +
                    "1864652e726b692e636f726f6e617761726e6170702d6465761a02763122033236322a13312e322e3834302e3130303435" +
                    "2e342e332e321001180122483046022100f363cc4813367bdd2fa03c91fc49c3521abf2db86ec8f5836f97f5d13f915285" +
                    "022100e8a51c68ece56ccc44de41cee75d766adb5f1d688d1773875dc1c6944bc2a1de504b0708a4db7bfc900000008b00" +
                    "0000504b03041400080808008d4a2f520000000000000000000000000a0000006578706f72742e62696ee3626164666211" +
                    "32e5e2e060146898fcef3fab103707a3200308dcb8ea20c4cfc104e6386cb9e4a0c0a4c10c9465060b1c5a69e728240bd4" +
                    "c604d52608d41621e1f3e19e73928304a302b30613d8546674535f34204c6560a8725060d460849bfac18d11622a8b40c3" +
                    "93c750538376c8b5be0efc602fc104520b00504b070867e7aa477b000000b2000000504b010214001400080808008d4a2f" +
                    "52a4db7bfc900000008b0000000a00000000000000000000000000000000006578706f72742e736967504b010214001400" +
                    "080808008d4a2f5267e7aa477b000000b20000000a00000000000000000000000000c80000006578706f72742e62696e50" +
                    "4b05060000000002000200700000007b0100000000"
                ).decodeHex()
        private val STATS_PROTO =
            (
                "0a040103020412350a080801108093feff05120b0801110000000000d8d540120f0802110000000040b4d24020022803120b08" +
                    "031100000000c2a93e41121d0a080802108093feff05121108011158184cf0de43624018012003280212350a0808031080" +
                    "93feff05120b0801110000000000e88040120f0802110000000000007a4020012801120b08031100000000f0460141121d" +
                    "0a0808041080e4e3ff05121108011152b81e85eb51f03f180220012801"
                ).decodeHex().toByteArray()
    }
}
