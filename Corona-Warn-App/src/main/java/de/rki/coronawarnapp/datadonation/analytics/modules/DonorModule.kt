package de.rki.coronawarnapp.datadonation.analytics.modules

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData

interface DonorModule {

    suspend fun beginDonation(request: Request): Contribution

    suspend fun deleteData()

    /**
     * Data that the modules may need to fullfil the request
     */
    interface Request {
        /**
         * The config data pulled at the start of the submission attempt.
         * Should be used by modules to prevent unnecessary config refreshes,
         * and to prevent the config from changing DURING the collection/submission.
         */
        val currentConfig: ConfigData
    }

    /**
     * An object that adds the data to the protobuf container, such that the Analytics class doesn't need to know the
     * individual data types.
     * This also acts as a callback so the donor modules know when to discard data.
     */
    interface Contribution {
        /**
         * You will be passed a protobuf container where the module will add it's data
         */
        suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder)

        /**
         * This will be called with the submission result.
         * For modules that track a data delta between each submission time:
         * If the submission failed, i.e. network issues, the donor module may collect their data for the next attempt.
         * If the submission was sucessful, the module may discard the data as it was submitted.
         */
        suspend fun finishDonation(successful: Boolean)
    }
}
