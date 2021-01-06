package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.tracing.states.IncreasedRisk
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.states.TracingDisabled
import de.rki.coronawarnapp.tracing.states.TracingInProgress
import de.rki.coronawarnapp.tracing.ui.homecards.IncreasedRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.LowRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingDisabledCard
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
    }

    object Submission {

    }
}
