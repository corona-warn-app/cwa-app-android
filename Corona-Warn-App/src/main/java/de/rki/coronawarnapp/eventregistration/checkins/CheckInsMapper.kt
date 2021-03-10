package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.server.protocols.internal.evreg.CheckInOuterClass

interface CheckInsMapper {
    fun map(checkIns: List<CheckIn>): List<CheckInOuterClass.CheckIn>
}
