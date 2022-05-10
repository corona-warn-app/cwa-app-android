package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.TestDispatcherProvider
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

class FindMatchesTest {

    private fun createInstance() = CheckInWarningMatcher(
        TestDispatcherProvider(),
        CheckInCryptography(SecureRandom().asKotlinRandom(), AesCryptography())
    )

    @Test
    fun `findMatches works`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:45+01:00"
        )

        val warning1 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:30+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startIntervalDateStr = "2021-03-04T15:30+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )
        val warningPackage = object : TraceWarningPackage {
            override suspend fun extractUnencryptedWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override suspend fun extractEncryptedWarnings() = emptyList<CheckInOuterClass.CheckInProtectedReport>()

            override val packageId: WarningPackageId
                get() = "id"
        }
        runTest {
            val result = createInstance().findMatches(listOf(checkIn1, checkIn2), warningPackage)
            result.size shouldBe 1
            result[0].checkInId shouldBe 3L
            result[0].roundedMinutes shouldBe 15
        }
    }
}
