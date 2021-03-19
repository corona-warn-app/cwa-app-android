package de.rki.coronawarnapp.ui.eventregistration.organizer.category

import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import de.rki.coronawarnapp.ui.durationpicker.toReadableDuration
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.Locale

sealed class TraceLocationCreateState {
    abstract val description: String
    abstract val address: String
    abstract val checkInLength: Duration

    abstract fun isDateVisible(): Boolean

    abstract fun isValid(): Boolean

    fun getCheckInLengthString(): String {
        return checkInLength.toReadableDuration()
    }

    abstract fun copy(
        description: String? = null,
        address: String? = null,
        checkInLength: Duration? = null
    ): TraceLocationCreateState

    data class TypeEvent(
        override val description: String = "",
        override val address: String = "",
        override val checkInLength: Duration = Duration.ZERO,
    ) : TraceLocationCreateState() {
        override fun isDateVisible() = true

        override fun isValid(): Boolean =
            description.trim().length in 1..100 && address.trim().length in 0..100 && checkInLength > Duration.ZERO

        override fun copy(description: String?, address: String?, checkInLength: Duration?): TraceLocationCreateState {
            return TypeEvent(
                description = description ?: this.description,
                address = address ?: this.address,
                checkInLength = checkInLength ?: this.checkInLength
            )
        }
    }

    data class TypeLocation(
        override val description: String = "",
        override val address: String = "",
        override val checkInLength: Duration = Duration.standardHours(2),
        val start: LocalDateTime? = null,
        val end: LocalDateTime? = null

    ) : TraceLocationCreateState() {

        fun startString(locale: Locale): String? = start?.toString("E, ${locale.shortDatePattern()}   HH:mm", locale)
        fun endString(locale: Locale): String? = end?.toString("E, ${locale.shortDatePattern()}   HH:mm", locale)

        override fun isDateVisible() = false
        override fun isValid(): Boolean =
            description.trim().length in 1..100 && address.trim().length in 0..100 && checkInLength > Duration.ZERO

        override fun copy(description: String?, address: String?, checkInLength: Duration?): TraceLocationCreateState {
            return TypeLocation(
                description = description ?: this.description,
                address = address ?: this.address,
                checkInLength = checkInLength ?: this.checkInLength,
                start = this.start,
                end = this.end
            )
        }
    }
}
