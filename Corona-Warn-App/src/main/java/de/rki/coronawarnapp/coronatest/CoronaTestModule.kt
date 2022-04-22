package de.rki.coronawarnapp.coronatest

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.coronatest.antigen.profile.RatProfileModule
import de.rki.coronawarnapp.coronatest.server.VerificationModule
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.pcr.PCRTestProcessor
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RATestProcessor

@Module(
    includes = [VerificationModule::class, RatProfileModule::class]
)
abstract class CoronaTestModule {

    @Binds
    @IntoSet
    abstract fun pcrProcessor(
        processor: PCRTestProcessor
    ): PersonalCoronaTestProcessor

    @Binds
    @IntoSet
    abstract fun ratProcessor(
        processor: RATestProcessor
    ): PersonalCoronaTestProcessor
}
