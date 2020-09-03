package de.rki.coronawarnapp.ui.riskdetails

import de.rki.coronawarnapp.risk.RiskLevelConstants
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
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.LOW_LEVEL_RISK, 0))
            Assert.assertFalse(isInformationBodyNoticeVisible(RiskLevelConstants.LOW_LEVEL_RISK, 1))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.UNKNOWN_RISK_INITIAL, 0))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.INCREASED_RISK, 0))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS, 0))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF, 0))
            Assert.assertTrue(isInformationBodyNoticeVisible(RiskLevelConstants.UNDETERMINED, 0))
        }
    }
}
