package de.rki.coronawarnapp.familytest.core.model

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.isFinalResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import org.joda.time.Instant

data class FamilyCoronaTest(
    @SerializedName("personName")
    val personName: String,
    val coronaTest: BaseCoronaTest,
) : CoronaTest by coronaTest {

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
        RECYCLED,
    }
}

fun BaseCoronaTest.markViewed(): BaseCoronaTest {
    return copy(uiState = uiState.copy(isViewed = true))
}

fun BaseCoronaTest.markBadgeAsViewed(): BaseCoronaTest {
    return copy(uiState = uiState.copy(didShowBadge = true))
}

fun BaseCoronaTest.updateResultNotification(sent: Boolean): BaseCoronaTest {
    return copy(uiState = uiState.copy(isResultAvailableNotificationSent = sent))
}

fun BaseCoronaTest.markDccCreated(created: Boolean): BaseCoronaTest {
    return copy(dcc = dcc.copy(isDccDataSetCreated = created))
}

fun BaseCoronaTest.recycle(now: Instant): BaseCoronaTest {
    return copy(recycledAt = now)
}

fun BaseCoronaTest.restore(): BaseCoronaTest {
    return copy(recycledAt = null)
}

fun BaseCoronaTest.updateTestResult(testResult: CoronaTestResult, now: Instant): BaseCoronaTest {
    return copy(testResult = testResult).let {
        if (testResult.isFinalResult && it.testResultReceivedAt == null) it.copy(testResultReceivedAt = now)
        else it
    }
}

fun BaseCoronaTest.updateLabId(labId: String?): BaseCoronaTest {
    return if (labId == null) copy(labId = labId) else this
}
