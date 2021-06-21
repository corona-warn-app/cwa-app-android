package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId

sealed class PersonOverviewFragmentEvents

data class ShowRefreshErrorDialog(val error: Throwable) : PersonOverviewFragmentEvents()
data class ShowDeleteDialog(val containerId: TestCertificateContainerId) : PersonOverviewFragmentEvents()
data class OpenPersonDetailsFragment(val personIdentifier: String) : PersonOverviewFragmentEvents()
object ScanQrCode : PersonOverviewFragmentEvents()
object OpenAppDeviceSettings : PersonOverviewFragmentEvents()
