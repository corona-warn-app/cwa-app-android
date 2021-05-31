package de.rki.coronawarnapp.coronatest.tan

import android.os.Parcelable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.joda.time.LocalDate

sealed class CoronaTestTAN : Parcelable, TestRegistrationRequest {

    abstract override val type: CoronaTest.Type
    abstract val tan: TestTAN

    @IgnoredOnParcel
    override val identifier: String
        get() = "tan-${type.raw}-$tan"

    @Parcelize
    data class PCR(
        override val tan: TestTAN,
    ) : CoronaTestTAN() {

        @IgnoredOnParcel
        override val type: CoronaTest.Type = CoronaTest.Type.PCR

        @IgnoredOnParcel
        override val isDccSupportedbyPoc: Boolean = false

        @IgnoredOnParcel
        override val isDccConsentGiven: Boolean = false

        @IgnoredOnParcel
        override val dateOfBirth: LocalDate? = null
    }
}

typealias TestTAN = String
