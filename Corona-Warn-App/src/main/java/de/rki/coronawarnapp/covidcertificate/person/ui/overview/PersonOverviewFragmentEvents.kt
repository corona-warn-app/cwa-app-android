package de.rki.coronawarnapp.covidcertificate.person.ui.overview

sealed class PersonOverviewFragmentEvents

data class ShowRefreshErrorDialog(val error: Throwable?) : PersonOverviewFragmentEvents()
data class OpenPersonDetailsFragment(val personIdentifier: String) : PersonOverviewFragmentEvents()
object ScanQrCode : PersonOverviewFragmentEvents()
