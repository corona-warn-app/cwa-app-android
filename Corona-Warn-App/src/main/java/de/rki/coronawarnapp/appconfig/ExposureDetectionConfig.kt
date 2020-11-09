package de.rki.coronawarnapp.appconfig

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.server.protocols.internal.ExposureDetectionParameters
import org.joda.time.Duration

interface ExposureDetectionConfig {

    val maxExposureDetectionsPerDay: Int
    val minTimeBetweenDetections: Duration
    val overAllDetectionTimeout: Duration

    val exposureDetectionConfiguration: ExposureConfiguration
    val exposureDetectionParameters: ExposureDetectionParameters.ExposureDetectionParametersAndroid

    interface Mapper : ConfigMapper<ExposureDetectionConfig>
}
