package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CoronaTestProvider @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
    private val familyTestRepository: FamilyTestRepository,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
) {

    private val allTests: Flow<Set<BaseCoronaTest>> = combine(
        coronaTestRepository.coronaTests,
        familyTestRepository.familyTests
    ) { personalCoronaTests, familyCoronaTests ->
        personalCoronaTests.plus(familyCoronaTests)
    }

    fun findTestById(testIdentifier: TestIdentifier): Flow<BaseCoronaTest?> =
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
}
