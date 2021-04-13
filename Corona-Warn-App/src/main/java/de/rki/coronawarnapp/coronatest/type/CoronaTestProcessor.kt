package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode

interface CoronaTestProcessor<in RequestType : CoronaTestQRCode, TestType : CoronaTest> {

    val type: CoronaTest.Type

    suspend fun create(data: RequestType): TestType

    suspend fun markSubmitted(test: TestType): TestType
}
