package de.rki.coronawarnapp.bugreporting.censors.family

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyTestCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    familyTestRepository: FamilyTestRepository,
) : BugCensor {

    private val mutex = Mutex()
    private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    private val names = mutableSetOf<String>()
    private val dates = mutableSetOf<LocalDate>()
    private val registrationTokens = mutableSetOf<String>()
    private val identifiers = mutableSetOf<String>()

    init {
        listOf(
            familyTestRepository.familyTests,
            familyTestRepository.familyTestsInRecycleBin
        )
            .merge()
            .filterNotNull()
            .onEach { tests ->
                mutex.withLock {
                    tests.forEach { test ->
                        registrationTokens.add(test.registrationToken)
                        identifiers.add(test.identifier)
                        names.add(test.personName)
                        test.coronaTest.additionalInfo?.let { info ->
                            info.firstName?.let { names.add(it) }
                            info.lastName?.let { names.add(it) }
                            info.dateOfBirth?.let { dates.add(it) }
                        }
                    }
                }
            }.launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): BugCensor.CensorContainer? {
        var container = BugCensor.CensorContainer(message)
        mutex.withLock {
            registrationTokens.forEach {
                container = container.censor(it, "#token")
            }
            identifiers.forEach {
                container = container.censor(it, "#identifier")
            }
            names.forEach {
                withValidName(it) { firstName ->
                    container = container.censor(firstName, "#name")
                }
            }
            dates.forEach {
                container = container.censor(it.toString(dateFormatter), "#dateOfBirth")
            }
        }
        return container.nullIfEmpty()
    }

    suspend fun addName(name: String) {
        mutex.withLock { names.add(name) }
    }
}
