package de.rki.coronawarnapp.familytest.core.model

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import org.joda.time.Instant

data class FamilyCoronaTest(
    @SerializedName("personName")
    val personName: String,
    @SerializedName("coronaTest")
    val coronaTest: CoronaTest,
) : BaseCoronaTest by coronaTest


fun FamilyCoronaTest.markViewed(): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.markViewed())
}

fun FamilyCoronaTest.markBadgeAsViewed(): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.markBadgeAsViewed())
}

fun FamilyCoronaTest.updateResultNotification(sent: Boolean): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.updateResultNotification(sent))
}

fun CoronaTest.markDccCreated(created: Boolean): CoronaTest {
    return copy(dcc = dcc.copy(isDccDataSetCreated = created))
}

fun FamilyCoronaTest.moveToRecycleBin(now: Instant): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.moveToRecycleBin(now))
}

fun FamilyCoronaTest.restore(): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.restore())
}

fun FamilyCoronaTest.updateTestResult(testResult: CoronaTestResult): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.updateTestResult(testResult))
}

fun FamilyCoronaTest.updateLabId(labId: String): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.updateLabId(labId))
}

fun FamilyCoronaTest.updateSampleCollectedAt(sampleCollectedAt: Instant): FamilyCoronaTest {
    return copy(coronaTest = coronaTest.updateSampleCollectedAt(sampleCollectedAt))
}
