package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

class CheckInWarningMatcherTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = CheckInWarningMatcher(
        TestDispatcherProvider(),
        CheckInCryptography(SecureRandom().asKotlinRandom(), AesCryptography())
    )

    @Test
    fun `reports new matches`() {
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
            endDateStr = "2021-03-04T10:12+01:00"
        )

        val warning1 = createWarning(
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
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
            val result = createInstance().process(
                checkIns = listOf(checkIn1, checkIn2),
                warningPackages = listOf(warningPackage)
            )
            result.apply {
                successful shouldBe true
                processedPackages.single().warningPackage shouldBe warningPackage
                processedPackages.single().apply {
                    overlaps.size shouldBe 2
                    overlaps.any { it.checkInId == 2L } shouldBe true
                    overlaps.any { it.checkInId == 3L } shouldBe true
                }
            }
        }
    }

    @Test
    fun `report empty list if no matches found`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        val warning1 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
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
            val result = createInstance().process(
                checkIns = listOf(checkIn1, checkIn2),
                warningPackages = listOf(warningPackage),
            )

            result.apply {
                successful shouldBe true
                processedPackages.single().warningPackage shouldBe warningPackage
                processedPackages.single().overlaps.size shouldBe 0
            }
        }
    }

    @Test
    fun `report empty list if package is empty`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        val warningPackage = object : TraceWarningPackage {
            override suspend fun extractUnencryptedWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf()
            }

            override suspend fun extractEncryptedWarnings() = emptyList<CheckInOuterClass.CheckInProtectedReport>()

            override val packageId: WarningPackageId
                get() = "id"
        }

        runTest {
            val result = createInstance().process(
                warningPackages = listOf(warningPackage),
                checkIns = listOf(checkIn1, checkIn2),
            )

            result.apply {
                successful shouldBe true
                processedPackages.single().warningPackage shouldBe warningPackage
                processedPackages.single().overlaps.size shouldBe 0
            }
        }
    }

    @Test
    fun `report failure if matching throws exception`() {
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
            endDateStr = "2021-03-04T10:12+01:00"
        )

        @Suppress("TooGenericExceptionThrown")
        val warningPackage = object : TraceWarningPackage {
            override suspend fun extractUnencryptedWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                throw Exception()
            }

            override suspend fun extractEncryptedWarnings(): List<CheckInOuterClass.CheckInProtectedReport> {
                throw Exception()
            }

            override val packageId: String
                get() = "id"
        }

        runTest {
            val result = createInstance().process(
                checkIns = listOf(checkIn1, checkIn2),
                warningPackages = listOf(warningPackage),
            )

            result.apply {
                successful shouldBe false
                processedPackages shouldBe emptyList()
            }
        }
    }

    @Test
    fun `partial processing is possible on exceptions`() {
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
            endDateStr = "2021-03-04T10:12+01:00"
        )

        @Suppress("TooGenericExceptionThrown")
        val warningPackage1 = object : TraceWarningPackage {
            override suspend fun extractUnencryptedWarnings() = throw Exception()
            override suspend fun extractEncryptedWarnings() = throw Exception()

            override val packageId: WarningPackageId = "id1"
        }
        val warningPackage2 = object : TraceWarningPackage {
            override suspend fun extractUnencryptedWarnings() = listOf(
                createWarning(
                    traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
                    startIntervalDateStr = "2021-03-04T10:00+01:00",
                    period = 6,
                    transmissionRiskLevel = 8
                )
            )

            override suspend fun extractEncryptedWarnings() = emptyList<CheckInOuterClass.CheckInProtectedReport>()

            override val packageId: WarningPackageId = "id2"
        }

        runTest {
            val result = createInstance().process(
                checkIns = listOf(checkIn1, checkIn2),
                warningPackages = listOf(warningPackage1, warningPackage2),
            )

            result.apply {
                successful shouldBe false
                processedPackages.single().apply {
                    warningPackage shouldBe warningPackage2
                    overlaps.single().checkInId shouldBe checkIn2.id
                }
            }
        }
    }

    @Test
    fun `we do not match our own CheckIns`() {
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
            endDateStr = "2021-03-04T10:12+01:00",
            isSubmitted = true
        )

        val warning1 = createWarning(
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
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
            val result = createInstance().process(
                checkIns = listOf(checkIn1, checkIn2),
                warningPackages = listOf(warningPackage)
            )
            result.apply {
                successful shouldBe true
                processedPackages.single().warningPackage shouldBe warningPackage
                processedPackages.single().apply {
                    overlaps.size shouldBe 1
                    overlaps.any { it.checkInId == 2L } shouldBe true
                }
            }
        }
    }
}
