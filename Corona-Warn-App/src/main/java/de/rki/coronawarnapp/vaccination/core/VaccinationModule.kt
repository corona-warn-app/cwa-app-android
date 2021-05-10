package de.rki.coronawarnapp.vaccination.core

import dagger.Module
import de.rki.coronawarnapp.vaccination.core.server.VaccinationServerModule

@Module(
    includes = [
        VaccinationServerModule::class
    ]
)
abstract class VaccinationModule
