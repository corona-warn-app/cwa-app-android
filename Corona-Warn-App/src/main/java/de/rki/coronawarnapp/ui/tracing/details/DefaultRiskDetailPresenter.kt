package de.rki.coronawarnapp.ui.tracing.details

import dagger.Reusable
import de.rki.coronawarnapp.risk.RiskState
import javax.inject.Inject

@Reusable
class DefaultRiskDetailPresenter @Inject constructor() {

    fun isAdditionalInfoVisible(riskState: RiskState, matchedKeyCount: Int) =
        riskState == RiskState.LOW_RISK && matchedKeyCount > 0

    fun isInformationBodyNoticeVisible(riskState: RiskState) = riskState != RiskState.LOW_RISK
}
