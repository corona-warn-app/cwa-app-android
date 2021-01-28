package de.rki.coronawarnapp.contactdiary.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import javax.inject.Inject

/**
 * This activity holds all the contact diary fragments
 */
class ContactDiaryActivity : AppCompatActivity() {

    @Inject lateinit var settings: ContactDiarySettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*if (settings.onboardingStatus == ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12) {
            graph.startDestination = R.id.contactDiaryOverviewFragment
        } else {
            graph.startDestination = R.id.contactDiaryOnboardingFragment
        }*/
    }
}
