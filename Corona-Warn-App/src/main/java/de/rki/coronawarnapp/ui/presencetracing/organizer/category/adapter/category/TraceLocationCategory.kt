package de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category

import android.os.Parcelable
import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_CRAFT
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_EDUCATIONAL_INSTITUTION
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_PUBLIC_BUILDING
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_RETAIL
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_WORKPLACE
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_WORSHIP_SERVICE
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.CategoryItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationUIType.EVENT
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationUIType.LOCATION
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TraceLocationCategory(
    val type: TraceLocationOuterClass.TraceLocationType,
    val uiType: TraceLocationUIType,
    @StringRes val title: Int,
    @StringRes val subtitle: Int? = null
) : CategoryItem, Parcelable {
    @IgnoredOnParcel override val stableId = hashCode().toLong()
}

enum class TraceLocationUIType {
    LOCATION, EVENT
}

val traceLocationCategories = listOf(
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_RETAIL,
        LOCATION,
        R.string.tracelocation_organizer_category_retail_title,
        R.string.tracelocation_organizer_category_retail_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
        LOCATION,
        R.string.tracelocation_organizer_category_food_service_title,
        R.string.tracelocation_organizer_category_food_service_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_CRAFT,
        LOCATION,
        R.string.tracelocation_organizer_category_craft_title,
        R.string.tracelocation_organizer_category_craft_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_WORKPLACE,
        LOCATION,
        R.string.tracelocation_organizer_category_workplace_title,
        R.string.tracelocation_organizer_category_workplace_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_EDUCATIONAL_INSTITUTION,
        LOCATION,
        R.string.tracelocation_organizer_category_educational_institution_title,
        R.string.tracelocation_organizer_category_educational_institution_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_PUBLIC_BUILDING,
        LOCATION,
        R.string.tracelocation_organizer_category_public_building_title,
        R.string.tracelocation_organizer_category_public_building_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_OTHER,
        LOCATION,
        R.string.tracelocation_organizer_category_other_location_title
    ),
    TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
        EVENT,
        R.string.tracelocation_organizer_category_cultural_event_title,
        R.string.tracelocation_organizer_category_cultural_event_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
        EVENT,
        R.string.tracelocation_organizer_category_club_activity_title,
        R.string.tracelocation_organizer_category_club_activity_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
        EVENT,
        R.string.tracelocation_organizer_category_private_event_title,
        R.string.tracelocation_organizer_category_private_event_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_WORSHIP_SERVICE,
        EVENT,
        R.string.tracelocation_organizer_category_worship_service_title
    ),
    TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_OTHER,
        EVENT,
        R.string.tracelocation_organizer_category_other_event_title
    )
)

@StringRes
fun mapTraceLocationToTitleRes(type: TraceLocationOuterClass.TraceLocationType) =
    mapTraceLocationToTitleRes(type.number)

@StringRes
fun mapTraceLocationToTitleRes(type: Int): Int {
    val category = traceLocationCategories.find { it.type.ordinal == type }
    return category?.title ?: R.string.tracelocation_organizer_category_other_location_title
}
