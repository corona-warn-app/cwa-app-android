package de.rki.coronawarnapp.familytest.core.model

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest

data class FamilyCoronaTest(
    @SerializedName("personName")
    val personName: String,
    @SerializedName("coronaTest")
    val coronaTest: CoronaTest,
) : BaseCoronaTest by coronaTest
