package de.rki.coronawarnapp.coronatest.type.antigen

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken

data class RapidAntigenCoronaTest(
    override val testGUID: CoronaTestGUID,
    override val registrationToken: RegistrationToken,
    override val isRefreshing: Boolean,
    override val isSubmissionAllowed: Boolean,
    override val isSubmitted: Boolean,
    val state: State,
) : CoronaTest {

    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    override fun toSubmittedState(): CoronaTest {
        TODO("Not yet implemented")
    }

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
        OUTDATED,
    }
}
