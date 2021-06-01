package de.rki.coronawarnapp.ui.submission.greencertificate

sealed class RequestDccNavEvent
object ToDispatcherScreen : RequestDccNavEvent()
object ToHomeScreen : RequestDccNavEvent()
object Back : RequestDccNavEvent()
