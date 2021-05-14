package de.rki.coronawarnapp.vaccination.core.server

import dagger.Module
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSetModule

@Module(
    includes = [
        VaccinationValueSetModule::class
    ]
)
abstract class VaccinationServerModule
