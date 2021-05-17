package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.serialization.BaseGson
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

    var vaccinationValueSet: VaccinationValueSet?
        get() = getValueSet()
        set(value) = setValueSet(value)

    private fun getValueSet(): VaccinationValueSet? {
        Timber.d("Loading value set")
        return prefs.getString(PKEY_VALUE_SETS_PREFIX, null)?.let {
            gson.fromJson(it, StoredVaccinationValueSet::class.java).also { loaded -> Timber.d("Loaded %s", loaded) }
        }.also { Timber.d("Returning %s", it) }
    }

    private fun setValueSet(value: VaccinationValueSet?) {
        Timber.d("Saving %s", value)
        value?.let {
            prefs.edit {
                val storeValue = it.toStoredVaccinationValueSet()
                val json = gson.toJson(storeValue, StoredVaccinationValueSet::class.java)
                Timber.d("String %s", json)
                putString(PKEY_VALUE_SETS_PREFIX, json)
            }
        }
    }

    fun clear() {
        Timber.d("Clearing local storage")
        prefs.clearAndNotify()
    }

    @Keep
    private data class StoredVaccinationValueSet(
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

    private fun VaccinationValueSet.toStoredVaccinationValueSet(): StoredVaccinationValueSet =
        StoredVaccinationValueSet(
            languageCode = languageCode,
            vp = vp.toStoredValueSet(),
            mp = mp.toStoredValueSet(),
            ma = ma.toStoredValueSet()
        )

    private fun VaccinationValueSet.ValueSet.toStoredValueSet(): StoredVaccinationValueSet.StoredValueSet =
        StoredVaccinationValueSet.StoredValueSet(
            items = items.map { it.toStoredItem() }
        )

    private fun VaccinationValueSet.ValueSet.Item.toStoredItem(): StoredVaccinationValueSet.StoredValueSet.StoredItem =
        StoredVaccinationValueSet.StoredValueSet.StoredItem(
            key = key,
            displayText = displayText
        )
}

private const val PREF_NAME = "valuesets_local"
private const val PKEY_VALUE_SETS_PREFIX = "valuesets"
