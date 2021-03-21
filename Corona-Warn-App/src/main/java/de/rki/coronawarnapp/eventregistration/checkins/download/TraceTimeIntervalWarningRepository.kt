package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
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
        return listOf()
    }
}
