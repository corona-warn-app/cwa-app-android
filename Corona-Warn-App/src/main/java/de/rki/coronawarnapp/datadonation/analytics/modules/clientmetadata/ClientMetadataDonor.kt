package de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata

import android.os.Build.VERSION.SDK_INT
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientMetadataDonor @Inject constructor(
    private val clientVersionParser: ClientVersionParser,
    private val appConfigProvider: AppConfigProvider,
    private val enfClient: ENFClient
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val config = appConfigProvider.getAppConfig()

        val version = clientVersionParser.parseClientVersion(BuildConfigWrap.VERSION_CODE.toInt())

        val clientMetadataBuilder = PpaData.PPAClientMetadataAndroid.newBuilder()
            .setCwaVersion(version.toPPASemanticVersion())
            .setAndroidApiLevel(SDK_INT.toLong())
            .setAppConfigETag(config.identifier)

        enfClient.getENFClientVersion()?.let {
            clientMetadataBuilder.setEnfVersion(it)
        }

        return ClientMetadataContribution(
            contributionProto = clientMetadataBuilder.build()
        )
    }

    override suspend fun deleteData() {
        // Nothing to be deleted
    }

    data class ClientMetadataContribution(
        val contributionProto: PpaData.PPAClientMetadataAndroid
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            protobufContainer.clientMetadata = contributionProto
        }

        override suspend fun finishDonation(successful: Boolean) {
            // No post processing needed for User Metadata
        }
    }
}
