package de.rki.coronawarnapp.datadonation.survey

import dagger.Module
import de.rki.coronawarnapp.datadonation.survey.consent.SurveyConsentModule

@Module(
    includes = [SurveyConsentModule::class]
)
class SurveyModule
