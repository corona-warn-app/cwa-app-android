package de.rki.coronawarnapp.appconfig

interface SafetyNetRequirements {
    val requireBasicIntegrity: Boolean
    val requireCTSProfileMatch: Boolean
    val requireEvaluationTypeBasic: Boolean
    val requireEvaluationTypeHardwareBacked: Boolean
}

data class SafetyNetRequirementsContainer(
    override val requireBasicIntegrity: Boolean = false,
    override val requireCTSProfileMatch: Boolean = false,
    override val requireEvaluationTypeBasic: Boolean = false,
    override val requireEvaluationTypeHardwareBacked: Boolean = false
) : SafetyNetRequirements
