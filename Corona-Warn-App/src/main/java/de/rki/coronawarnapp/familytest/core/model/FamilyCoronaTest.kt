package de.rki.coronawarnapp.familytest.core.model

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.type.CoronaTest

data class FamilyCoronaTest(
    @SerializedName("personName")
    val personName: String,
    @SerializedName("coronaTest")
    val coronaTest: BaseCoronaTest,
) : CoronaTest by coronaTest
