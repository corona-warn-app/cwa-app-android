package de.rki.coronawarnapp.coronatest.type.pcr

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken

data class PCRCoronaTest(
    override val testGUID: CoronaTestGUID,
    override val registrationToken: RegistrationToken,
    override val isRefreshing: Boolean,
    override val isSubmissionAllowed: Boolean,
    override val isSubmitted: Boolean,
    val state: State,
) : CoronaTest {

    override val type: CoronaTest.Type = CoronaTest.Type.PCR

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
    }
}
