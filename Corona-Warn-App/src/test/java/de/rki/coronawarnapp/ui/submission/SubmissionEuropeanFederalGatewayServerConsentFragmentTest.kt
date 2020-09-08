package de.rki.coronawarnapp.ui.submission

import de.rki.coronawarnapp.ui.viewmodel.EuropeanFederalGatewayServerConsentViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import org.junit.Assert.*
import org.junit.Test

class SubmissionEuropeanFederalGatewayServerConsentFragmentTest {
    private val submissionViewModel = SubmissionViewModel()
    private val europeanFederealGatewayServerViewModel = EuropeanFederalGatewayServerConsentViewModel()

    @Test
    fun testNavigation() {
        europeanFederealGatewayServerViewModel.isEuropeanConsentGranted.postValue(false)

    }
}