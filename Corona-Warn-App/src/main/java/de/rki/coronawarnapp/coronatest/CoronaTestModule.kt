package de.rki.coronawarnapp.coronatest

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.coronatest.server.VerificationModule
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.pcr.PCRTestProcessor
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RATestProcessor
import de.rki.coronawarnapp.profile.ProfileModule
import de.rki.coronawarnapp.util.reset.Resettable

@Module(
    includes = [CoronaTestModule.ResetModule::class, VerificationModule::class, ProfileModule::class]
)
interface CoronaTestModule {

    @Binds
    @IntoSet
    fun pcrProcessor(
        processor: PCRTestProcessor
    ): PersonalCoronaTestProcessor

    @Binds
    @IntoSet
    fun ratProcessor(
        processor: RATestProcessor
    ): PersonalCoronaTestProcessor

    @Module
    interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableCoronaTestRepository(resettable: CoronaTestRepository): Resettable
    }
}
