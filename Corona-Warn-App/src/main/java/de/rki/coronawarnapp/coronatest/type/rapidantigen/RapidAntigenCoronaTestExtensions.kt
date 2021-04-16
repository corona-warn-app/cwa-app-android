package de.rki.coronawarnapp.coronatest.type.rapidantigen

fun RapidAntigenCoronaTest?.toSubmissionState(): SubmissionStateRAT {
    if (this == null) return SubmissionStateRAT.NoTest

    throw NotImplementedError()
}
