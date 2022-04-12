package de.rki.coronawarnapp.profile.storage

import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.model.ProfileId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.joda.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor() {
    val profilesFlow: Flow<Set<Profile>> = flowOf(setOf(dummy1, dummy2))

    fun deleteProfile(id: ProfileId) {
        // to do
    }

    fun upsertProfile(profile: Profile) {
        // to do
    }
}

val dummy1 = Profile(
    id = 1,
    firstName = "First name",
    lastName = "Last name",
    birthDate = LocalDate(1981, 3, 20),
    street = "Main street",
    zipCode = "12132",
    city = "London",
    phone = "111111111",
    email = "email@example.com"
)

val dummy2 = Profile(
    id = 1,
    firstName = "Jimmy",
    lastName = "Fallon",
    birthDate = null,
    street = "",
    zipCode = "",
    city = "New York",
    phone = "",
    email = ""
)
