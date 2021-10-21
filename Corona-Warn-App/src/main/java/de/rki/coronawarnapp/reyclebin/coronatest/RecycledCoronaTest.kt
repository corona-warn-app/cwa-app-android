package de.rki.coronawarnapp.reyclebin.coronatest

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import org.joda.time.Instant

data class RecycledCoronaTest(
    val recycledAt: Instant,
    val coronaTest: CoronaTest,
)
