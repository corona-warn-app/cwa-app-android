package de.rki.coronawarnapp.util.serialization.validation

interface JsonSchemaSource {

    /**
     * This may cause blocking IO
     */
    val rawSchema: String

    val version: Version

    enum class Version {
        V2019_09
    }
}
