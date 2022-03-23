package de.rki.coronawarnapp.util.coroutine

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode

fun CoronaTestQRCode.modifyCategoryType(category: CoronaTestQRCode.CategoryType) : CoronaTestQRCode
{
    return when (this) {
        is CoronaTestQRCode.PCR -> copy(categoryType = category)
        is CoronaTestQRCode.RapidPCR -> copy(categoryType = category)
        is CoronaTestQRCode.RapidAntigen -> copy(categoryType = category)
        else -> this
    }
}
