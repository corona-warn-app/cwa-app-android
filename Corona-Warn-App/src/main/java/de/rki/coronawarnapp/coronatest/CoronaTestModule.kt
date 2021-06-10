package de.rki.coronawarnapp.coronatest

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.coronatest.server.VerificationModule
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.pcr.PCRTestProcessor
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RATestProcessor

@Module(
    includes = [VerificationModule::class]
)
abstract class CoronaTestModule {

    @Binds
    @IntoSet
    abstract fun pcrProcessor(
        processor: PCRTestProcessor
    ): CoronaTestProcessor

    @Binds
    @IntoSet
    abstract fun ratProcessor(
        processor: RATestProcessor
    ): CoronaTestProcessor
}
