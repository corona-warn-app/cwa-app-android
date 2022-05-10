package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.CoronaRapidAntigenTestParametersContainer
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.joda.time.Duration
import org.junit.Test
import testhelpers.BaseTest

class CoronaTestParametersDistinctTest : BaseTest() {
    private val durationOf48H: Duration = Duration.standardHours(48)
    private val durationOf12H: Duration = Duration.standardHours(12)

    @Test
    fun `can we use distinctUntilChanged on CoronaTestParameters`() = runTest {
        val flow = flow {
            emit(CoronaRapidAntigenTestParametersContainer(durationOf48H))
            emit(CoronaRapidAntigenTestParametersContainer(Duration.standardHours(48)))
            emit(CoronaRapidAntigenTestParametersContainer(durationOf12H))
        }

        flow.distinctUntilChanged().drop(1).first().hoursToDeemTestOutdated shouldBe durationOf12H
    }
}
