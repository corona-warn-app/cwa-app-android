package de.rki.coronawarnapp.ui.riskdetails

import de.rki.coronawarnapp.risk.RiskLevelConstants

class DefaultRiskDetailPresenter : RiskDetailPresenter {

    override fun isAdditionalInfoVisible(riskLevel: Int, matchedKeyCount: Int) =
        riskLevel == RiskLevelConstants.LOW_LEVEL_RISK && matchedKeyCount > 0

    override fun isInformationBodyNoticeVisible(riskLevel: Int, matchedKeyCount: Int) =
        !(riskLevel == RiskLevelConstants.LOW_LEVEL_RISK && matchedKeyCount > 0)
}
