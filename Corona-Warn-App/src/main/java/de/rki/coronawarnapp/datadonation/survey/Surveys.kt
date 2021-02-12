package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.joda.time.Seconds
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Surveys @Inject constructor(
    private val deviceAttestation: DeviceAttestation,
    private val settings: SurveySettings,
    private val appConfigProvider: AppConfigProvider,
    dispatcherProvider: DispatcherProvider,
    private val urlProvider: SurveyUrlProvider
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

        // TODO adjust for server com
        // Just to have a glimpse at the loading spinner
        delay(Seconds.THREE.toStandardDuration().millis)

        // TODO: generate and authenticate real otp
        val otp = UUID.randomUUID()

        return Survey(
            type = Type.HIGH_RISK_ENCOUNTER,
            surveyLink = urlProvider.provideUrl(type, otp)
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
