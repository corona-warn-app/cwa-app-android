package de.rki.coronawarnapp.ui.submission.covidcertificate

sealed class RequestDccNavEvent
object ToDispatcherScreen : RequestDccNavEvent()
object ToHomeScreen : RequestDccNavEvent()
object Back : RequestDccNavEvent()
