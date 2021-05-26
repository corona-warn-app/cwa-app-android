package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidEmail
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidPhoneNumber
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class DiaryPersonCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    private val mutex = Mutex()

    // We keep a history of all persons so that we can censor them even after they got deleted
    private val personHistory = mutableSetOf<ContactDiaryPerson>()

    init {
        diary.people
            .onEach { mutex.withLock { personHistory.addAll(it) } }
            .launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensoredString? = mutex.withLock {

        if (personHistory.isEmpty()) return null

        val newMessage = personHistory.fold(CensoredString.fromOriginal(message)) { orig, person ->
            var wip = orig

            withValidName(person.fullName) {
                wip += wip.censor(it, "Person#${person.personId}/Name")
            }
            withValidEmail(person.emailAddress) {
                wip += wip.censor(it, "Person#${person.personId}/EMail")
            }
            withValidPhoneNumber(person.phoneNumber) {
                wip += wip.censor(it, "Person#${person.personId}/PhoneNumber")
            }

            wip
        }

        return newMessage.toNullIfUnmodified()
    }
}
