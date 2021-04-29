package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine

interface BugCensor {

    /**
     * If there is something to censor a new log line is returned, otherwise returns null
     */
    suspend fun checkLog(entry: LogLine): LogLine?

    companion object {
        fun withValidName(name: String?, action: (String) -> Unit): Boolean {
            if (name.isNullOrBlank()) return false
            if (name.length < 3) return false
            action(name)
            return true
        }

        fun withValidEmail(email: String?, action: (String) -> Unit): Boolean {
            if (email.isNullOrBlank()) return false
            if (email.length < 6) return false
            action(email)
            return true
        }

        fun withValidPhoneNumber(number: String?, action: (String) -> Unit): Boolean {
            if (number.isNullOrBlank()) return false
            if (number.length < 4) return false
            action(number)
            return true
        }

        fun withValidComment(comment: String?, action: (String) -> Unit): Boolean {
            if (comment.isNullOrBlank()) return false
            if (comment.length < 3) return false
            action(comment)
            return true
        }

        fun withValidDescription(description: String?, action: (String) -> Unit): Boolean {
            if (description.isNullOrBlank()) return false
            if (description.length < 5) return false
            action(description)
            return true
        }

        fun withValidAddress(address: String?, action: (String) -> Unit): Boolean {
            if (address.isNullOrBlank()) return false
            if (address.length < 4) return false
            action(address)
            return true
        }

        fun withValidCity(city: String?, action: (String) -> Unit): Boolean {
            if (city.isNullOrBlank()) return false
            if (city.length < 3) return false
            action(city)
            return true
        }

        fun withValidZipCode(zipCode: String?, action: (String) -> Unit): Boolean {
            if (zipCode.isNullOrBlank()) return false
            if (zipCode.length < 5) return false
            action(zipCode)
            return true
        }

        fun LogLine.toNewLogLineIfDifferent(newMessage: String): LogLine? {
            return if (newMessage != message) copy(message = newMessage) else null
        }
    }
}
