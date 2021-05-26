package de.rki.coronawarnapp.bugreporting.censors

interface BugCensor {

    /**
     * If there is something to censor a new log line is returned, otherwise returns null
     */
    suspend fun checkLog(message: String): CensoredString?

    data class CensorContainer(
        // Original String, necessary for correct censoring ranges
        val original: String,
        val actions: List<Action> = emptyList()
    ) {

        fun censor(toReplace: String, replacement: String): CensorContainer {
            if (!original.contains(toReplace)) return this

            val start = original.indexOf(toReplace)
            if (start == -1) return this // Shouldn't happen

            val end = original.lastIndexOf(toReplace) + toReplace.length

            val newAction = Action(
                range = start..end,
                execute = { it.replace(toReplace, replacement) }
            )
            return this.copy(actions = actions.plus(newAction))
        }

        fun compile(): CensoredString? {
            val ranges = actions.map { it.range }
            if (ranges.isEmpty()) return null

            val isIntersecting = ranges.any { outter ->
                ranges.any { inner ->
                    outter != inner && (inner.contains(outter.first) || inner.contains(outter.last))
                }
            }

            val minMin = ranges.minOf { it.first }.coerceAtLeast(0).coerceAtMost(original.length)
            val maxMax = ranges.maxOf { it.last }.coerceAtLeast(0).coerceAtMost(original.length)

            return if (isIntersecting) {
                CensoredString(
                    censored = original.replaceRange(minMin, maxMax, "<internal-censor-collision>"),
                    range = minMin..maxMax
                )
            } else {
                CensoredString(
                    censored = actions.fold(original) { notOriginal, action -> action.execute(notOriginal) },
                    range = minMin..maxMax
                )
            }
        }

        data class Action(
            val range: IntRange,
            val execute: (String) -> String
        )

        companion object {
            fun fromOriginal(original: String): CensorContainer = CensorContainer(
                original = original
            )
        }
    }

    data class CensoredString(
        // The censored version of the string
        val censored: String,
        // The range that we censored
        // If there is a collision, this range in the original needs to be removed.
        val range: IntRange
    )

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

        fun CensorContainer.compile(): CensoredString? {
            return if (actions.isEmpty()) null else this.compile()
        }
    }
}
