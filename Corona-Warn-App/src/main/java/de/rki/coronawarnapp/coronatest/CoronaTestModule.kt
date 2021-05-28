package de.rki.coronawarnapp.coronatest

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.coronatest.server.VerificationModule
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.pcr.PCRProcessor
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RAProcessor

@Module(
    includes = [VerificationModule::class]
)
abstract class CoronaTestModule {

    @Binds
    @IntoSet
    abstract fun pcrProcessor(
        processor: PCRProcessor
    ): CoronaTestProcessor

    @Binds
    @IntoSet
    abstract fun ratProcessor(
        processor: RAProcessor
    ): CoronaTestProcessor
}
