package de.rki.coronawarnapp.covidcertificate.common.certificate

import android.widget.LinearLayout
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.IncludeTravelNoticeCardBinding
import de.rki.coronawarnapp.util.setUrl

class TravelNoticeView @JvmOverloads constructor(
    context: Context,
) : LinearLayout(context) {

    private val binding: IncludeTravelNoticeCardBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.include_travel_notice_card, this, true)

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
        binding = IncludeTravelNoticeCardBinding.bind(this)

        binding.apply {
            if (travelNoticeGerman.text ==
                context.getString(R.string.green_certificate_attribute_certificate_travel_notice_german)
            ) {
                travelNoticeGerman.setUrl(
                    R.string.green_certificate_attribute_certificate_travel_notice_german,
                    R.string.green_certificate_travel_notice_link_de,
                    R.string.green_certificate_travel_notice_link_de
                )
            }

            if (travelNoticeEnglish.text ==
                context.getString(R.string.green_certificate_attribute_certificate_travel_notice_english)
            ) {
                travelNoticeEnglish.setUrl(
                    R.string.green_certificate_attribute_certificate_travel_notice_english,
                    R.string.green_certificate_travel_notice_link_en,
                    R.string.green_certificate_travel_notice_link_en
                )
            }
        }
    }
}
