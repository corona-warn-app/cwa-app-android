package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import de.rki.coronawarnapp.ccl.dccwalletinfo.text.textResource
import de.rki.coronawarnapp.ccl.dccwalletinfo.text.urlResource

data class DccWalletInfoWrapper(
    val dccWalletInfo: DccWalletInfo = dummyDccWalletInfo
) {
    val admissionState = dccWalletInfo.admissionState
    val admissionBadgeText = ""// by textResource(admissionState.badgeText)
    val admissionTitleText = ""// textResource(admissionState.titleText)
    val admissionSubtitleText = ""// textResource(admissionState.subtitleText)
    val admissionLongText = ""// textResource(admissionState.longText)
    val admissionFaqAnchor = ""// urlResource(admissionState.faqAnchor)
}
