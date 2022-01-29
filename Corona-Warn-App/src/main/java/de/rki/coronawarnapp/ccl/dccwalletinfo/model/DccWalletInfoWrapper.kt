package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import de.rki.coronawarnapp.ccl.ui.text.urlResource
import de.rki.coronawarnapp.ccl.ui.text.textResource

data class DccWalletInfoWrapper(
    val dccWalletInfo: DccWalletInfo
) {
    val admissionState = dccWalletInfo.admissionState
    val admissionBadgeText by textResource(admissionState.badgeText)
    val admissionTitleText by textResource(admissionState.titleText)
    val admissionSubtitleText by textResource(admissionState.subtitleText)
    val admissionLongText by textResource(admissionState.longText)
    val admissionFaqAnchor by urlResource(admissionState.faqAnchor)
}
