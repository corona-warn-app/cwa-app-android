package de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets

interface VaccinationValueSets : ValueSets {

    // Vaccine or prophylaxis
    val vp: ValueSets.ValueSet

    // Vaccine medicinal product
    val mp: ValueSets.ValueSet

    // Marketing Authorization Holder
    val ma: ValueSets.ValueSet
}
