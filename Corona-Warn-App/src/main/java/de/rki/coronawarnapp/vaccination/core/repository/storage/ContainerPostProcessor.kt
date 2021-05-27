package de.rki.coronawarnapp.vaccination.core.repository.storage

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationQRCodeExtractor
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@Reusable
class ContainerPostProcessor @Inject constructor(
    private val qrCodeExtractor: VaccinationQRCodeExtractor,
) : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
        val delegate = gson.getDelegateAdapter(this, type)

        return object : TypeAdapter<T>() {

            override fun write(output: JsonWriter, value: T) = delegate.write(output, value)

            @Throws(IOException::class)
            override fun read(input: JsonReader): T {
                val obj = delegate.read(input)

                when (obj) {
                    is VaccinationContainer -> {
                        Timber.v("Injecting VaccinationContainer %s", obj.hashCode())
                        obj.qrCodeExtractor = qrCodeExtractor
                    }
                }

                return obj
            }
        }
    }
}
