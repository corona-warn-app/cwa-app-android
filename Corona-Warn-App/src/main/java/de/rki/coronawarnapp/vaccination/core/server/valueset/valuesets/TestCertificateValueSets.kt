package de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets

interface TestCertificateValueSets : ValueSets {
    // Type of Test
    val tt: ValueSets.ValueSet

    // RAT Test name and manufacturer
    val ma: ValueSets.ValueSet

    // Test Result
    val tr: ValueSets.ValueSet
}
