package de.rki.coronawarnapp.statistics.local.storage

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(name = "SelectedDistrict", value = SelectedStatisticsLocation.SelectedDistrict::class),
    JsonSubTypes.Type(name = "SelectedFederalState", value = SelectedStatisticsLocation.SelectedFederalState::class)
)
sealed class SelectedStatisticsLocation(val type: String) {
    abstract val addedAt: Instant
    abstract val uniqueID: Long

    data class SelectedDistrict(
        @JsonProperty("district") val district: Districts.District,
        @JsonProperty("addedAt") override val addedAt: Instant,
    ) : SelectedStatisticsLocation("SelectedDistrict") {
        @get:JsonIgnore
        override val uniqueID: Long
            get() = 1000000L + district.districtId
    }

    data class SelectedFederalState(
        @JsonProperty("federalState") val federalState: PpaData.PPAFederalState,
        @JsonProperty("addedAt") override val addedAt: Instant,
    ) : SelectedStatisticsLocation("SelectedFederalState") {
        @get:JsonIgnore
        override val uniqueID: Long
            get() = 2000000L + federalState.number
    }
}
