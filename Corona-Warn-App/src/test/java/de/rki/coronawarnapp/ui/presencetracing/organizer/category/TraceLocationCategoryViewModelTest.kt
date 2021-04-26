package de.rki.coronawarnapp.ui.presencetracing.organizer.category

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationUIType
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.header.TraceLocationHeaderItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.separator.TraceLocationSeparatorItem
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.observeForTesting

@ExtendWith(InstantExecutorExtension::class)
internal class TraceLocationCategoryViewModelTest : BaseTest() {

    @Test
    fun `viewModel should emit correct categoryItems`() {
        val viewModel = TraceLocationCategoryViewModel()

        viewModel.categoryItems.observeForTesting {
            viewModel.categoryItems.value shouldBe listOf(

                // Location Header
                TraceLocationHeaderItem(R.string.tracelocation_organizer_category_type_location_header),

                // Location Categories
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_RETAIL,
                    TraceLocationUIType.LOCATION,
                    R.string.tracelocation_organizer_category_retail_title,
                    R.string.tracelocation_organizer_category_retail_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
                    TraceLocationUIType.LOCATION,
                    R.string.tracelocation_organizer_category_food_service_title,
                    R.string.tracelocation_organizer_category_food_service_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_CRAFT,
                    TraceLocationUIType.LOCATION,
                    R.string.tracelocation_organizer_category_craft_title,
                    R.string.tracelocation_organizer_category_craft_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_WORKPLACE,
                    TraceLocationUIType.LOCATION,
                    R.string.tracelocation_organizer_category_workplace_title,
                    R.string.tracelocation_organizer_category_workplace_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_EDUCATIONAL_INSTITUTION,
                    TraceLocationUIType.LOCATION,
                    R.string.tracelocation_organizer_category_educational_institution_title,
                    R.string.tracelocation_organizer_category_educational_institution_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_PUBLIC_BUILDING,
                    TraceLocationUIType.LOCATION,
                    R.string.tracelocation_organizer_category_public_building_title,
                    R.string.tracelocation_organizer_category_public_building_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER,
                    TraceLocationUIType.LOCATION,
                    R.string.tracelocation_organizer_category_other_location_title
                ),

                // Separator
                TraceLocationSeparatorItem,

                // Event Header
                TraceLocationHeaderItem(R.string.tracelocation_organizer_category_type_event_header),

                // Event Categories
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
                    TraceLocationUIType.EVENT,
                    R.string.tracelocation_organizer_category_cultural_event_title,
                    R.string.tracelocation_organizer_category_cultural_event_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CLUB_ACTIVITY,
                    TraceLocationUIType.EVENT,
                    R.string.tracelocation_organizer_category_club_activity_title,
                    R.string.tracelocation_organizer_category_club_activity_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
                    TraceLocationUIType.EVENT,
                    R.string.tracelocation_organizer_category_private_event_title,
                    R.string.tracelocation_organizer_category_private_event_subtitle
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_WORSHIP_SERVICE,
                    TraceLocationUIType.EVENT,
                    R.string.tracelocation_organizer_category_worship_service_title
                ),
                TraceLocationCategory(
                    TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
                    TraceLocationUIType.EVENT,
                    R.string.tracelocation_organizer_category_other_event_title
                )
            )
        }
    }
}
