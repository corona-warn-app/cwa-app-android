package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
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
        Timber.v("Loading value set")
        return prefs.getString(PKEY_VALUE_SETS_PREFIX, null)
            ?.let { gson.fromJson(it, StoredVaccinationValueSet::class.java) }
            .also { loaded -> Timber.v("Loaded value set %s", loaded) }
    }

    private fun setValueSet(value: VaccinationValueSet?) {
        Timber.v("Saving value set %s", value)
        prefs.edit {
            val json = value?.let {
                gson.toJson(it.toStoredVaccinationValueSet(), StoredVaccinationValueSet::class.java)
            }
            Timber.v("Writing %s to prefs", json)
            putString(PKEY_VALUE_SETS_PREFIX, json)
        }
    }

    @VisibleForTesting()
    fun VaccinationValueSet.toStoredVaccinationValueSet(): StoredVaccinationValueSet =
        StoredVaccinationValueSet(
            languageCode = languageCode,
            vp = vp.toStoredValueSet(),
            mp = mp.toStoredValueSet(),
            ma = ma.toStoredValueSet()
        )

    @VisibleForTesting()
    fun VaccinationValueSet.ValueSet.toStoredValueSet(): StoredVaccinationValueSet.StoredValueSet =
        StoredVaccinationValueSet.StoredValueSet(
            items = items.map { it.toStoredItem() }
        )

    @VisibleForTesting()
    fun VaccinationValueSet.ValueSet.Item.toStoredItem():
        StoredVaccinationValueSet.StoredValueSet.StoredItem =
        StoredVaccinationValueSet.StoredValueSet.StoredItem(
            key = key,
            displayText = displayText
        )

    @VisibleForTesting
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
