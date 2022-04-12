package de.rki.coronawarnapp.profile.storage

import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val dao: ProfileDao,
    @AppScope private val scope: CoroutineScope,
) {
    val profilesFlow: Flow<Set<Profile>> = dao.getAll().mapLatest { list ->
        list.map { it.fromEntity() }.toSet()
    }

    fun deleteProfile(id: Int) = scope.launch {
        dao.delete(id)
    }

    fun upsertProfile(profile: Profile) = scope.launch {
        val entity = profile.toEntity()
        if (profile.id == null)
            dao.insert(entity)
        else
            dao.update(entity)
    }

    fun clear() = scope.launch {
        dao.deleteAll()
    }
}

internal fun Profile.toEntity() = ProfileEntity(
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

internal fun ProfileEntity.fromEntity() = Profile(
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
