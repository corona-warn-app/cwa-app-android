package de.rki.coronawarnapp.familytest.core.model

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.CoronaTestProcessor.ServerResponse.Success
import org.joda.time.Instant

data class FamilyCoronaTest(
    @SerializedName("personName")
    val personName: String,
    @SerializedName("coronaTest")
    val coronaTest: CoronaTest
) : BaseCoronaTest by coronaTest

internal fun FamilyCoronaTest.markViewed(): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.markViewed())
}

internal fun FamilyCoronaTest.markBadgeAsViewed(): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.markBadgeAsViewed())
}

internal fun FamilyCoronaTest.markAsNotified(notified: Boolean): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.markAsNotified(notified))
}

internal fun CoronaTest.markDccCreated(created: Boolean): CoronaTest {
    return copy(dcc = dcc.copy(isDccDataSetCreated = created))
}

internal fun FamilyCoronaTest.moveToRecycleBin(now: Instant): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.moveToRecycleBin(now))
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

internal fun FamilyCoronaTest.updateFromResponse(
    updateResult: Success
): FamilyCoronaTest = updateTestResult(updateResult.coronaTestResult)
    .let { updated ->
        updateResult.labId?.let { labId ->
            updated.updateLabId(labId)
        } ?: updated
    }.let { updated ->
        updateResult.sampleCollectedAt?.let { collectedAt ->
            updated.updateSampleCollectedAt(collectedAt)
        } ?: updated
    }

private fun CoronaTest.UiState.update(
    resultChanged: Boolean
) = when {
    // New result change should also trigger notification -> reset notification flag
    resultChanged -> copy(
        hasResultChangeBadge = true,
        isResultAvailableNotificationSent = false
    )
    // No change -> keep notification flag as is
    else -> copy(hasResultChangeBadge = false)
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
