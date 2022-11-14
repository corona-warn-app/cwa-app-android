package de.rki.coronawarnapp.test.hometestcards.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestOutdatedCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.RegisterTestCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestResultItem
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

class HomeTestCardsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val cards: Flow<List<TestResultItem>> = flowOf(
        listOf(
            RegisterTestCard.Item(SubmissionStatePCR.NoTest) {},
            TestFetchingCard.Item(SubmissionStatePCR.FetchingResult),
            PcrTestPendingCard.Item(SubmissionStatePCR.TestPending) {},
            PcrTestReadyCard.Item(SubmissionStatePCR.TestResultReady) {},
            PcrTestInvalidCard.Item(SubmissionStatePCR.TestInvalid) {},
            PcrTestErrorCard.Item(SubmissionStatePCR.TestError) {},
            PcrTestNegativeCard.Item(SubmissionStatePCR.TestNegative(Instant.now())) {},
            PcrTestPositiveCard.Item(SubmissionStatePCR.TestPositive(Instant.now()), {}) {},
            PcrTestSubmissionDoneCard.Item(SubmissionStatePCR.SubmissionDone(Instant.now())) {},
            RapidTestPendingCard.Item(SubmissionStateRAT.TestPending) {},
            RapidTestReadyCard.Item(SubmissionStateRAT.TestResultReady) {},
            RapidTestInvalidCard.Item(SubmissionStateRAT.TestInvalid) {},
            RapidTestOutdatedCard.Item(SubmissionStateRAT.TestOutdated) {},
            RapidTestErrorCard.Item(SubmissionStateRAT.TestError) {},
            RapidTestNegativeCard.Item(SubmissionStateRAT.TestNegative(Instant.now())) {},
            RapidTestPositiveCard.Item(SubmissionStateRAT.TestPositive(Instant.now()), {}) {},
            RapidTestSubmissionDoneCard.Item(SubmissionStateRAT.SubmissionDone(Instant.now())) {}
        )
    )

    val homeItems: LiveData<List<HomeItem>> = cards.asLiveData(dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<HomeTestCardsFragmentViewModel>
}
