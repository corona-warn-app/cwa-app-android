package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var tekHistoryStorage: TEKHistoryStorage
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    fun createInstance(scope: CoroutineScope) = SubmissionRepository(
        scope = scope,
        submissionSettings = submissionSettings,
        tekHistoryStorage = tekHistoryStorage,
        coronaTestRepository = coronaTestRepository,
    )

    @Test
    fun todo() {
        // TODO
    }
}
