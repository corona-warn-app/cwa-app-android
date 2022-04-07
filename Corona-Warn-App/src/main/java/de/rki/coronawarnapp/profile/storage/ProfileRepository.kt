package de.rki.coronawarnapp.profile.storage

import de.rki.coronawarnapp.profile.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val dao: ProfileDao,
) {
    val profilesFlow: Flow<Set<Profile>> = dao.getAll().mapLatest {
        it.map { it.fromEntity() }.toSet()
    }

    fun deleteProfile(id: Int) {
        dao.delete(id)
    }

    fun upsertProfile(profile: Profile) {
        val entity = profile.toEntity()
        if (profile.id == null)
            dao.insert(entity)
        else
            dao.update(entity)
    }
}

fun Profile.toEntity() = ProfileEntity(
    id = id ?: 0,
    firstName = firstName,
    lastName = lastName,
    birthDate = birthDate,
    street = street,
    zipCode = zipCode,
    city = city,
    phone = phone,
    email = email
)

fun ProfileEntity.fromEntity() = Profile(
    id = id,
    firstName = firstName,
    lastName = lastName,
    birthDate = birthDate,
    street = street,
    zipCode = zipCode,
    city = city,
    phone = phone,
    email = email
)
