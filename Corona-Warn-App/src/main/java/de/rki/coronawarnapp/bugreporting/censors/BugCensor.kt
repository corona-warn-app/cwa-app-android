package de.rki.coronawarnapp.bugreporting.censors

interface BugCensor {

    suspend fun checkLog(message: String): CensorContainer?

    data class CensorContainer(
        // Original String, necessary for correct censoring ranges
        val original: String,
        val actions: Set<Action> = emptySet()
    ) {

        fun censor(toReplace: String, replacement: String): CensorContainer {
            if (!original.contains(toReplace)) return this

            val start = original.indexOf(toReplace)
            if (start == -1) return this // Shouldn't happen

            val end = original.lastIndexOf(toReplace) + toReplace.length

            val newAction = Action(
                range = start..end,
                modifier = Action.SimpleReplace(toReplace, replacement)
            )
            return this.copy(actions = actions.plus(newAction))
        }

        fun compile(): CensoredString? {
            val ranges = actions.map { it.range }
            if (ranges.isEmpty()) return null

            val isIntersecting = ranges.any { outer ->
                ranges.any { inner ->
                    outer !== inner &&
                        (inner.contains(outer.first) || inner.contains(outer.last)) &&
                        (inner.last != outer.first && inner.first != outer.last)
                }
            }

            return if (isIntersecting) {
                val minMin = ranges.minOf { it.first }
                val maxMax = ranges.maxOf { it.last }
                CensoredString(
                    censored = original.replaceRange(minMin, maxMax, COLLISION_STRING),
                    ranges = listOf(minMin..maxMax)
                )
            } else {
                CensoredString(
                    censored = actions.fold(original) { notOriginal, action ->
                        action.execute(notOriginal)
                    },
                    ranges = ranges
                )
            }
        }

        fun nullIfEmpty(): CensorContainer? = if (actions.isEmpty()) null else this

        data class Action(
            val range: IntRange,
            val modifier: StringModifier,
        ) {

            fun execute(original: String) = modifier.execute(original)

            data class SimpleReplace(
                val oldValue: String,
                val newValue: String,
            ) : StringModifier {
                override val execute: (String) -> String = { it.replace(oldValue, newValue) }
            }

            interface StringModifier {
                val execute: (String) -> String
            }
        }

        companion object {
            const val COLLISION_STRING = "<censor-collision/>"

            fun createErrorContainer(censor: BugCensor, exception: Exception): CensorContainer {
                return CensorContainer("<censor-error>$censor: $exception</censor-error>")
            }
        }
    }

    data class CensoredString(
        // The censored version of the string
        val censored: String,
        // The range that we censored
        // If there is a collision, this range in the original needs to be removed.
        val ranges: List<IntRange>
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

        fun containerForError(
            censor: BugCensor,
            original: String,
            error: Exception
        ): CensorContainer = CensorContainer(
            original = original,
            actions = setOf(
                CensorContainer.Action(
                    range = 0..original.length,
                    modifier = CensorContainer.Action.SimpleReplace(
                        original,
                        "<censor-error>Module ${censor.javaClass.simpleName}: $error</censor-error>"
                    )
                )
            )
        )
    }
}
