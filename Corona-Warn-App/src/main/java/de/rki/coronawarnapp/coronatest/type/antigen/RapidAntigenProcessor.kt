package de.rki.coronawarnapp.coronatest.type.antigen

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestProcessor
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RapidAntigenProcessor @Inject constructor(

) : CoronaTestProcessor {
    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    override fun create(data: CoronaTestQRCode): CoronaTest {
        Timber.tag(TAG).d("create(data=%s)", data)
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "RapidAntigenProcessor"
    }
}
