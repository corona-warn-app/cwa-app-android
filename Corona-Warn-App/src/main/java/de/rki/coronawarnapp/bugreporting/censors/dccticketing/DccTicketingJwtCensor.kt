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
                "givenName"
            )
        }
        vc.fnt?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "familyName"
            )
        }
        vc.dob?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "dateOfBirth"
            )
        }

        vc.coa?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "countryOfArrival"
            )
        }

        vc.roa?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "regionOfArrival"
            )
        }

        vc.cod?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "countryOfDeparture"
            )
        }

        vc.rod?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "regionOfDeparture"
            )
        }

        vc.category?.let {
            it.forEachIndexed { index, category ->
                newMessage = newMessage.censor(
                    category.trim(),
                    "category$index"
                )
            }

        }

        vc.validationClock?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "validationClock"
            )
        }

        vc.validFrom?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "validFrom"
            )
        }

        vc.validTo?.let {
            newMessage = newMessage.censor(
                it.trim(),
                "validTo"
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
            "rawJwt"
        )
        return newMessage
    }

    fun clear() {
        synchronized(jwtSet) {
            jwtSet.clear()
        }
        synchronized(vcSet) {
            vcSet.clear()
        }
    }

    private val jwtSet: MutableSet<String> = mutableSetOf()
    private val vcSet: MutableSet<DccTicketingValidationCondition> = mutableSetOf()
}


