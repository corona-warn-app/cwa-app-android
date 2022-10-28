package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragment
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.util.ui.toResolvingString

fun CertificateContainerId.toDccDetails(): DccResult = DccResult.Details(uri())
fun CertificateContainerId.toMaxPersonsWarning(max: Int): DccResult = DccResult.MaxPersonsWarning(uri(), max)

private fun CertificateContainerId.uri(): Uri = when (this) {
    is RecoveryCertificateContainerId -> RecoveryCertificateDetailsFragment.uri(qrCodeHash)
    is TestCertificateContainerId -> TestCertificateDetailsFragment.uri(qrCodeHash)
    is VaccinationCertificateContainerId -> VaccinationDetailsFragment.uri(qrCodeHash)
}

fun CheckInQrCodeHandler.Result.toCheckInResult(requireOnboarding: Boolean): CheckInResult = when (this) {
    is CheckInQrCodeHandler.Result.Invalid -> CheckInResult.Error(errorTextRes.toResolvingString())
    is CheckInQrCodeHandler.Result.Valid -> CheckInResult.Details(verifiedTraceLocation, requireOnboarding)
}
