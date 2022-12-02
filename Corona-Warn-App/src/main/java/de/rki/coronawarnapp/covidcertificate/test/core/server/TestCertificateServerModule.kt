package de.rki.coronawarnapp.covidcertificate.test.core.server

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.covidcertificate.DCCHttpClient
import de.rki.coronawarnapp.environment.covidcertificate.DCCServerUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.reflect.Type

@Module
class TestCertificateServerModule {

    /**
     * Handles DCC server 202 "retry later" response with 0-byte bodies.
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
        @DCCHttpClient httpClient: OkHttpClient,
        @DCCServerUrl url: String,
        jacksonConverterFactory: JacksonConverterFactory
    ): TestCertificateApiV1 = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(url)
        .addConverterFactory(nullConverter)
        .addConverterFactory(jacksonConverterFactory)
        .build()
        .create(TestCertificateApiV1::class.java)
}
