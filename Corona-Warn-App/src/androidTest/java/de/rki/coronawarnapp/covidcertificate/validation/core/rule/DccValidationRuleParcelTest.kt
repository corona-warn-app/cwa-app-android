package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import android.os.Bundle
import android.os.Parcel
import io.kotest.matchers.shouldBe
import org.junit.Test
import testhelpers.BaseUITest

class DccValidationRuleParcelTest : BaseUITest() {

    @Test
    fun parcelization() {
        val dccValidationRule = DccValidationRuleTest().createOne()

        val bundle = Bundle().apply {
            putParcelable("dccValidationRule", dccValidationRule)
        }

        val parcelRaw = Parcel.obtain().apply {
            writeBundle(bundle)
        }.marshall()

        val restoredParcel = Parcel.obtain().apply {
            unmarshall(parcelRaw, 0, parcelRaw.size)
            setDataPosition(0)
        }

        val restoredData = restoredParcel.readBundle()!!.run {
            classLoader = DccValidationRule::class.java.classLoader
            getParcelable<DccValidationRule>("dccValidationRule")
        }
        restoredData shouldBe dccValidationRule
    }
}
