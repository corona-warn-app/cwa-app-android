package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.submission.ui.homecards.FetchingResult
import de.rki.coronawarnapp.submission.ui.homecards.NoTest
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionDone
import de.rki.coronawarnapp.submission.ui.homecards.TestError
import de.rki.coronawarnapp.submission.ui.homecards.TestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalid
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.TestNegative
import de.rki.coronawarnapp.submission.ui.homecards.TestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.TestPending
import de.rki.coronawarnapp.submission.ui.homecards.TestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestPositive
import de.rki.coronawarnapp.submission.ui.homecards.TestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.TestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.TestUnregisteredCard
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.tracing.states.IncreasedRisk
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.states.TracingDisabled
import de.rki.coronawarnapp.tracing.states.TracingFailed
import de.rki.coronawarnapp.tracing.states.TracingInProgress
import de.rki.coronawarnapp.tracing.ui.homecards.IncreasedRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.LowRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingDisabledCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingFailedCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingProgressCard
import org.joda.time.Instant

object HomeData {

    object Tracing {
        val LOW_RISK_ITEM = LowRiskCard.Item(
            state = LowRisk(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = false,
                lastExposureDetectionTime = Instant.now(),
                allowManualUpdate = false,
                daysWithEncounters = 1,
                activeTracingDays = 1
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val INCREASED_RISK_ITEM = IncreasedRiskCard.Item(
            state = IncreasedRisk(
                riskState = RiskState.INCREASED_RISK,
                isInDetailsMode = false,
                lastExposureDetectionTime = Instant.now(),
                allowManualUpdate = false,
                daysWithEncounters = 1,
                activeTracingDays = 1,
                lastEncounterAt = Instant.now()
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val TRACING_DISABLED_ITEM = TracingDisabledCard.Item(
            state = TracingDisabled(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = false,
                lastExposureDetectionTime = Instant.now()
            ),
            onCardClick = {},
            onEnableTracingClick = {}
        )

        val TRACING_PROGRESS_ITEM = TracingProgressCard.Item(
            state = TracingInProgress(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = false,
                tracingProgress = TracingProgress.Downloading
            ),
            onCardClick = {}
        )

        val TRACING_FAILED_ITEM = TracingFailedCard.Item(
            state = TracingFailed(
                riskState = RiskState.CALCULATION_FAILED,
                isInDetailsMode = false,
                lastExposureDetectionTime = Instant.now()
            ),
            onCardClick = {},
            onRetryClick = {}
        )
    }

    object Submission {
        val TEST_UNREGISTERED_ITEM = TestUnregisteredCard.Item(
            state = NoTest,
            onClickAction = {}
        )

        val TEST_FETCHING_ITEM = TestFetchingCard.Item(
            state = FetchingResult
        )

        val TEST_POSITIVE_ITEM = TestPositiveCard.Item(
            state = TestPositive,
            onClickAction = {}
        )

        val TEST_NEGATIVE_ITEM = TestNegativeCard.Item(
            state = TestNegative,
            onClickAction = {}
        )

        val TEST_INVALID_ITEM = TestInvalidCard.Item(
            state = TestInvalid,
            onDeleteTest = {}
        )

        val TEST_ERROR_ITEM = TestErrorCard.Item(
            state = TestError,
            onDeleteTest = {}
        )

        val TEST_PENDING_ITEM = TestPendingCard.Item(
            state = TestPending,
            onClickAction = {}
        )

        val TEST_SUBMISSION_DONE_ITEM = TestSubmissionDoneCard.Item(
            state = SubmissionDone
        )
    }
}
