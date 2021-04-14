package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RapidAntigenCoronaTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val CoronaTestRepository.latestPCRT: Flow<PCRCoronaTest?>
    get() = this.coronaTests
        .map { allTests ->
            allTests.singleOrNull {
                it.type == CoronaTest.Type.PCR
            } as? PCRCoronaTest
        }
        .distinctUntilChanged()

val CoronaTestRepository.latestRAT: Flow<RapidAntigenCoronaTest?>
    get() = this.coronaTests
        .map { allTests ->
            allTests.singleOrNull {
                it.type == CoronaTest.Type.RAPID_ANTIGEN
            } as? RapidAntigenCoronaTest
        }
        .distinctUntilChanged()
