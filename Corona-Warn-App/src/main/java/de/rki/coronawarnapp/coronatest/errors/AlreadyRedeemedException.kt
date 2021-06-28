package de.rki.coronawarnapp.coronatest.errors

import de.rki.coronawarnapp.coronatest.type.CoronaTest

class AlreadyRedeemedException(
    coronaTest: CoronaTest
) : IllegalArgumentException("Test was already redeemed ${coronaTest.identifier}")
