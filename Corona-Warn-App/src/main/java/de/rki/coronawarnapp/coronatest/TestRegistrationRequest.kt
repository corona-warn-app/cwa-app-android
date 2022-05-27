package de.rki.coronawarnapp.coronatest

import android.os.Parcelable
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import java.time.LocalDate

interface TestRegistrationRequest : Parcelable {
    val type: BaseCoronaTest.Type
    val identifier: String
    val isDccSupportedByPoc: Boolean
    val isDccConsentGiven: Boolean
    val dateOfBirth: LocalDate?
}
