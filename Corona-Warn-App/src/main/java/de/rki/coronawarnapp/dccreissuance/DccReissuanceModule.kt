package de.rki.coronawarnapp.dccreissuance

import dagger.Module
import de.rki.coronawarnapp.dccreissuance.core.server.DccReissuanceServerModule

@Module(includes = [DccReissuanceServerModule::class])
class DccReissuanceModule
