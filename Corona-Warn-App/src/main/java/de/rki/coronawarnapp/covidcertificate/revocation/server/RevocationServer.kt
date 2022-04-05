package de.rki.coronawarnapp.covidcertificate.revocation.server

import dagger.Lazy
import javax.inject.Inject

// To Do: Implement
class RevocationServer @Inject constructor(
    private val revocationApiLazy: Lazy<RevocationApi>
) {

    private val revocationApi: RevocationApi get() = revocationApiLazy.get()
}
