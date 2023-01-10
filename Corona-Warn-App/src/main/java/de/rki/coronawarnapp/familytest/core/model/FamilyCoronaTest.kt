package de.rki.coronawarnapp.familytest.core.model

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import java.time.Instant

data class FamilyCoronaTest(
    @JsonProperty("personName")
    val personName: String,
    @JsonProperty("coronaTest")
    val coronaTest: CoronaTest
) : BaseCoronaTest by coronaTest

internal fun FamilyCoronaTest.markViewed(): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.markViewed())
}

internal fun FamilyCoronaTest.markBadgeAsViewed(): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.markBadgeAsViewed())
}

internal fun FamilyCoronaTest.updateResultNotification(sent: Boolean): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.updateResultNotification(sent))
}

internal fun CoronaTest.markDccCreated(created: Boolean): CoronaTest {
    return copy(dcc = dcc.copy(isDccDataSetCreated = created))
}

internal fun FamilyCoronaTest.moveToRecycleBin(now: Instant): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.moveToRecycleBin(now))
}

internal fun CoronaTest.markAsNotified(notified: Boolean): CoronaTest {
    return copy(uiState = uiState.copy(isResultAvailableNotificationSent = notified))
}

internal fun FamilyCoronaTest.restore(): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.restore())
}

internal fun FamilyCoronaTest.updateTestResult(testResult: CoronaTestResult): FamilyCoronaTest {
    val updated = coronaTest.updateTestResult(testResult)
    val resultChanged = Pair(coronaTest.state, updated.state).hasChanged
    return copy(
        coronaTest = updated.copy(
            uiState = updated.uiState.update(resultChanged)
        )
    )
}

private fun CoronaTest.UiState.update(
    resultChanged: Boolean
) = when {
    // New result change should also trigger notification -> reset notification flag
    resultChanged -> copy(
        hasResultChangeBadge = resultChanged,
        isResultAvailableNotificationSent = false
    )
    // No change -> keep notification flag as is
    else -> copy(hasResultChangeBadge = resultChanged)
}

internal fun FamilyCoronaTest.updateLabId(labId: String): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.updateLabId(labId))
}

internal fun FamilyCoronaTest.updateSampleCollectedAt(sampleCollectedAt: Instant): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.updateSampleCollectedAt(sampleCollectedAt))
}

val Pair<CoronaTest.State, CoronaTest.State>.hasChanged: Boolean
    get() = this.first != this.second && this.second in setOf(
        CoronaTest.State.NEGATIVE,
        CoronaTest.State.POSITIVE,
        CoronaTest.State.INVALID
    )
