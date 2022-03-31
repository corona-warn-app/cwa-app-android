package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject

class CoronaTestProvider @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
    private val familyTestRepository: FamilyTestRepository,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
) {

    private val allTests: Flow<Set<BaseCoronaTest>> = combine(
        coronaTestRepository.coronaTests,
        familyTestRepository.familyTests
    ) { personalCoronaTests, familyCoronaTests ->
        personalCoronaTests.plus(familyCoronaTests)
    }.shareLatest(
        tag = TAG,
        scope = appScope + dispatcherProvider.IO
    )

    fun getTestForIdentifier(testIdentifier: TestIdentifier): Flow<BaseCoronaTest?> =
        allTests.map { tests ->
            tests.find { it.identifier == testIdentifier }
        }

    suspend fun setTestAsViewed(test: BaseCoronaTest) {
        when (test) {
            is PersonalCoronaTest -> {
                coronaTestRepository.markAsViewed(test.identifier)
                testResultAvailableNotificationService.cancelTestResultAvailableNotification()
            }
            is FamilyCoronaTest -> {
                familyTestRepository.markViewed(test.identifier)
            }
        }
    }

    suspend fun refreshTest(test: BaseCoronaTest) {
        when (test) {
            is PersonalCoronaTest -> {
                coronaTestRepository.refresh(test.type)
            }
            is FamilyCoronaTest -> {
                familyTestRepository.refresh() // TODO refresh specific test
            }
        }
    }

    suspend fun giveConsent(test: BaseCoronaTest) {
        if (test is PersonalCoronaTest) {
            Timber.v("giveConsentToSubmission(type=%s)", test.type)
            Timber.v("giveConsentToSubmission(type=$test.type): %s", test)
            coronaTestRepository.updateSubmissionConsent(identifier = test.identifier, consented = true)
        }
    }

    companion object {
        private val TAG = tag<CoronaTestProvider>()
    }
}
