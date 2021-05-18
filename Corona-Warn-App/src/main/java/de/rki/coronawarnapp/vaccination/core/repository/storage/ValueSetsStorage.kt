package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.vaccination.core.server.valueset.DefaultVaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class ValueSetsStorage @Inject constructor(
    @AppContext private val context: Context,
    @BaseGson private val gson: Gson
) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var valueSet: FlowPreference<StoredVaccinationValueSet> = prefs.createFlowPreference(
        key = PKEY_VALUE_SETS_PREFIX,
        reader = FlowPreference.gsonReader(gson = gson, createEmptyValueSet()),
        writer = FlowPreference.gsonWriter(gson = gson)
    )

    fun clear() {
        Timber.d("Clearing local storage")
        prefs.clearAndNotify()
    }

    private fun createEmptyValueSet() = StoredVaccinationValueSet(
        languageCode = Locale.ENGLISH,
        vp = StoredVaccinationValueSet.StoredValueSet(items = emptyList()),
        mp = StoredVaccinationValueSet.StoredValueSet(items = emptyList()),
        ma = StoredVaccinationValueSet.StoredValueSet(items = emptyList())
    )

    @Keep
    data class StoredVaccinationValueSet(
        @SerializedName("languageCode") override val languageCode: Locale,
        @SerializedName("vp") override val vp: StoredValueSet,
        @SerializedName("mp") override val mp: StoredValueSet,
        @SerializedName("ma") override val ma: StoredValueSet
    ) : VaccinationValueSet {

        @Keep
        data class StoredValueSet(
            @SerializedName("items") override val items: List<StoredItem>
        ) : VaccinationValueSet.ValueSet {

            @Keep
            data class StoredItem(
                @SerializedName("key") override val key: String,
                @SerializedName("displayText") override val displayText: String
            ) : VaccinationValueSet.ValueSet.Item
        }
    }
}

private const val PREF_NAME = "valuesets_localdata"
private const val PKEY_VALUE_SETS_PREFIX = "valueset"
