package de.rki.coronawarnapp.bugreporting.censors.profile

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidAddress
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidCity
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidEmail
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidPhoneNumber
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidZipCode
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Reusable
class ProfileCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    profileRepository: ProfileRepository
) : BugCensor {

    private val mutex = Mutex()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val names = mutableSetOf<String>()
    private val dates = mutableSetOf<String>()
    private val emails = mutableSetOf<String>()
    private val cities = mutableSetOf<String>()
    private val phoneNumbers = mutableSetOf<String>()
    private val zipCodes = mutableSetOf<String>()
    private val streets = mutableSetOf<String>()

    init {
        profileRepository.profilesFlow
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { profiles ->
                mutex.withLock {
                    profiles.forEach { profile ->
                        withValidName(profile.firstName) {
                            names.add(it)
                        }
                        withValidName(profile.lastName) {
                            names.add(it)
                        }
                        profile.birthDate?.let { dates.add(it.format(dateFormatter)) }
                        withValidEmail(profile.email) {
                            emails.add(it)
                        }
                        withValidCity(profile.city) {
                            cities.add(it)
                        }
                        withValidPhoneNumber(profile.phone) {
                            phoneNumbers.add(it)
                        }
                        withValidZipCode(profile.zipCode) {
                            zipCodes.add(it)
                        }
                        withValidAddress(profile.street) {
                            streets.add(it)
                        }
                    }
                }
            }.launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensorContainer? = mutex.withLock {
        var container = CensorContainer(message)

        names.forEach { name ->
            container = container.censor(name, "#name")
        }

        dates.forEach { dateString ->
            container = container.censor(dateString, "#date")
        }

        streets.forEach { street ->
            container = container.censor(street, "#street")
        }

        cities.forEach { city ->
            container = container.censor(city, "#city")
        }

        zipCodes.forEach { zipCode ->
            container = container.censor(zipCode, "#zipCode")
        }

        phoneNumbers.forEach { phone ->
            container = container.censor(phone, "#phone")
        }

        emails.forEach { email ->
            container = container.censor(email, "#email")
        }

        return container.nullIfEmpty()
    }
}
