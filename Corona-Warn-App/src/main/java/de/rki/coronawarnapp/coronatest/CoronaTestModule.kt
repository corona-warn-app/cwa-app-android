package de.rki.coronawarnapp.coronatest

import dagger.Module
import de.rki.coronawarnapp.coronatest.server.VerificationModule

@Module(
    includes = [VerificationModule::class]
)
class CoronaTestModule
