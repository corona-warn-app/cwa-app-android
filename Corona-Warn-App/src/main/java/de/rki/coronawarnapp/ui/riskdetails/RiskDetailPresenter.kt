package de.rki.coronawarnapp.ui.riskdetails

interface RiskDetailPresenter {

    fun isAdditionalInfoVisible(riskLevel: Int, matchedKeyCount: Int): Boolean

    fun isInformationBodyNoticeVisible(riskLevel: Int, matchedKeyCount: Int): Boolean
}
