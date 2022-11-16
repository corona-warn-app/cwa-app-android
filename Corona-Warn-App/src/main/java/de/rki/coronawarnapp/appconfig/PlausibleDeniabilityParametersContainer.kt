package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParametersOrBuilder

data class PlausibleDeniabilityParametersContainer(
    val checkInSizesInBytes: List<Int> = emptyList(),
    val probabilityToFakeCheckInsIfNoCheckIns: Double = 0.0,
    val probabilityToFakeCheckInsIfSomeCheckIns: Double = 0.0,
    val numberOfFakeCheckInsFunctionParameters: List<NumberOfFakeCheckInsFunctionParametersOrBuilder> = emptyList()
)
