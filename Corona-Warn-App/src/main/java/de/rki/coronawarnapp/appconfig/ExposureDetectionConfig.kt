package de.rki.coronawarnapp.appconfig

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.server.protocols.internal.AppConfig.ApplicationConfiguration

interface ExposureDetectionConfig {

    val exposureDetectionConfiguration: ExposureConfiguration

    interface Mapper {
        fun map(rawConfig: ApplicationConfiguration): ExposureDetectionConfig
    }
}
