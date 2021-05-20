package de.rki.coronawarnapp.bugreporting.censors

import kotlin.math.max
import kotlin.math.min

interface BugCensor {

    /**
     * If there is something to censor a new log line is returned, otherwise returns null
     */
    suspend fun checkLog(message: String): CensoredString?

    data class CensoredString(
        // The censored version of the string
        val string: String,
        // The range that we censored
        // If there is a collision, this range in the original needs to be removed.
        val range: IntRange? = null
    )

    companion object {
        operator fun CensoredString.plus(newer: CensoredString?): CensoredString {
            if (newer == null) return this

            val range = when {
                newer.range == null -> this.range
                this.range == null -> newer.range
                else -> min(this.range.first, newer.range.first)..max(this.range.last, newer.range.last)
            }

            return CensoredString(string = newer.string, range = range)
        }

        fun CensoredString.censor(orig: String, replacement: String): CensoredString? {
            val start = this.string.indexOf(orig)
            if (start == -1) return null

            val end = start + orig.length
            return CensoredString(
                string = this.string.replace(orig, replacement),
                range = start..end
            )
        }

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

        fun CensoredString.toNullIfUnmodified(): CensoredString? {
            return if (range == null) null else this
        }
    }
}
