package de.rki.coronawarnapp.reyclebin.coronatest

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import org.joda.time.Instant

data class RecycledCoronaTest(
    override val recycledAt: Instant,
    val coronaTest: CoronaTest,
) : Recyclable
