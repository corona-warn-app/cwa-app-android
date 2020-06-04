package de.rki.coronawarnapp.http.responses

import com.google.gson.annotations.SerializedName

data class TestResultResponse(
    @SerializedName("testResult")
    val testResult: Int
)
