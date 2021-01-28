package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Surveys @Inject constructor(
    private val deviceAttestation: DeviceAttestation,
    private val settings: SurveySettings
) {

    val availableSurveys: Flow<Collection<Type>> = emptyFlow()

    suspend fun requestDetails(type: Type): Survey {
        TODO()
    }

    suspend fun resetSurvey(type: Type) {
        TODO()
    }

    enum class Type {
        HIGH_RISK_ENCOUNTER
    }

    data class Survey(
        val type: Type,
        val otp: String,
        val surveyLink: URL
    )
}
