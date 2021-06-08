package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.FetchingResult
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.NoTest
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.SubmissionDone
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestError
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestInvalid
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestNegative
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestPending
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR.TestPositive
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.ImmuneVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.VaccinationCard
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
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
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Duration
import org.joda.time.Instant

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

        val TEST_POSITIVE_ITEM = PcrTestPositiveCard.Item(
            state = TestPositive(
                testRegisteredAt = Instant.now()
            ),
            onClickAction = {}
        )

        val TEST_NEGATIVE_ITEM = PcrTestNegativeCard.Item(
            state = TestNegative(
                testRegisteredAt = Instant.now()
            ),
            onClickAction = {}
        )

        val TEST_NEGATIVE_ITEM_RAT = RapidTestNegativeCard.Item(
            state = SubmissionStateRAT.TestNegative(
                testRegisteredAt = Instant.now()
            ),
            onClickAction = {}
        )

        val TEST_INVALID_ITEM = PcrTestInvalidCard.Item(
            state = TestInvalid,
            onDeleteTest = {}
        )

        val TEST_ERROR_ITEM = PcrTestErrorCard.Item(
            state = TestError,
            onDeleteTest = {}
        )

        val TEST_PENDING_ITEM = PcrTestPendingCard.Item(
            state = TestPending,
            onClickAction = {}
        )

        val TEST_SUBMISSION_DONE_ITEM = PcrTestSubmissionDoneCard.Item(
            state = SubmissionDone(
                testRegisteredAt = Instant.now()
            ),
            onClickAction = {}
        )
    }

    object Vaccination {
        val INCOMPLETE = VaccinationCard.Item(
            vaccinatedPerson = mockk<VaccinatedPerson>().apply {
                every { fullName } returns "Andrea Schneider"
                every { identifier } returns mockk()
                every { getVaccinationStatus(any()) } returns VaccinatedPerson.Status.INCOMPLETE
                every { getTimeUntilImmunity(any()) } returns Duration.standardDays(14)
            },
            onClickAction = {}
        )
        val COMPLETE = VaccinationCard.Item(
            vaccinatedPerson = mockk<VaccinatedPerson>().apply {
                every { fullName } returns "Andrea Schneider"
                every { identifier } returns mockk()
                every { getVaccinationStatus(any()) } returns VaccinatedPerson.Status.COMPLETE
                every { getTimeUntilImmunity(any()) } returns Duration.standardDays(14)
            },
            onClickAction = {}
        )
        val IMMUNITY = ImmuneVaccinationCard.Item(
            vaccinatedPerson = mockk<VaccinatedPerson>().apply {
                every { fullName } returns "Andrea Schneider"
                every { identifier } returns mockk()
                every { getVaccinationStatus(any()) } returns VaccinatedPerson.Status.IMMUNITY
                every { getTimeUntilImmunity(any()) } returns Duration.standardDays(14)
            },
            onClickAction = {}
        )
    }
}
