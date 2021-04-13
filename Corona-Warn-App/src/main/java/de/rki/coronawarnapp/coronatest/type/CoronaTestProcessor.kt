package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode

interface CoronaTestProcessor {

    val type: CoronaTest.Type

    fun create(data: CoronaTestQRCode): CoronaTest
}
