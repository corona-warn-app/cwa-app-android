package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateServerException

sealed class PersonOverviewFragmentEvents

data class ShowRefreshErrorDialog(val error: Throwable) : PersonOverviewFragmentEvents() {
    val isLabError
        get() = error is TestCertificateServerException &&
            error.errorCode == TestCertificateServerException.ErrorCode.DCC_NOT_SUPPORTED_BY_LAB
}

data class ShowDeleteDialog(val certificateId: String) : PersonOverviewFragmentEvents()
data class OpenPersonDetailsFragment(val personIdentifier: String, val position: Int) : PersonOverviewFragmentEvents()
object ScanQrCode : PersonOverviewFragmentEvents()
object OpenAppDeviceSettings : PersonOverviewFragmentEvents()
