package de.rki.coronawarnapp.bugreporting.censors.dccticketing

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccTicketingJwtCensor @Inject constructor() : BugCensor {
    override suspend fun checkLog(message: String): BugCensor.CensorContainer? {
        var newMessage = BugCensor.CensorContainer(message)
        synchronized(jwtSet) {
            jwtSet.forEach {
                newMessage = censorJwt(it, newMessage)
            }
        }
        synchronized(vcSet) {
            vcSet.forEach {
                newMessage = censorVc(it, newMessage)
            }
        }
        return newMessage.nullIfEmpty()
    }

    fun addJwt(rawJwt: String) {
        synchronized(jwtSet) {
            jwtSet.add(rawJwt)
        }
    }

    fun addVc(vc: DccTicketingValidationCondition) {
        synchronized(vcSet) {
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
                it.trim(),
                "ticketing/givenName"
            )
        }
        vc.fnt?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/familyName"
            )
        }
        vc.dob?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/dateOfBirth"
            )
        }

        vc.coa?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/countryOfArrival"
            )
        }

        vc.roa?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/regionOfArrival"
            )
        }

        vc.cod?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/countryOfDeparture"
            )
        }

        vc.rod?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/regionOfDeparture"
            )
        }

        vc.category?.let {
            it.forEachIndexed { index, category ->
                newMessage = newMessage.censor(
                    category.trim(),
                    "ticketing/category$index"
                )
            }
        }

        vc.validationClock?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/validationClock"
            )
        }

        vc.validFrom?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/validFrom"
            )
        }

        vc.validTo?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "ticketing/validTo"
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
            rawJwt,
            "ticketing/rawJwt"
        )
        return newMessage
    }

    private val jwtSet: MutableSet<String> = mutableSetOf()
    private val vcSet: MutableSet<DccTicketingValidationCondition> = mutableSetOf()
}
