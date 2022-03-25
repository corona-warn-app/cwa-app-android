package de.rki.coronawarnapp.reyclebin.coronatest.handler

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class CoronaTestRestoreHandler @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider
) {

    suspend fun restoreCoronaTest(recycledCoronaTest: BaseCoronaTest): CoronaTestRestoreEvent {
        Timber.tag(TAG).d("restoreCoronaTest(recycledCoronaTest=%S)", recycledCoronaTest::class.java.simpleName)
        val currentCoronaTest by lazy { submissionRepository.testForType(recycledCoronaTest.type) }
        return when {
            recycledCoronaTest is PersonalCoronaTest && currentCoronaTest.first() != null ->
                CoronaTestRestoreEvent
                    .RestoreDuplicateTest(recycledCoronaTest.toRestoreRecycledTestRequest())
            else -> {
                recycledCoronaTestsProvider.restoreCoronaTest(recycledCoronaTest.identifier)
                CoronaTestRestoreEvent.RestoredTest(recycledCoronaTest)
            }
        }.also { Timber.tag(TAG).d("returning %S", it::class.java.simpleName) }
    }
}

private val TAG = tag<CoronaTestRestoreHandler>()
