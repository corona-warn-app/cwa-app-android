package de.rki.coronawarnapp.coronatest

import android.os.Parcelable
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import org.joda.time.LocalDate

interface TestRegistrationRequest : Parcelable {
    val type: CoronaTest.Type
    val identifier: String
    val isDccSupportedByPoc: Boolean
    val isDccConsentGiven: Boolean
    val dateOfBirth: LocalDate?
}

val TestRegistrationRequest.isFamilyTest: Boolean
    get() = this is CoronaTestQRCode && this.categoryType == CoronaTestQRCode.CategoryType.FAMILY
