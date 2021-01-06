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
        val lowRiskItem = LowRiskCard.Item(
            state = LowRisk(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = true,
                lastExposureDetectionTime = Instant.now(),
                allowManualUpdate = true,
                daysWithEncounters = 1,
                activeTracingDays = 1
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val increasedRiskItem = IncreasedRiskCard.Item(
            state = IncreasedRisk(
                riskState = RiskState.INCREASED_RISK,
                isInDetailsMode = true,
                lastExposureDetectionTime = Instant.now(),
                allowManualUpdate = true,
                daysWithEncounters = 1,
                activeTracingDays = 1,
                lastEncounterAt = Instant.now()
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val tracingDisabledItem = TracingDisabledCard.Item(
            state = TracingDisabled(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = true,
                lastExposureDetectionTime = Instant.now()
            ),
            onCardClick = {},
            onEnableTracingClick = {}
        )

        val tracingProgressItem = TracingProgressCard.Item(
            state = TracingInProgress(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = true,
                tracingProgress = TracingProgress.Downloading
            ),
            onCardClick = {}
        )

        val tracingFailedItem = TracingFailedCard.Item(
            state = TracingFailed(
                riskState = RiskState.CALCULATION_FAILED,
                isInDetailsMode = true,
                lastExposureDetectionTime = Instant.now()
            ),
            onCardClick = {},
            onRetryClick = {}
        )
    }

    object Submission {
        val testUnregisteredItem = TestUnregisteredCard.Item(
            state = NoTest,
            onClickAction = {}
        )

        val testFetchingItem = TestFetchingCard.Item(
            state = FetchingResult
        )

        val testPositiveItem = TestPositiveCard.Item(
            state = TestPositive,
            onClickAction = {}
        )

        val testNegativeItem = TestNegativeCard.Item(
            state = TestNegative,
            onClickAction = {}
        )

        val testInvalidItem = TestInvalidCard.Item(
            state = TestInvalid,
            onDeleteTest = {}
        )

        val testErrorItem = TestErrorCard.Item(
            state = TestError,
            onDeleteTest = {}
        )

        val testPendingItem = TestPendingCard.Item(
            state = TestPending,
            onClickAction = {}
        )

        val testSubmissionDoneItem = TestSubmissionDoneCard.Item(
            state = SubmissionDone
        )
    }
}
