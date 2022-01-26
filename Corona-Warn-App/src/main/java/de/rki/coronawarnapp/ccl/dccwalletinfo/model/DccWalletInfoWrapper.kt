package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import de.rki.coronawarnapp.ccl.dccwalletinfo.text.textResource
import de.rki.coronawarnapp.ccl.dccwalletinfo.text.urlResource
import de.rki.coronawarnapp.util.HashExtensions.toSHA256

data class DccWalletInfoWrapper(
    val dccWalletInfo: DccWalletInfo = dummyDccWalletInfo
) {
    val admissionState = dccWalletInfo.admissionState
    val vaccinationState = dccWalletInfo.vaccinationState
    val mostRelevantCertificateHash = dccWalletInfo.mostRelevantCertificate.certificateRef.barcodeData.toSHA256()

    val admissionBadgeText by textResource(admissionState.badgeText)
    val admissionTitleText by textResource(admissionState.titleText)
    val admissionSubtitleText by textResource(admissionState.subtitleText)
    val admissionLongText by textResource(admissionState.longText)
    val admissionFaqAnchor by urlResource(admissionState.faqAnchor)

    val isVaccinationStateVisible = vaccinationState.visible
    val vaccinationTitleText by textResource(vaccinationState.titleText)
    val vaccinationSubtitleText by textResource(vaccinationState.subtitleText)
    val vaccinationLongText by textResource(vaccinationState.longText)
    val vaccinationFaqAnchor by urlResource(vaccinationState.faqAnchor)
}
