package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import de.rki.coronawarnapp.R
import java.util.Locale

class CountrySelectionList(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    init {
        orientation = VERTICAL
    }

    private var _countryList: List<String>? = null
    var countryList: List<String>?
        get() = _countryList
        set(list) {
            _countryList = list
            var index = 0
            list?.map { it.toLowerCase(Locale.ROOT) }?.forEach { countryCode ->

                val countryFlagImageDrawableId = context.resources.getIdentifier(
                    "ic_country_$countryCode",
                    "drawable",
                    context.packageName
                )

                val countryNameResourceId = context.resources.getIdentifier(
                    "country_name_$countryCode",
                    "string",
                    context.packageName
                )


                if (countryFlagImageDrawableId > 0 && countryNameResourceId > 0) {
                    inflate(context, R.layout.view_country_list_entry, this)
                    val child = this.getChildAt(index)
                    index++

                    val imgDrawable = ResourcesCompat
                        .getDrawable(context.resources, countryFlagImageDrawableId, null)


                    child.findViewById<ImageView>(R.id.img_country_flag)
                        .setImageDrawable(imgDrawable)
                    child.findViewById<TextView>(R.id.label_country_name).text =
                        context.getString(countryNameResourceId)

                }

            }
        }

}
