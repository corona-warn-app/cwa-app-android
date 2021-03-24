package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.lists.HasStableId

interface CheckInsItem : HasStableId {
    val checkin: CheckIn
}
