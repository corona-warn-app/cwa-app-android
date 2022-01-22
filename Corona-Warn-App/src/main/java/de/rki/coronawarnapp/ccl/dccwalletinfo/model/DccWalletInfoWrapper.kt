package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import de.rki.coronawarnapp.ccl.dccwalletinfo.text.textResource

data class DccWalletInfoWrapper(
    val dccWalletInfo: DccWalletInfo = dummyDccWalletInfo
) {
    val admissionState = dccWalletInfo.admissionState
    val admissionBadgeText by textResource(admissionState.badgeText)
    val admissionTitleText by textResource(admissionState.titleText)
    val admissionSubtitleText by textResource(admissionState.subtitleText)
    val admissionLongText by textResource(admissionState.longText)
}
