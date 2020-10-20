package de.rki.coronawarnapp.ui.tracing.details

import dagger.Reusable
import de.rki.coronawarnapp.risk.RiskLevelConstants
import javax.inject.Inject

@Reusable
class DefaultRiskDetailPresenter @Inject constructor() {

    fun isAdditionalInfoVisible(riskLevel: Int, matchedKeyCount: Int) =
        riskLevel == RiskLevelConstants.LOW_LEVEL_RISK && matchedKeyCount > 0

    fun isInformationBodyNoticeVisible(riskLevel: Int, matchedKeyCount: Int) =
        !(riskLevel == RiskLevelConstants.LOW_LEVEL_RISK && matchedKeyCount > 0)
}
