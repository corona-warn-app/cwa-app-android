package de.rki.coronawarnapp.ccl.ui.text

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import dagger.Reusable
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CCLJsonFunctions
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.getDefaultInputParameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CCLText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Parameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTimeDependentText
import de.rki.coronawarnapp.util.serialization.BaseJackson
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class CCLTextFormatter @Inject constructor(
    private val cclJsonFunctions: CCLJsonFunctions,
    @BaseJackson private val mapper: ObjectMapper
) {
    /**
     * Format [CCLText] based on its sub-types
     * if the text is a [SystemTimeDependentText] it will be evaluated by [CCLJsonFunctions]
     * @return [String] empty string if [CCLText] is null or could not be formatted
     */
    suspend operator fun invoke(
        cclText: CCLText?,
        locale: Locale = Locale.getDefault()
    ): String = runCatching {
        when (cclText) {
            is PluralText -> cclText.formatPlural(locale)
            is SingleText -> cclText.formatSingle(locale)
            is SystemTimeDependentText -> invoke(cclText.formatSystemTimeDependent())
            else -> null
        }
    }.getOrElse {
        Timber.w(it, "CCLText.format() failed")
        null
    }.orEmpty()

    /**
     * From a url from  FAQ anchor
     * @return url [String] null if anchor is nullable
     */
    operator fun invoke(
        faqAnchor: String?,
        locale: Locale = Locale.getDefault()
    ) = when {
        faqAnchor.isNullOrBlank() -> null
        else -> {
            val language = when (locale.language) {
                Locale.GERMAN.language -> locale.language
                else -> Locale.ENGLISH.language
            }
            "https://www.coronawarn.app/$language/faq/#$faqAnchor"
        }
    }

    private fun SingleText.formatSingle(
        locale: Locale
    ): String? {
        val text = localizedText[locale.language]
            ?: localizedText[EN] // Default for other languages
            ?: localizedText[DE] // Default for EN

        return text
            ?.replace("%@", "%s")
            ?.format(*parameters.convertValues(locale))
    }

    private fun PluralText.formatPlural(
        locale: Locale
    ): String? {
        val quantity = quantity()
        val quantityText = localizedText[locale.language]
            ?: localizedText[EN] // Default for other languages
            ?: localizedText[DE] // Default for EN
            ?: return null

        val text = pluralText(quantity, quantityText, locale)
        return text
            .replace("%@", "%s")
            .format(*parameters.convertValues(locale))
    }

    private fun PluralText.quantity(): Int = quantity ?: quantityFromIndex()

    private fun PluralText.quantityFromIndex(): Int {
        val param = parameters[quantityParameterIndex ?: 0]
        return when (param.type) {
            Parameters.Type.NUMBER -> param.toNumber()
            else -> error("`quantity` can't be derived from param=$param")
        }
    }

    private suspend fun SystemTimeDependentText.formatSystemTimeDependent(): CCLText? =
        runCatching {
            val defaultParameters = getDefaultInputParameters(DateTime.now()).toObjectNode()
            val allParameters = JsonNodeFactory.instance.objectNode()
                .setAll<ObjectNode>(defaultParameters)
                .setAll<ObjectNode>(parameters)
            val output = cclJsonFunctions.evaluateFunction(functionName, allParameters)
            mapper.treeToValue(output, CCLText::class.java)
        }.onFailure {
            Timber.e(it, "SystemTimeDependentText.formatSystemTimeDependent() failed.")
        }.getOrNull()

    private fun List<Parameters>.convertValues(locale: Locale): Array<Any> =
        map { parameter -> parameter.covertValue(locale) }.toTypedArray()

    private fun Parameters.covertValue(locale: Locale) = when (type) {
        Parameters.Type.STRING -> value.toString()
        Parameters.Type.NUMBER -> toNumber()
        Parameters.Type.BOOLEAN -> toBoolean()
        Parameters.Type.LOCAL_DATE -> toLocalDate(locale)
        Parameters.Type.LOCAL_DATE_TIME -> toLocalDateTime(locale)
        Parameters.Type.UTC_DATE -> toUTCDate(locale)
        Parameters.Type.UTC_DATE_TIME -> toUTCDateTime(locale)
    }

    private fun Parameters.toUTCDateTime(locale: Locale): String {
        return runCatching {
            DateTime.parse(value.toString()).run {
                "%s, %s".format(
                    toString(DateTimeFormat.shortDate().withZoneUTC().withLocale(locale)),
                    toString(DateTimeFormat.shortTime().withZoneUTC().withLocale(locale)),
                )
            }
        }.getOrElse {
            Timber.e(it, "Parameters.toUTCDateTime() failed")
            ""
        }
    }

    private fun Parameters.toUTCDate(locale: Locale): String {
        return runCatching {
            DateTime.parse(value.toString()).toString(
                DateTimeFormat.shortDate().withZoneUTC().withLocale(locale)
            )
        }.getOrElse {
            Timber.e(it, "Parameters.toUTCDate() failed")
            ""
        }
    }

    private fun Parameters.toLocalDateTime(locale: Locale): String {
        return runCatching {
            DateTime.parse(value.toString()).run {
                "%s, %s".format(
                    toString(DateTimeFormat.shortDate().withZone(DateTimeZone.getDefault()).withLocale(locale)),
                    toString(DateTimeFormat.shortTime().withZone(DateTimeZone.getDefault()).withLocale(locale)),
                )
            }
        }.getOrElse {
            Timber.e(it, "Parameters.toLocalDateTime() failed")
            ""
        }
    }

    private fun Parameters.toLocalDate(locale: Locale): String {
        return runCatching {
            DateTime.parse(value.toString()).toString(
                DateTimeFormat.shortDate().withZone(DateTimeZone.getDefault()).withLocale(locale)
            )
        }.getOrElse {
            Timber.e(it, "Parameters.toLocalDate() failed")
            ""
        }
    }

    private fun Parameters.toNumber(): Int = runCatching {
        (value as Number).toInt()
    }.getOrElse {
        Timber.e(it, "Parameters.toNumber() failed")
        0
    }

    private fun Parameters.toBoolean(): Boolean = runCatching {
        value as Boolean
    }.getOrElse {
        Timber.e(it, " Parameters.toBoolean() failed")
        false
    }

    private fun Any.toObjectNode(): ObjectNode = mapper.valueToTree(this)

    companion object {
        private const val EN = "en"
        private const val DE = "de"
    }
}
