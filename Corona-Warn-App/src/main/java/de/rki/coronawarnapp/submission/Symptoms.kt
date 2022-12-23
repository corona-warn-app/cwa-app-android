package de.rki.coronawarnapp.submission

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Symptoms(
    /**
     * this is null if there are no symptoms or there is no information
     */
    @JsonProperty("startOfSymptoms") val startOfSymptoms: StartOf?,
    @JsonProperty("symptomIndication") val symptomIndication: Indication
) : Parcelable {

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true
    )
    @JsonSubTypes(
        JsonSubTypes.Type(name = "Date", value = StartOf.Date::class),
        JsonSubTypes.Type(name = "LastSevenDays", value = StartOf.LastSevenDays::class),
        JsonSubTypes.Type(name = "OneToTwoWeeksAgo", value = StartOf.OneToTwoWeeksAgo::class),
        JsonSubTypes.Type(name = "MoreThanTwoWeeks", value = StartOf.MoreThanTwoWeeks::class),
        JsonSubTypes.Type(name = "NoInformation", value = StartOf.NoInformation::class)
    )
    sealed class StartOf(val type: String) : Parcelable {
        @Parcelize
        data class Date(
            @JsonProperty("date") val date: LocalDate
        ) : StartOf("Date")

        @Parcelize
        object LastSevenDays : StartOf("LastSevenDays") {
            override fun equals(other: Any?): Boolean = other is LastSevenDays

            override fun hashCode(): Int = System.identityHashCode(this)
        }

        @Parcelize
        object OneToTwoWeeksAgo : StartOf("OneToTwoWeeksAgo") {
            override fun equals(other: Any?): Boolean = other is OneToTwoWeeksAgo

            override fun hashCode(): Int = System.identityHashCode(this)
        }

        @Parcelize
        object MoreThanTwoWeeks : StartOf("MoreThanTwoWeeks") {
            override fun equals(other: Any?): Boolean = other is MoreThanTwoWeeks

            override fun hashCode(): Int = System.identityHashCode(this)
        }

        @Parcelize
        object NoInformation : StartOf("NoInformation") {
            override fun equals(other: Any?): Boolean = other is NoInformation

            override fun hashCode(): Int = System.identityHashCode(this)
        }
    }

    @Parcelize
    enum class Indication : Parcelable {
        @JsonProperty("POSITIVE")
        POSITIVE,

        @JsonProperty("NEGATIVE")
        NEGATIVE,

        @JsonProperty("NO_INFORMATION")
        NO_INFORMATION
    }

    companion object {
        val NO_INFO_GIVEN = Symptoms(
            startOfSymptoms = null,
            symptomIndication = Indication.NO_INFORMATION
        )
    }
}
