package de.rki.coronawarnapp.ui.riskdetails

import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.ui.tracing.details.DefaultRiskDetailPresenter
import org.junit.Assert
import org.junit.Test

class DefaultRiskDetailPresenterTest {

    @Test
    fun test_isAdditionalInfoVisible() {
        DefaultRiskDetailPresenter().apply {
            Assert.assertFalse(isAdditionalInfoVisible(RiskLevelConstants.LOW_LEVEL_RISK, 0))
            Assert.assertTrue(isAdditionalInfoVisible(RiskLevelConstants.LOW_LEVEL_RISK, 1))
            Assert.assertFalse(isAdditionalInfoVisible(RiskLevelConstants.UNKNOWN_RISK_INITIAL, 0))
            Assert.assertFalse(isAdditionalInfoVisible(RiskLevelConstants.INCREASED_RISK, 0))
            Assert.assertFalse(isAdditionalInfoVisible(RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS, 0))
            Assert.assertFalse(isAdditionalInfoVisible(RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF, 0))
            Assert.assertFalse(isAdditionalInfoVisible(RiskLevelConstants.UNDETERMINED, 0))
        }
    }

    @Test
    fun test_isInformationBodyNoticeVisible() {
        DefaultRiskDetailPresenter().apply {
            Assert.assertFalse(isInformationBodyNoticeVisible(RiskLevelConstants.LOW_LEVEL_RISK))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.UNKNOWN_RISK_INITIAL))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.INCREASED_RISK))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.UNDETERMINED))
        }
    }
}
