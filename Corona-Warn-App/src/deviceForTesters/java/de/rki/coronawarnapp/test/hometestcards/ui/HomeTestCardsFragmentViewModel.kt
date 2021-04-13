package de.rki.coronawarnapp.test.hometestcards.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.submission.ui.homecards.FetchingResult
import de.rki.coronawarnapp.submission.ui.homecards.NoTest
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionDone
import de.rki.coronawarnapp.submission.ui.homecards.TestError
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalid
import de.rki.coronawarnapp.submission.ui.homecards.TestNegative
import de.rki.coronawarnapp.submission.ui.homecards.TestPending
import de.rki.coronawarnapp.submission.ui.homecards.TestPositive
import de.rki.coronawarnapp.submission.ui.homecards.TestResultItem
import de.rki.coronawarnapp.submission.ui.homecards.TestResultReady
import de.rki.coronawarnapp.submission.ui.homecards.TestUnregisteredCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date

class HomeTestCardsFragmentViewModel  @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val pcrTest: Flow<List<TestResultItem>> = flowOf(
        listOf(
            TestUnregisteredCard.Item(NoTest){},
            TestFetchingCard.Item(FetchingResult),
            PcrTestPendingCard.Item(TestPending){},
            PcrTestReadyCard.Item(TestResultReady){},
            PcrTestInvalidCard.Item(TestInvalid){},
            PcrTestErrorCard.Item(TestError){},
            PcrTestNegativeCard.Item(TestNegative),
            PcrTestPositiveCard.Item(TestPositive){},
            PcrTestSubmissionDoneCard.Item(SubmissionDone(Date()))
        )
    )

    val homeItems: LiveData<List<HomeItem>> = pcrTest.asLiveData(dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<HomeTestCardsFragmentViewModel>
}
