package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.coronatest.type.pcr.FetchingResult
import de.rki.coronawarnapp.coronatest.type.pcr.NoTest
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionDone
import de.rki.coronawarnapp.coronatest.type.pcr.TestError
import de.rki.coronawarnapp.coronatest.type.pcr.TestInvalid
import de.rki.coronawarnapp.coronatest.type.pcr.TestNegative
import de.rki.coronawarnapp.coronatest.type.pcr.TestPending
import de.rki.coronawarnapp.coronatest.type.pcr.TestPositive
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.submission.ui.homecards.TestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.TestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.TestPendingCard
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
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.Instant
import java.util.Date

object HomeData {

    private val todayAtNineFiftyFive = Instant.now().toDateTime()
        .withTime(9, 55, 0, 0).toInstant()

    object Tracing {

        val LOW_RISK_ITEM_NO_ENCOUNTERS_WITHOUT_INSTALL_TIME = LowRiskCard.Item(
            state = LowRisk(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = false,
                lastExposureDetectionTime = Instant.now(),
                lastEncounterAt = null,
                allowManualUpdate = false,
                daysWithEncounters = 0,
                daysSinceInstallation = 20,
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val LOW_RISK_ITEM_NO_ENCOUNTERS = LowRiskCard.Item(
            state = LowRisk(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = false,
                lastExposureDetectionTime = todayAtNineFiftyFive,
                lastEncounterAt = null,
                allowManualUpdate = false,
                daysWithEncounters = 0,
                daysSinceInstallation = 4,
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val LOW_RISK_ITEM_WITH_ENCOUNTERS = LowRiskCard.Item(
            state = LowRisk(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = false,
                lastExposureDetectionTime = todayAtNineFiftyFive,
                lastEncounterAt = todayAtNineFiftyFive.toLocalDateUtc(),
                allowManualUpdate = false,
                daysWithEncounters = 1,
                daysSinceInstallation = 4
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val INCREASED_RISK_ITEM = IncreasedRiskCard.Item(
            state = IncreasedRisk(
                riskState = RiskState.INCREASED_RISK,
                isInDetailsMode = false,
                lastExposureDetectionTime = todayAtNineFiftyFive,
                allowManualUpdate = false,
                daysWithEncounters = 1,
                lastEncounterAt = todayAtNineFiftyFive.toLocalDateUtc()
            ),
            onCardClick = {},
            onUpdateClick = {}
        )

        val TRACING_DISABLED_ITEM = TracingDisabledCard.Item(
            state = TracingDisabled(
                riskState = RiskState.LOW_RISK,
                isInDetailsMode = false,
                lastExposureDetectionTime = todayAtNineFiftyFive
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
                lastExposureDetectionTime = todayAtNineFiftyFive
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
            state = SubmissionDone(
                testRegisteredOn = Date()
            )
        )
    }
}
