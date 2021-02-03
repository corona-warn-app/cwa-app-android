package de.rki.coronawarnapp.tracing.states

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IncreasedRiskTest {

    @MockK private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getRiskContactLast() should return null when lastEncounterAt == null`() {
        val risk = defaultRisk.copy(lastEncounterAt = null)
        risk.getRiskContactLast(context) shouldBe null
    }

    @Test
    fun `getRiskContactLast() should return correct string when daysWithEncounters == 1`() {
        val slot = slot<String>()
        every {
            context.getString(
                R.string.risk_card_high_risk_most_recent_body_encounter_on_single_day,
                capture(slot)
            )
        } answers { "On ${slot.captured}" }

        val risk = defaultRisk.copy(daysWithEncounters = 1)
        risk.getRiskContactLast(context) shouldBe "On ${slot.captured}"
    }

    @Test
    fun `getRiskContactLast() should return correct string when daysWithEncounters is greater 1`() {
        val slot = slot<String>()
        every {
            context.getString(
                R.string.risk_card_high_risk_most_recent_body_encounters_on_more_than_one_day,
                capture(slot)
            )
        } answers { "Most recently on ${slot.captured}" }

        val risk = defaultRisk.copy(daysWithEncounters = 2)
        risk.getRiskContactLast(context) shouldBe "Most recently on ${slot.captured}"
    }

    private val defaultRisk = IncreasedRisk(
        riskState = RiskState.INCREASED_RISK,
        isInDetailsMode = false,
        lastExposureDetectionTime = Instant.now(),
        allowManualUpdate = false,
        daysWithEncounters = 1,
        activeTracingDays = 1,
        lastEncounterAt = Instant.now()
    )
}
