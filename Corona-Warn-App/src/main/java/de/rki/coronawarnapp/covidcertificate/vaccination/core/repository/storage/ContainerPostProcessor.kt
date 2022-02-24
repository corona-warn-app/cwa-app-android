package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

// TODO: delete
@Reusable
class ContainerPostProcessor @Inject constructor(
    private val vaccinationQrCodeExtractor: DccQrCodeExtractor,
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
                    }
                }

                return obj
            }
        }
    }
}
