package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Surveys @Inject constructor(
    private val deviceAttestation: DeviceAttestation,
    private val settings: SurveySettings,
    private val appConfigProvider: AppConfigProvider
) {

    val availableSurveys: Flow<Collection<Type>> = appConfigProvider.currentConfig.map {
        mutableListOf<Type>().apply {
            if (it.survey.surveyOnHighRiskEnabled) {
                add(Type.HIGH_RISK_ENCOUNTER)
            }
        }
    }

    suspend fun requestDetails(type: Type): Survey {
        // TODO
        return Survey(
            type = Type.HIGH_RISK_ENCOUNTER,
            surveyLink = "Link to high risk encounter survey..."
        )
    }

    suspend fun resetSurvey(type: Type) {
        // TODO
    }

    enum class Type {
        HIGH_RISK_ENCOUNTER
    }

    data class Survey(
        val type: Type,
        val surveyLink: String
    )
}
