package de.rki.coronawarnapp.bugreporting.censors.profile

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidAddress
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidCity
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidPhoneNumber
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidZipCode
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

@Reusable
class ProfileCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    profileRepository: ProfileRepository
) : BugCensor {

    private val mutex = Mutex()
    private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    private val names = mutableSetOf<String>()
    private val dates = mutableSetOf<LocalDate>()
    private val emails = mutableSetOf<String>()
    private val cities = mutableSetOf<String>()
    private val phoneNumbers = mutableSetOf<String>()
    private val zipCodes = mutableSetOf<String>()
    private val streets = mutableSetOf<String>()

    init {
        profileRepository.profilesFlow
            .filterNotNull()
            .onEach { profiles ->
                mutex.withLock {
                    profiles.forEach { profile ->
                        names.add(profile.firstName)
                        names.add(profile.lastName)
                        profile.birthDate?.let { dates.add(it) }
                        emails.add(profile.email)
                        cities.add(profile.city)
                        phoneNumbers.add(profile.phone)
                        zipCodes.add(profile.zipCode)
                        streets.add(profile.street)
                    }
                }
            }.launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensorContainer? = mutex.withLock {
        var container = CensorContainer(message)

        names.forEach {
            withValidName(it) { firstName ->
                container = container.censor(firstName, "#name")
            }
        }

        dates.forEach {
            it.toString(dateFormatter)?.let { dateString ->
                container = container.censor(dateString, "#date")
            }
        }

        streets.forEach {
            withValidAddress(it) { street ->
                container = container.censor(street, "#street")
            }
        }

        cities.forEach {
            withValidCity(it) { city ->
                container = container.censor(city, "#city")
            }
        }

        zipCodes.forEach {
            withValidZipCode(it) { zipCode ->
                container = container.censor(zipCode, "#zipCode")
            }
        }

        phoneNumbers.forEach {
            withValidPhoneNumber(it) { phone ->
                container = container.censor(phone, "#phone")
            }
        }

        emails.forEach {
            withValidPhoneNumber(it) { phone ->
                container = container.censor(phone, "#email")
            }
        }

        return container.nullIfEmpty()
    }
}
