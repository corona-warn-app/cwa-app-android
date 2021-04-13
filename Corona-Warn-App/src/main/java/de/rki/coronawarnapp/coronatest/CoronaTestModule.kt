package de.rki.coronawarnapp.coronatest

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.VerificationModule
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.antigen.RapidAntigenCoronaTest
import de.rki.coronawarnapp.coronatest.type.antigen.RapidAntigenProcessor
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRProcessor

@Module(
    includes = [VerificationModule::class]
)
abstract class CoronaTestModule {

    @Binds
    @IntoSet
    abstract fun pcrProcessor(
        processor: PCRProcessor
    ): CoronaTestProcessor<CoronaTestQRCode.PCR, PCRCoronaTest>

    @Binds
    @IntoSet
    abstract fun ratProcessor(
        processor: RapidAntigenProcessor
    ): CoronaTestProcessor<CoronaTestQRCode.RapidAntigen, RapidAntigenCoronaTest>
}
