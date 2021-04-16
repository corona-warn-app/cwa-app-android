package de.rki.coronawarnapp.coronatest.type.rapidantigen

fun RACoronaTest?.toSubmissionState(): SubmissionStateRAT {
    if (this == null) return SubmissionStateRAT.NoTest

    throw NotImplementedError()
}
