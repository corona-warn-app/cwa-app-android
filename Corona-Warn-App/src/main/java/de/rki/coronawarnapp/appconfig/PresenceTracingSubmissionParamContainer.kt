package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.v2
    .PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.DurationFilter

import de.rki.coronawarnapp.server.protocols.internal.v2
    .PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.AerosoleDecayFunctionLinear

data class PresenceTracingSubmissionParamContainer(
    val durationFilters: List<DurationFilter>,
    val aerosoleDecayLinearFunctions: List<AerosoleDecayFunctionLinear>
)
