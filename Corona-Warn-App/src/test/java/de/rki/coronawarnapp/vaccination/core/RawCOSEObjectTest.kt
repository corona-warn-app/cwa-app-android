package de.rki.coronawarnapp.vaccination.core

import com.google.gson.GsonBuilder
import de.rki.coronawarnapp.util.serialization.fromJson
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RawCOSEObjectTest : BaseTest() {

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `comparison and conversion`() {
        val rawRaw = "The Cake Is A Lie!".toByteArray()
        val rawRaw2 = "The Cake Is Not A Lie!".toByteArray() // This is a lie
        val rawCOSEObject1 = RawCOSEObject(rawRaw)
        val rawCOSEObject2 = RawCOSEObject(rawRaw2)

        rawRaw shouldNotBe rawRaw2
        rawCOSEObject1 shouldNotBe rawCOSEObject2

        rawCOSEObject1.asByteArray shouldBe rawRaw
        rawCOSEObject2.asByteArray shouldBe rawRaw2
    }

    @Test
    fun `serialization and deserialization`() {
        val rawRaw = "The Cake Is A Lie!".toByteArray()
        val rawRaw2 = "The Cake Is Not A Lie!".toByteArray() // This is a lie
        val rawCOSEObject1 = RawCOSEObject(rawRaw)
        val rawCOSEObject2 = RawCOSEObject(rawRaw2)

        val gson = GsonBuilder().apply {
            registerTypeAdapter(RawCOSEObject::class.java, RawCOSEObject.JsonAdapter())
        }.create()

        val json1 = gson.toJson(rawCOSEObject1)
        json1 shouldBe "\"VGhlIENha2UgSXMgQSBMaWUh\""
        gson.fromJson<RawCOSEObject>(json1) shouldBe rawCOSEObject1

        val json2 = gson.toJson(rawCOSEObject2)
        json2 shouldBe "\"VGhlIENha2UgSXMgTm90IEEgTGllIQ\\u003d\\u003d\""
        gson.fromJson<RawCOSEObject>(json2) shouldBe rawCOSEObject2
    }
}
