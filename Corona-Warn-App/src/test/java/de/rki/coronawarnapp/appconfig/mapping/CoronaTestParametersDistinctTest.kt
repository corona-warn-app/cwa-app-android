package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.CoronaRapidAntigenTestParametersContainer
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import testhelpers.BaseTest

class CoronaTestParametersDistinctTest : BaseTest() {

    @Test
    fun `can we use distinctUntilChanged on CoronaTestParameters`() = runBlockingTest {
        val flow = flow {
            emit(CoronaRapidAntigenTestParametersContainer(48))
            emit(CoronaRapidAntigenTestParametersContainer(48))
            emit(CoronaRapidAntigenTestParametersContainer(12))
        }

        flow.distinctUntilChanged().drop(1).first().hoursToDeemTestOutdated shouldBe 12
    }
}
