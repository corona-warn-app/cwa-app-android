package de.rki.coronawarnapp.presencetracing.storage.repo

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.TraceLocationDatabase
import de.rki.coronawarnapp.presencetracing.storage.dao.TraceLocationDao
import de.rki.coronawarnapp.presencetracing.storage.entity.toTraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

internal class DefaultTraceLocationRepositoryTest : BaseTest() {

    @MockK lateinit var factory: TraceLocationDatabase.Factory
    @MockK lateinit var database: TraceLocationDatabase
    @MockK lateinit var appScope: CoroutineScope
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var traceLocationDao: TraceLocationDao

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { factory.create() } returns database
        every { database.traceLocationDao() } returns traceLocationDao
    }

    private fun createInstance() = DefaultTraceLocationRepository(factory, appScope, timeStamper)

    @Test
    fun `getTraceLocationsWithinRetention() should filter out stale trace locations`() = runTest {

        // Now = Jan 16th 2020, 00:00
        // TraceLocations should be kept for 15 days, so every TraceLocation with an end date before
        // Jan 1st 2020, 00:00 should get deleted
        every { timeStamper.nowUTC } returns Instant.parse("2020-01-16T00:00:00.000Z")

        val traceLocationWithinRetention = createTraceLocationWithEndDate(Instant.parse("2020-01-01T00:00:00.000Z"))

        // should be filtered out
        val staleTraceLocation = createTraceLocationWithEndDate(Instant.parse("2019-12-31T23:59:59.000Z"))

        every { traceLocationDao.allEntries() } returns flowOf(
            listOf(
                staleTraceLocation.toTraceLocationEntity(),
                traceLocationWithinRetention.toTraceLocationEntity()
            )
        )

        createInstance().traceLocationsWithinRetention.first() shouldBe listOf(traceLocationWithinRetention)
    }

    private fun createTraceLocationWithEndDate(endDate: Instant?) = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.UNRECOGNIZED,
        description = "",
        address = "",
        startDate = null,
        endDate = endDate,
        defaultCheckInLengthInMinutes = 30,
        cryptographicSeed = "seed byte array".encode(),
        cnPublicKey = "cnPublicKey"
    )
}
