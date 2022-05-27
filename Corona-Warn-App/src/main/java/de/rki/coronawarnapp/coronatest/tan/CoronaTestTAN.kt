package de.rki.coronawarnapp.coronatest.tan

import android.os.Parcelable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

sealed class CoronaTestTAN : Parcelable, TestRegistrationRequest {

    abstract override val type: BaseCoronaTest.Type
    abstract val tan: TestTAN

    @IgnoredOnParcel
    override val identifier: String
        get() = "tan-${type.raw}-$tan"

    @Parcelize
    data class PCR(
        override val tan: TestTAN,
    ) : CoronaTestTAN() {

        @IgnoredOnParcel
        override val type: BaseCoronaTest.Type = BaseCoronaTest.Type.PCR

        @IgnoredOnParcel
        override val isDccSupportedByPoc: Boolean = false

        @IgnoredOnParcel
        override val isDccConsentGiven: Boolean = false

        @IgnoredOnParcel
        override val dateOfBirth: LocalDate? = null
    }
}

typealias TestTAN = String
