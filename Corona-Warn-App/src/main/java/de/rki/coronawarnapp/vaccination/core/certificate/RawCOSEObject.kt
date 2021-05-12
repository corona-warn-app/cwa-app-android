package de.rki.coronawarnapp.vaccination.core.certificate

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.inject.Inject

data class RawCOSEObject(
    val data: ByteString
) {
    constructor(data: ByteArray) : this(data.toByteString())

    val asByteArray: ByteArray
        get() = data.toByteArray()

    companion object {
        val EMPTY = RawCOSEObject(data = ByteString.EMPTY)
    }

    class JsonAdapter : TypeAdapter<RawCOSEObject>() {
        override fun write(out: JsonWriter, value: RawCOSEObject?) {
            if (value == null) out.nullValue()
            else value.data.base64().let { out.value(it) }
        }

        override fun read(reader: JsonReader): RawCOSEObject? = when (reader.peek()) {
            JSONObject.NULL -> reader.nextNull().let { null }
            else -> {
                val raw = reader.nextString()
                raw.decodeBase64()?.let { RawCOSEObject(data = it) }
                    ?: throw JsonParseException("Can't decode base64 ByteArray: $raw")
            }
        }
    }

    class RetroFitConverterFactory @Inject constructor() : Converter.Factory() {

        override fun requestBodyConverter(
            type: Type,
            parameterAnnotations: Array<out Annotation>,
            methodAnnotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<RawCOSEObject, RequestBody> {
            return ConverterToBody()
        }

        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<ResponseBody, RawCOSEObject> {
            return ConverterFromBody()
        }

        class ConverterFromBody : Converter<ResponseBody, RawCOSEObject> {
            override fun convert(value: ResponseBody): RawCOSEObject {
                val rawData = value.byteString()
                return RawCOSEObject(rawData)
            }
        }

        class ConverterToBody : Converter<RawCOSEObject, RequestBody> {
            override fun convert(value: RawCOSEObject): RequestBody {
                return value.data.toRequestBody(
                    "application/octet-stream".toMediaTypeOrNull()
                )
            }
        }
    }
}
