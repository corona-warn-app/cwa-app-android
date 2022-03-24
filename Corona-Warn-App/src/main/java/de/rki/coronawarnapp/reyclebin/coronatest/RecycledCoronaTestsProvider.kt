package de.rki.coronawarnapp.reyclebin.coronatest

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.PersonalTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
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
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RecycledCoronaTestsProvider @Inject constructor(
    private val personalTestRepository: PersonalTestRepository,
    private val familyTestRepository: FamilyTestRepository,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
    private val analyticsTestResultCollector: AnalyticsTestResultCollector,
) {

    private val testsMaps: Flow<Map<TestIdentifier, BaseCoronaTest>> = combine(
        personalTestRepository.recycledTests,
        familyTestRepository.recycledTests
    ) { personalTests, familyTests ->
        personalTests.plus(familyTests).associateBy { it.identifier }
    }
    val tests: Flow<Set<BaseCoronaTest>> = testsMaps.map { it.values.toSet() }

    /**
     * Find corona test in recycled items
     * @return [BaseCoronaTest] if found , otherwise `null`
     */
    suspend fun findCoronaTest(coronaTestQrCodeHash: String?): BaseCoronaTest? {
        if (coronaTestQrCodeHash == null) return null
        Timber.tag(TAG).d("findCoronaTest(coronaTestQrCodeHash=%s)", coronaTestQrCodeHash)
        return tests.first()
            .find { it.qrCodeHash == coronaTestQrCodeHash }
            .also { Timber.tag(TAG).d("returning %s", it) }
    }

    suspend fun recycleCoronaTest(identifier: TestIdentifier) {
        Timber.tag(TAG).d("recycleCoronaTest(identifier=%s)", identifier)
        // Test is not yet in the recycled ones
        personalTestRepository.recycleTest(identifier)
        familyTestRepository.recycleTest(identifier)
    }

    suspend fun restoreCoronaTest(identifier: TestIdentifier) {
        Timber.tag(TAG).d("restoreCoronaTest(identifier=%s)", identifier)
        when (findTest(identifier)) {
            is PersonalCoronaTest -> {
                resetAnalytics(identifier)
                personalTestRepository.restoreTest(identifier)
            }
            is FamilyCoronaTest -> familyTestRepository.restoreTest(identifier)
        }
    }

    suspend fun deleteCoronaTest(identifier: TestIdentifier) {
        try {
            Timber.tag(TAG).d("deleteCoronaTest(identifier=%s)", identifier)
            when (findTest(identifier)) {
                is PersonalCoronaTest -> personalTestRepository.removeTest(identifier)
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
        personalTestRepository.recycledTests
            .first().find { it.identifier == identifier }?.type
            ?.let {
                analyticsKeySubmissionCollector.reset(it)
                analyticsTestResultCollector.clear(it)
                Timber.tag(TAG).d("resetAnalytics() - end")
            }
    }

    private suspend fun findTest(identifier: TestIdentifier): BaseCoronaTest? {
        return testsMaps.first()[identifier]
    }

    companion object {
        private val TAG = tag<RecycledCoronaTestsProvider>()
    }
}
