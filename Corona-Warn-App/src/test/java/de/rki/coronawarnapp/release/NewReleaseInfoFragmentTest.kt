package de.rki.coronawarnapp.release

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.R
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.After
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
class NewReleaseInfoFragmentTest : BaseTest() {

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Config(qualifiers = "de")
    @Test
    fun `ensure GERMAN new release info arrays are of equal length`() = loadAndCompareStringArrayResources()

    @Config(qualifiers = "en")
    @Test
    fun `ensure ENGLISH new release info arrays are of equal length`() = loadAndCompareStringArrayResources()

    @Config(qualifiers = "pl")
    @Test
    fun `ensure POLISH new release info arrays are of equal length`() = loadAndCompareStringArrayResources()

    @Config(qualifiers = "ro")
    @Test
    fun `ensure ROMANIAN new release info arrays are of equal length`() = loadAndCompareStringArrayResources()

    @Config(qualifiers = "uk")
    @Test
    fun `ensure UKRAINIAN new release info arrays are of equal length`() = loadAndCompareStringArrayResources()

    @Config(qualifiers = "tr")
    @Test
    fun `ensure TURKISH new release info arrays are of equal length`() = loadAndCompareStringArrayResources()

    @Config(qualifiers = "bg")
    @Test
    fun `ensure BULGARIAN new release info arrays are of equal length`() = loadAndCompareStringArrayResources()

    @Config(qualifiers = "fr")
    @Test
    fun `ensure DEFAULT aka FRENCH new release info arrays are of equal length`() = loadAndCompareStringArrayResources()

    private fun loadAndCompareStringArrayResources() {
        val titles = context.resources.getStringArray(R.array.new_release_title)
        val bodies = context.resources.getStringArray(R.array.new_release_body)
        val labels = context.resources.getStringArray(R.array.new_release_linkified_labels)
        val urls = context.resources.getStringArray(R.array.new_release_target_urls)

        titles.size shouldBe bodies.size
        bodies.size shouldBe labels.size
        labels.size shouldBe urls.size
    }
}
