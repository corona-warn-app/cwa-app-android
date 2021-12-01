package de.rki.coronawarnapp.bugreporting.censors.dccticketing

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccTicketingJwtCensor @Inject constructor() : BugCensor {

    private val vcMutex = Mutex()
    private val jwtMutex = Mutex()

    override suspend fun checkLog(message: String): BugCensor.CensorContainer? {
        var newMessage = BugCensor.CensorContainer(message)
        jwtMutex.withLock {
            jwtSet.forEach {
                newMessage = censorJwt(it, newMessage)
            }
        }
        vcMutex.withLock {
            vcSet.forEach {
                newMessage = censorVc(it, newMessage)
            }
        }
        return newMessage.nullIfEmpty()
    }

    suspend fun addJwt(rawJwt: String) {
        jwtMutex.withLock {
            jwtSet.add(rawJwt)
        }
    }

    suspend fun addVc(vc: DccTicketingValidationCondition) {
        vcMutex.withLock {
            vcSet.add(vc)
        }
    }

    private fun censorVc(
        vc: DccTicketingValidationCondition,
        message: BugCensor.CensorContainer
    ): BugCensor.CensorContainer {
        var newMessage = message
        vc.gnt?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/givenName"
            )
        }
        vc.fnt?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/familyName"
            )
        }
        vc.dob?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/dateOfBirth"
            )
        }

        vc.coa?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/countryOfArrival"
            )
        }

        vc.roa?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/regionOfArrival"
            )
        }

        vc.cod?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/countryOfDeparture"
            )
        }

        vc.rod?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/regionOfDeparture"
            )
        }

        vc.category?.let {
            it.forEachIndexed { index, category ->
                newMessage = newMessage.censor(
                    toReplace = category.trim(),
                    replacement = "ticketing/category$index"
                )
            }
        }

        vc.validationClock?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/validationClock"
            )
        }

        vc.validFrom?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/validFrom"
            )
        }

        vc.validTo?.let {
            newMessage = newMessage.censor(
                toReplace = it.trim(),
                replacement = "ticketing/validTo"
            )
        }

        return newMessage
    }

    private fun censorJwt(
        rawJwt: String,
        message: BugCensor.CensorContainer
    ): BugCensor.CensorContainer {
        var newMessage = message
        newMessage = newMessage.censor(
            toReplace = rawJwt,
            "ticketing/rawJwt"
        )
        return newMessage
    }

    private val jwtSet: MutableSet<String> = mutableSetOf()
    private val vcSet: MutableSet<DccTicketingValidationCondition> = mutableSetOf()
}
