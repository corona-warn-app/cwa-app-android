package de.rki.coronawarnapp.covidcertificate.common.certificate

import android.content.res.AssetManager
import io.kotest.matchers.string.shouldContain
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccJsonSchemaTest : BaseTest() {

    @MockK lateinit var assetManager: AssetManager

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { assetManager.open(any()) } answers {
            this.javaClass.classLoader!!.getResourceAsStream(arg<String>(0))
        }
    }

    fun createInstance() = DccJsonSchema(assetManager)

    @Test
    fun `test asset reading`() {
        createInstance().apply {
            rawSchema shouldContain """
                "title": "EU DGC"
            """.trimIndent()
        }
    }
}
