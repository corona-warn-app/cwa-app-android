package de.rki.coronawarnapp.ui.riskdetails

import de.rki.coronawarnapp.risk.RiskState.CALCULATION_FAILED
import de.rki.coronawarnapp.risk.RiskState.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskState.LOW_RISK
import de.rki.coronawarnapp.ui.tracing.details.DefaultRiskDetailPresenter
import io.kotest.matchers.shouldBe
import org.junit.Test

class DefaultRiskDetailPresenterTest {

    @Test
    fun test_isAdditionalInfoVisible() {
        DefaultRiskDetailPresenter().apply {
            isAdditionalInfoVisible(LOW_RISK, 0) shouldBe false
            isAdditionalInfoVisible(LOW_RISK, 1) shouldBe true
            isAdditionalInfoVisible(INCREASED_RISK, 0) shouldBe false
            isAdditionalInfoVisible(CALCULATION_FAILED, 0) shouldBe false
        }
    }

    @Test
    fun test_isInformationBodyNoticeVisible() {
        DefaultRiskDetailPresenter().apply {
            isInformationBodyNoticeVisible(LOW_RISK) shouldBe false
            isInformationBodyNoticeVisible(INCREASED_RISK) shouldBe true
            isInformationBodyNoticeVisible(CALCULATION_FAILED) shouldBe true
        }
    }
}
