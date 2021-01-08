package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.server.protocols.internal.v2.ExposureDetectionParameters
import org.joda.time.Duration

interface ExposureDetectionConfig {

    val maxExposureDetectionsPerUTCDay: Int
    val minTimeBetweenDetections: Duration
    val overallDetectionTimeout: Duration

    val exposureDetectionParameters: ExposureDetectionParameters.ExposureDetectionParametersAndroid?

    interface Mapper : ConfigMapper<ExposureDetectionConfig>
}
