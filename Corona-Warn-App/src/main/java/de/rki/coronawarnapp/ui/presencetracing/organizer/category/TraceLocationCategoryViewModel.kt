package de.rki.coronawarnapp.ui.presencetracing.organizer.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.CategoryItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationUIType
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.traceLocationCategories
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.header.TraceLocationHeaderItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.separator.TraceLocationSeparatorItem
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TraceLocationCategoryViewModel @AssistedInject constructor() : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationCategoryViewModel>

    private val categoryItemList = mutableListOf<CategoryItem>().apply {
        add(TraceLocationHeaderItem(R.string.tracelocation_organizer_category_type_location_header))
        addAll(traceLocationCategories.filter { it.uiType == TraceLocationUIType.LOCATION })
        add(TraceLocationSeparatorItem)
        add(TraceLocationHeaderItem(R.string.tracelocation_organizer_category_type_event_header))
        addAll(traceLocationCategories.filter { it.uiType == TraceLocationUIType.EVENT })
    }.toList()

    val categoryItems: LiveData<List<CategoryItem>> = MutableLiveData(categoryItemList)
}
