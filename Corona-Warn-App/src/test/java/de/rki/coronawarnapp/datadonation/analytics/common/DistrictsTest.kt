package de.rki.coronawarnapp.datadonation.analytics.common

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.ConscryptMode
import testhelpers.BaseTest
import testhelpers.EmptyApplication

@Config(sdk = [Build.VERSION_CODES.P], application = EmptyApplication::class)
@RunWith(RobolectricTestRunner::class)
@ConscryptMode(ConscryptMode.Mode.OFF)
class DistrictsTest : BaseTest() {

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    fun createInstance() = Districts(
        context = context,
        gson = Gson()
    )

    @Test
    fun `loading from assets and parsing`() = runTest {
        val districts = createInstance().loadDistricts()
        districts.size shouldBe 412
        districts.last() shouldBe Districts.District(
            districtName = "Weimar",
            districtShortName = "WE",
            districtId = 11016055,
            federalStateName = "Thüringen",
            federalStateShortName = "TH",
            federalStateId = 13000016
        )
    }

    @Test
    fun `districts have only known short names for federal states`() = runTest {
        val districts = createInstance().loadDistricts()

        val stateCodesInDistricts = mutableSetOf<String>()
        districts.forEach { stateCodesInDistricts.add(it.federalStateShortName) }

        val knownFederalStates = PpaData.PPAFederalState.values().filterNot {
            it == PpaData.PPAFederalState.UNRECOGNIZED || it == PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED
        }

        stateCodesInDistricts.size shouldBe knownFederalStates.size

        stateCodesInDistricts.sorted() shouldBe knownFederalStates.map { it.federalStateShortName }.sorted()
    }
}
