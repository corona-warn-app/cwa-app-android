package de.rki.coronawarnapp.eventregistration.events.ui.category

import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
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

data class TraceLocationCategory(
    val type: TraceLocationOuterClass.TraceLocationType,
    @StringRes val title: Int,
    @StringRes val subtitle: Int? = null
)

val traceLocationCategoriesLocations = listOf(
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_RETAIL,
        R.string.tracelocation_organizer_category_retail_title,
        R.string.tracelocation_organizer_category_retail_subtitle
    ),
    TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
        R.string.tracelocation_organizer_category_food_service_title,
        R.string.tracelocation_organizer_category_food_service_subtitle
    ), TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_WORKPLACE,
        R.string.tracelocation_organizer_category_workplace_title,
        R.string.tracelocation_organizer_category_workplace_subtitle
    ), TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_EDUCATIONAL_INSTITUTION,
        R.string.tracelocation_organizer_category_educational_institution_title,
        R.string.tracelocation_organizer_category_educational_institution_subtitle
    ), TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_PUBLIC_BUILDING,
        R.string.tracelocation_organizer_category_public_building_title,
        R.string.tracelocation_organizer_category_public_building_subtitle
    ), TraceLocationCategory(
        LOCATION_TYPE_PERMANENT_OTHER,
        R.string.tracelocation_organizer_category_other_event_title
    )
)

val traceLocationCategoriesEvents = listOf(
    TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
        R.string.tracelocation_organizer_category_cultural_event_title,
        R.string.tracelocation_organizer_category_cultural_event_subtitle
    ), TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
        R.string.tracelocation_organizer_category_club_activity_title,
        R.string.tracelocation_organizer_category_club_activity_subtitle
    ), TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
        R.string.tracelocation_organizer_category_private_event_title,
        R.string.tracelocation_organizer_category_private_event_subtitle
    ), TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_WORSHIP_SERVICE,
        R.string.tracelocation_organizer_category_worship_service_title
    ), TraceLocationCategory(
        LOCATION_TYPE_TEMPORARY_OTHER,
        R.string.tracelocation_organizer_category_other_event_title
    )
)
