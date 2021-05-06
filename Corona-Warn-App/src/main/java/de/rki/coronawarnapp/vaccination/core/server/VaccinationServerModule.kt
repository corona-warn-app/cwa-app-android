package de.rki.coronawarnapp.vaccination.core.server

import dagger.Module
import de.rki.coronawarnapp.vaccination.core.server.proof.VaccinationProofModule
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSetModule

@Module(
    includes = [
        VaccinationValueSetModule::class,
        VaccinationProofModule::class
    ]
)
abstract class VaccinationServerModule
