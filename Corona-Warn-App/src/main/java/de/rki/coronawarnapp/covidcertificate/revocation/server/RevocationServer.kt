package de.rki.coronawarnapp.covidcertificate.revocation.server

import dagger.Lazy
import javax.inject.Inject

// TODO("Implement)
class RevocationServer @Inject constructor(
    private val revocationApiLazy: Lazy<RevocationApi>
) {

    private val revocationApi: RevocationApi get() = revocationApiLazy.get()
}
