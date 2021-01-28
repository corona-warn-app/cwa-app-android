package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.datadonation.storage.DataDonationPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveySettings @Inject constructor(
    private val dataDonationPreferences: DataDonationPreferences
)
