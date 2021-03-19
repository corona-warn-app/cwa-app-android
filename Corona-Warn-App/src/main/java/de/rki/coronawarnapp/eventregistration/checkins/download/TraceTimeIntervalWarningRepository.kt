package de.rki.coronawarnapp.eventregistration.checkins.download

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.DateTime
import org.joda.time.Duration
import javax.inject.Inject

interface TraceTimeIntervalWarningRepository {

    val allWarningPackages: Flow<List<TraceTimeIntervalWarningPackage>>

    fun addWarningPackages(list: List<TraceTimeIntervalWarningPackage>)

    fun removeWarningPackages(list: List<TraceTimeIntervalWarningPackage>)
}

// proprietary dummy implementations
class FakeTraceTimeIntervalWarningRepository @Inject constructor() : TraceTimeIntervalWarningRepository {
    override val allWarningPackages: Flow<List<TraceTimeIntervalWarningPackage>>
        get() = listOf(listOf<TraceTimeIntervalWarningPackage>(DummyCheckInPackage)).asFlow()

    override fun addWarningPackages(list: List<TraceTimeIntervalWarningPackage>) {
        // TODO("Not yet implemented")
    }

    override fun removeWarningPackages(list: List<TraceTimeIntervalWarningPackage>) {
        // TODO("Not yet implemented")
    }
}

object DummyCheckInPackage : TraceTimeIntervalWarningPackage {
    override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
        return listOf(dummyEventCheckIn1, dummyEventCheckIn2)
    }
}

val checkInStart1 = DateTime(2021, 2, 20, 11, 45).millis

private val dummyEventCheckIn1 = TraceWarning.TraceTimeIntervalWarning.newBuilder()
    .setLocationGuidHash(ByteString.copyFromUtf8("69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060"))
    .setPeriod(6) // one hour
    .setStartIntervalNumber((Duration(checkInStart1).standardMinutes / 10).toInt())
    .setTransmissionRiskLevel(4)
    .build()

val checkInStart2 = DateTime(2021, 3, 20, 18, 45).millis

private val dummyEventCheckIn2 = TraceWarning.TraceTimeIntervalWarning.newBuilder()
    .setLocationGuidHash(ByteString.copyFromUtf8("69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060"))
    .setPeriod(6) // one hour
    .setStartIntervalNumber((Duration(checkInStart2).standardMinutes / 10).toInt())
    .setTransmissionRiskLevel(4)
    .build()
