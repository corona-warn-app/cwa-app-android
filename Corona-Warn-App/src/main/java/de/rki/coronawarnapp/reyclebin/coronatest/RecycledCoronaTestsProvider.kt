package de.rki.coronawarnapp.reyclebin.coronatest

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultCollector
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RecycledCoronaTestsProvider @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
    private val familyTestRepository: FamilyTestRepository,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    private val analyticsTestResultCollector: AnalyticsTestResultCollector,
) {

    val testsMap: Flow<Map<TestIdentifier, CoronaTest>> = combine(
        coronaTestRepository.recycledCoronaTests,
        familyTestRepository.recycledFamilyTests
    ) { personalTests, familyTests ->
        personalTests.plus(familyTests).associateBy { it.identifier }
    }

    /**
     * Find corona test in recycled items
     * @return [CoronaTest] if found , otherwise `null`
     */
    suspend fun findCoronaTest(coronaTestQrCodeHash: String?): CoronaTest? {
        if (coronaTestQrCodeHash == null) return null
        Timber.tag(TAG).d("findCoronaTest(coronaTestQrCodeHash=%s)", coronaTestQrCodeHash)
        return testsMap.first()
            .values
            .find { it.qrCodeHash == coronaTestQrCodeHash }
            .also { Timber.tag(TAG).d("returning %s", it) }
    }

    suspend fun recycleCoronaTest(identifier: TestIdentifier) {
        Timber.tag(TAG).d("recycleCoronaTest(identifier=%s)", identifier)
        when (findTest(identifier)) {
            is PersonalCoronaTest -> coronaTestRepository.recycleTest(identifier)
            is FamilyCoronaTest -> familyTestRepository.recycleTest(identifier)
        }
    }

    suspend fun restoreCoronaTest(identifier: TestIdentifier) {
        Timber.tag(TAG).d("restoreCoronaTest(identifier=%s)", identifier)
        when (findTest(identifier)) {
            is PersonalCoronaTest -> {
                resetAnalytics(identifier)
                coronaTestRepository.restoreTest(identifier)
            }
            is FamilyCoronaTest -> familyTestRepository.restoreTest(identifier)
        }
    }

    suspend fun deleteCoronaTest(identifier: TestIdentifier) {
        try {
            Timber.tag(TAG).d("deleteCoronaTest(identifier=%s)", identifier)
            when (findTest(identifier)) {
                is PersonalCoronaTest -> coronaTestRepository.removeTest(identifier)
                is FamilyCoronaTest -> familyTestRepository.removeTest(identifier)
            }
        } catch (e: CoronaTestNotFoundException) {
            Timber.tag(TAG).e(e)
        }
    }

    suspend fun deleteAllCoronaTest(identifiers: Collection<TestIdentifier>) {
        Timber.tag(TAG).d("deleteAllCoronaTest(identifiers=%s)", identifiers)
        identifiers.toSet().forEach { deleteCoronaTest(identifier = it) }
    }

    private suspend fun resetAnalytics(identifier: TestIdentifier) {
        Timber.tag(TAG).d("resetAnalytics(identifier=%s)", identifier)
        coronaTestRepository.recycledCoronaTests
            .first().find { it.identifier == identifier }?.type
            ?.let {
                analyticsKeySubmissionCollector.reset(it)
                analyticsTestResultCollector.clear(it)
                Timber.tag(TAG).d("resetAnalytics() - end")
            }
    }

    private suspend fun findTest(identifier: TestIdentifier): CoronaTest? {
        return testsMap.first()[identifier]
    }

    companion object {
        private val TAG = tag<RecycledCoronaTestsProvider>()
    }
}
