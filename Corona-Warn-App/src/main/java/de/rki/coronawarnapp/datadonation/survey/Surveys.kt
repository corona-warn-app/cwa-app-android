package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import kotlinx.coroutines.delay
import org.joda.time.Seconds
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Surveys @Inject constructor(
    private val deviceAttestation: DeviceAttestation,
    private val settings: SurveySettings,
    private val appConfigProvider: AppConfigProvider,
    dispatcherProvider: DispatcherProvider
) {

    val availableSurveys: Flow<Collection<Type>> by lazy {
        appConfigProvider.currentConfig
            .flowOn(dispatcherProvider.Default)
            .map {
                mutableListOf<Type>().apply {
                    if (it.survey.surveyOnHighRiskEnabled) {
                        add(Type.HIGH_RISK_ENCOUNTER)
                    }
                }
            }
    }

    suspend fun requestDetails(type: Type): Survey {
        // TODO
        // Just to have a glimpse at the loading spinner
        delay(Seconds.THREE.toStandardDuration().millis)
        return Survey(
            type = Type.HIGH_RISK_ENCOUNTER,
            surveyLink = "https://www.example.com/",
            queryParam = "whatever"
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
        val surveyLink: String,
        val queryParam: String
    )
}
