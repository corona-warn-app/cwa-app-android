package de.rki.coronawarnapp

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import de.rki.coronawarnapp.http.ServiceFactory
import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object MainModule {

    @Singleton
    @Provides
    fun serviceFactory(): ServiceFactory = ServiceFactory()

    @Provides
    fun submissionService(serviceFactory: ServiceFactory): SubmissionService =
        serviceFactory.submissionService()

    @Provides
    fun verificationService(serviceFactory: ServiceFactory): VerificationService =
        serviceFactory.verificationService()

    @Provides
    fun distributionService(serviceFactory: ServiceFactory): DistributionService =
        serviceFactory.distributionService()
}
