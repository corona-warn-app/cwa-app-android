package de.rki.coronawarnapp.covidcertificate.signature.core.server

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.covidcertificate.signature.DSCHttpClient
import de.rki.coronawarnapp.environment.covidcertificate.signature.DSCServerUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

@Module
class DscServerModule {

    /**
     * TODO: is this necessary?
     * Handles DSC server 202 "retry later" response with 0-byte bodies.
     */
    private val nullConverter = object : Converter.Factory() {
        fun factoryRef() = this
        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ) = object : Converter<ResponseBody, Any?> {
            val nextConverter = retrofit.nextResponseBodyConverter<Any?>(factoryRef(), type, annotations)

            override fun convert(value: ResponseBody): Any? {
                return if (value.contentLength() != 0L) nextConverter.convert(value) else null
            }
        }
    }

    @Reusable
    @Provides
    fun apiV1(
        @DSCHttpClient httpClient: OkHttpClient,
        @DSCServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory
    ): DscApiV1 = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(url)
        .addConverterFactory(nullConverter)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(DscApiV1::class.java)
}
