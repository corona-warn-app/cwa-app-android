package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TraceLocationUrlTest : BaseTest() {

    @Test
    fun `locationUrl 1`() = runTest {
        val traceLocation = TraceLocation(
            id = 1,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
            description = "My Birthday Party",
            address = "at my place",
            startDate = 2687955L.secondsToInstant(),
            endDate = 2687991L.secondsToInstant(),
            defaultCheckInLengthInMinutes = null,
            cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
            cnPublicKey = PUB_KEY,
            version = TraceLocation.VERSION
        )

        traceLocation.locationUrl shouldBe
            "https://e.coronawarn.app?v=1#CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekATD3h6QBGmoIAR" +
            "JgOMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq-voxQ1EcFJ" +
            "QkEIujVwoCNK0MNGuDK1ayjGxeDc4UDGgQxMjM0IgQIARAC"
    }

    @Test
    fun `locationUrl 2`() = runTest {
        val traceLocation = TraceLocation(
            id = 2,
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER,
            description = "Icecream Shop",
            address = "Main Street 1",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = 10,
            cryptographicSeed = CRYPTOGRAPHIC_SEED.decodeBase64()!!,
            cnPublicKey = PUB_KEY,
            version = TraceLocation.VERSION
        )

        traceLocation.locationUrl shouldBe
            "https://e.coronawarn.app?v=1#CAESIAgBEg1JY2VjcmVhbSBTaG9wGg1NYWluIFN0cmVldCAxGmoIARJgOMTa6eYSiaDv8l" +
            "W13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq-voxQ1EcFJQkEIujVwoCNK0MNG" +
            "uDK1ayjGxeDc4UDGgQxMjM0IgYIARABGAo"
    }

    companion object {
        private const val CRYPTOGRAPHIC_SEED = "MTIzNA=="
        private const val PUB_KEY =
            "OMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9" +
                "eq+voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UD"
    }
}
