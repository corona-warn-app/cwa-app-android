package de.rki.coronawarnapp.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.rki.coronawarnapp.R

enum class Country(
    val code: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    AT("at", R.string.country_name_at, R.drawable.ic_country_at),
    BE("be", R.string.country_name_be, R.drawable.ic_country_be),
    BG("bg", R.string.country_name_bg, R.drawable.ic_country_bg),
    CH("ch", R.string.country_name_ch, R.drawable.ic_country_ch),
    CY("cy", R.string.country_name_cy, R.drawable.ic_country_cy),
    CZ("cz", R.string.country_name_cz, R.drawable.ic_country_cz),
    DE("de", R.string.country_name_de, R.drawable.ic_country_de),
    DK("dk", R.string.country_name_dk, R.drawable.ic_country_dk),
    EE("ee", R.string.country_name_ee, R.drawable.ic_country_ee),
    ES("es", R.string.country_name_es, R.drawable.ic_country_es),
    FI("fi", R.string.country_name_fi, R.drawable.ic_country_fi),
    FR("fr", R.string.country_name_fr, R.drawable.ic_country_fr),
    UK("uk", R.string.country_name_uk, R.drawable.ic_country_uk),
    GR("gr", R.string.country_name_gr, R.drawable.ic_country_gr),
    HR("hr", R.string.country_name_hr, R.drawable.ic_country_hr),
    HU("hu", R.string.country_name_hu, R.drawable.ic_country_hu),
    IE("ie", R.string.country_name_ie, R.drawable.ic_country_ie),
    IS("is", R.string.country_name_is, R.drawable.ic_country_is),
    IT("it", R.string.country_name_it, R.drawable.ic_country_it),
    LI("li", R.string.country_name_li, R.drawable.ic_country_li),
    LT("lt", R.string.country_name_lt, R.drawable.ic_country_lt),
    LU("lu", R.string.country_name_lu, R.drawable.ic_country_lu),
    LV("lv", R.string.country_name_lv, R.drawable.ic_country_lv),
    MT("mt", R.string.country_name_mt, R.drawable.ic_country_mt),
    NL("nl", R.string.country_name_nl, R.drawable.ic_country_nl),
    NO("no", R.string.country_name_no, R.drawable.ic_country_no),
    PL("pl", R.string.country_name_pl, R.drawable.ic_country_pl),
    PT("pt", R.string.country_name_pt, R.drawable.ic_country_pt),
    RO("ro", R.string.country_name_ro, R.drawable.ic_country_ro),
    SE("se", R.string.country_name_se, R.drawable.ic_country_se),
    SI("si", R.string.country_name_si, R.drawable.ic_country_si),
    SK("sk", R.string.country_name_sk, R.drawable.ic_country_sk);

    fun getLabel(context: Context): String {
        return context.getString(labelRes)
    }
}
