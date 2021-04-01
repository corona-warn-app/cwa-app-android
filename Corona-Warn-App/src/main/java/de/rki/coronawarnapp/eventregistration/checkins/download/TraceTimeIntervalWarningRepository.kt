package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import org.joda.time.Instant
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
    override suspend fun extractTraceTimeIntervalWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
        return warnings
    }

    override val warningPackageId: String
        get() = "id"
}

val warnings = (1L..1000L).map {
    createWarning(
        startIntervalDateStr = "2021-03-04T10:00+01:00",
        period = 6,
        transmissionRiskLevel = 8
    )
}

fun createWarning(
    startIntervalDateStr: String,
    period: Int,
    transmissionRiskLevel: Int
) = TraceWarning.TraceTimeIntervalWarning.newBuilder()
    // .locationIdHash = TODO: set location Id hash
    .setPeriod(period)
    .setStartIntervalNumber((Duration(Instant.parse(startIntervalDateStr).millis).standardMinutes / 10).toInt())
    .setTransmissionRiskLevel(transmissionRiskLevel)
    .build()
