package de.rki.coronawarnapp.appconfig

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.server.protocols.internal.AppConfig.ApplicationConfiguration
import de.rki.coronawarnapp.server.protocols.internal.ExposureDetectionParameters

interface ExposureDetectionConfig {

    val exposureDetectionConfiguration: ExposureConfiguration
    val exposureDetectionParameters: ExposureDetectionParameters.ExposureDetectionParametersAndroid

    interface Mapper {
        fun map(rawConfig: ApplicationConfiguration): ExposureDetectionConfig
    }
}
