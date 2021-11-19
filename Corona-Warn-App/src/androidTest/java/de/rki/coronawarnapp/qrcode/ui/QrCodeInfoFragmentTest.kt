package de.rki.coronawarnapp.qrcode.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.qrcode.ui.info.UqsInfoFragment
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class QrCodeInfoFragmentTest : BaseUITest() {

    @Screenshot
    @Test
    fun qrCodeInfoFragmentScreenshot() {
        launchFragmentInContainer2<UqsInfoFragment>()
        takeScreenshot<UqsInfoFragment>()
    }
}
