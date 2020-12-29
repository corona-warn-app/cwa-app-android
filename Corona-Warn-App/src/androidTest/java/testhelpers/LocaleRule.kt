package testhelpers

import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.test.platform.app.InstrumentationRegistry

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Locale

// TODO Check if it is working on different Android versions
class LocaleRule(private val locales: Array<Locale>) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val deviceLocale = Locale.getDefault()
                try {
                    for (locale in locales) {
                        setLocale(locale)
                        base.evaluate()
                    }
                } finally {
                    setLocale(deviceLocale)
                }
            }
        }
    }

    private fun setLocale(locale: Locale) {
        val resources: Resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        Locale.setDefault(locale)
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        resources.updateConfiguration(config, displayMetrics)
    }
}
