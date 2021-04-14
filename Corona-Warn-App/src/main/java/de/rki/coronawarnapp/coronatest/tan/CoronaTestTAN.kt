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
        override val type: CoronaTest.Type,
        override val tan: TestTAN,
    ) : CoronaTestTAN()

    @Parcelize
    data class RapidAntigen(
        override val type: CoronaTest.Type,
        override val tan: TestTAN,
    ) : CoronaTestTAN()
}

typealias TestTAN = String
