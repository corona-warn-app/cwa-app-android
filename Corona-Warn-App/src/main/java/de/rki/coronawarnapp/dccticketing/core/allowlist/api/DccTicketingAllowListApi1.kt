package de.rki.coronawarnapp.dccticketing.core.allowlist.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface DccTicketingAllowListApi1 {
    @GET(" /version/v1/ehn-dgc/validation-services")
    fun allowList(): Response<ResponseBody>
}
