package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.Test

class MatchingTest {
    private val guid1 = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060"
    private val guid2 = "70eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060"
    private val guid3 = "71eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060"

    private val time = Instant.ofEpochMilli(1397210400000)
    private val end = Instant.ofEpochMilli(1397210400001)

    private val checkIn = CheckIn(
        id = 1L,
        guid = guid1,
        version = 1,
        type = 2,
        description = "my birthday",
        address = "New Orleans",
        traceLocationStart = time,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        traceLocationBytes = ByteString.EMPTY,
        signature = ByteString.EMPTY,
        checkInStart = time,
        checkInEnd = end,
        completed = false,
        createJournalEntry = false
    )

    private val checkIn2 = CheckIn(
        id = 2L,
        guid = guid2,
        version = 1,
        type = 2,
        description = "my birthday",
        address = "Cape Town",
        traceLocationStart = time,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        traceLocationBytes = ByteString.EMPTY,
        signature = ByteString.EMPTY,
        checkInStart = time,
        checkInEnd = end,
        completed = false,
        createJournalEntry = false
    )

    private val warningStart = DateTime(2021, 3, 20, 18, 45).millis

    private val warning1 = TraceWarning.TraceTimeIntervalWarning.newBuilder()
        .setLocationGuidHash(com.google.protobuf.ByteString.copyFromUtf8(guid1.toSHA256()))
        .setPeriod(6) // one hour
        .setStartIntervalNumber((Duration(warningStart).standardMinutes / 10).toInt())
        .setTransmissionRiskLevel(4)
        .build()

    private val warning2 = TraceWarning.TraceTimeIntervalWarning.newBuilder()
        .setLocationGuidHash(com.google.protobuf.ByteString.copyFromUtf8(guid2.toSHA256()))
        .setPeriod(6) // one hour
        .setStartIntervalNumber((Duration(warningStart).standardMinutes / 10).toInt())
        .setTransmissionRiskLevel(4)
        .build()

    private val warning3 = TraceWarning.TraceTimeIntervalWarning.newBuilder()
        .setLocationGuidHash(com.google.protobuf.ByteString.copyFromUtf8(guid3.toSHA256()))
        .setPeriod(6) // one hour
        .setStartIntervalNumber((Duration(warningStart).standardMinutes / 10).toInt())
        .setTransmissionRiskLevel(4)
        .build()

    @Test
    fun `test filter no relevant warnings`() {
        val checkIns: List<CheckIn> = listOf(checkIn, checkIn2)
        val traceTimeIntervalWarningPackage: TraceTimeIntervalWarningPackage = object :
            TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning3)
            }
        }
        runBlockingTest {
            val warnings = filterRelevantWarnings(checkIns, traceTimeIntervalWarningPackage)
            warnings.size shouldBe 0
        }
    }

    @Test
    fun `test filter no check ins`() {
        val checkIns: List<CheckIn> = listOf()
        val traceTimeIntervalWarningPackage: TraceTimeIntervalWarningPackage = object :
            TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2, warning3)
            }
        }
        runBlockingTest {
            val warnings = filterRelevantWarnings(checkIns, traceTimeIntervalWarningPackage)
            warnings.size shouldBe 0
        }
    }

    @Test
    fun `test filter no warnings`() {
        val checkIns: List<CheckIn> = listOf(checkIn, checkIn2)
        val traceTimeIntervalWarningPackage: TraceTimeIntervalWarningPackage = object :
            TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf()
            }
        }
        runBlockingTest {
            val warnings = filterRelevantWarnings(checkIns, traceTimeIntervalWarningPackage)
            warnings.size shouldBe 0
        }
    }

    @Test
    fun `test filterRelevantWarnings`() {
        val checkIns: List<CheckIn> = listOf(checkIn, checkIn2)
        val traceTimeIntervalWarningPackage: TraceTimeIntervalWarningPackage = object :
            TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2, warning3)
            }
        }
        runBlockingTest {
            val warnings = filterRelevantWarnings(checkIns, traceTimeIntervalWarningPackage)
            warnings.size shouldBe 2
            warnings shouldContain (warning1)
            warnings shouldContain (warning2)
        }
    }
}
