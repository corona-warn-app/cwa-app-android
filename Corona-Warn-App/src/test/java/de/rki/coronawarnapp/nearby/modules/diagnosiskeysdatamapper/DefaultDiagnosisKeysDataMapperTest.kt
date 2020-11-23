package de.rki.coronawarnapp.nearby.modules.diagnosiskeysdatamapper

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import com.google.android.gms.nearby.exposurenotification.Infectiousness
import com.google.android.gms.nearby.exposurenotification.ReportType
import de.rki.coronawarnapp.nearby.modules.diagnosiskeysdatamapper.DefaultDiagnosisKeysDataMapper.Companion.hasChanged
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DefaultDiagnosisKeysDataMapperTest : BaseTest() {

    @Test
    fun `mapping change detection works`() {
        // Note that we cant create an empty mapping as the DiagnosisKeysDataMappingBuilder
        // throws a IllegalArgumentException if one of the properties is missing
        val nullMapping: DiagnosisKeysDataMapping? = null
        val firstMapping = DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder().apply {
            setReportTypeWhenMissing(ReportType.CONFIRMED_TEST)
            setInfectiousnessWhenDaysSinceOnsetMissing(Infectiousness.HIGH)
            setDaysSinceOnsetToInfectiousness(mapOf(0 to Infectiousness.STANDARD, 1 to Infectiousness.HIGH))
        }.build()
        val secondMapping = DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder().apply {
            setReportTypeWhenMissing(ReportType.CONFIRMED_TEST)
            setInfectiousnessWhenDaysSinceOnsetMissing(Infectiousness.HIGH)
            setDaysSinceOnsetToInfectiousness(mapOf(0 to Infectiousness.HIGH, 1 to Infectiousness.STANDARD))
        }.build()
        val thirdMapping = DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder().apply {
            setReportTypeWhenMissing(ReportType.CONFIRMED_TEST)
            setInfectiousnessWhenDaysSinceOnsetMissing(Infectiousness.HIGH)
            setDaysSinceOnsetToInfectiousness(mapOf())
        }.build()
        val fourthMapping = DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder().apply {
            setReportTypeWhenMissing(ReportType.CONFIRMED_TEST)
            setInfectiousnessWhenDaysSinceOnsetMissing(Infectiousness.HIGH)
            setDaysSinceOnsetToInfectiousness(mapOf())
        }.build()

        firstMapping.hasChanged(nullMapping) shouldBe true
        firstMapping.hasChanged(secondMapping) shouldBe true
        firstMapping.hasChanged(thirdMapping) shouldBe true

        secondMapping.hasChanged(nullMapping) shouldBe true
        secondMapping.hasChanged(firstMapping) shouldBe true
        secondMapping.hasChanged(thirdMapping) shouldBe true

        thirdMapping.hasChanged(nullMapping) shouldBe true
        thirdMapping.hasChanged(firstMapping) shouldBe true
        thirdMapping.hasChanged(secondMapping) shouldBe true

        nullMapping.hasChanged(nullMapping) shouldBe true
        firstMapping.hasChanged(firstMapping) shouldBe false
        secondMapping.hasChanged(secondMapping) shouldBe false
        thirdMapping.hasChanged(thirdMapping) shouldBe false
        thirdMapping.hasChanged(fourthMapping) shouldBe false
    }
}