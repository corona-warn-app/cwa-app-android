package de.rki.coronawarnapp.coronatest.tan

import android.os.Parcelable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

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

        @IgnoredOnParcel override val type: CoronaTest.Type = CoronaTest.Type.PCR
    }

    @Parcelize
    data class RapidAntigen(
        override val tan: TestTAN,
    ) : CoronaTestTAN() {

        @IgnoredOnParcel override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN
    }
}

typealias TestTAN = String
