package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import de.rki.coronawarnapp.ccl.ui.text.urlResource
import de.rki.coronawarnapp.ccl.ui.text.textResource

data class DccWalletInfoWrapper(
    val dccWalletInfo: DccWalletInfo
) {
    val admissionState = dccWalletInfo.admissionState
    val admissionBadgeText = ""// by textResource(admissionState.badgeText)
    val admissionTitleText = ""// textResource(admissionState.titleText)
    val admissionSubtitleText = ""// textResource(admissionState.subtitleText)
    val admissionLongText = ""// textResource(admissionState.longText)
    val admissionFaqAnchor = ""// urlResource(admissionState.faqAnchor)
}
