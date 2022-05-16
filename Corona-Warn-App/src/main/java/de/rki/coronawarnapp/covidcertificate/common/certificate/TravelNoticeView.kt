package de.rki.coronawarnapp.covidcertificate.common.certificate

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.IncludeTravelNoticeCardBinding
import setTextWithUrl

class TravelNoticeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: IncludeTravelNoticeCardBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.include_travel_notice_card, this, true)

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
        binding = IncludeTravelNoticeCardBinding.bind(this)

        binding.apply {
            if (travelNoticeGerman.text ==
                context.getString(R.string.covid_certificate_attribute_travel_notice_german)
            ) {
                travelNoticeGerman.setTextWithUrl(
                    R.string.covid_certificate_attribute_travel_notice_german,
                    R.string.green_certificate_travel_notice_link_de,
                    R.string.green_certificate_travel_notice_link_de
                )
                travelNoticeGerman.setLinkTextColor(resources.getColor(R.color.colorTextTint, null))
            }

            if (travelNoticeEnglish.text ==
                context.getString(R.string.covid_certificate_attribute_travel_notice_english)
            ) {
                travelNoticeEnglish.setTextWithUrl(
                    R.string.covid_certificate_attribute_travel_notice_english,
                    R.string.green_certificate_travel_notice_link_en,
                    R.string.green_certificate_travel_notice_link_en
                )
                travelNoticeEnglish.setLinkTextColor(resources.getColor(R.color.colorTextTint, null))
            }
        }
    }
}
