package de.rki.coronawarnapp.coronatest.errors

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest

class AlreadyRedeemedException(
    coronaTest: BaseCoronaTest
) : IllegalArgumentException("Test was already redeemed ${coronaTest.identifier}")
