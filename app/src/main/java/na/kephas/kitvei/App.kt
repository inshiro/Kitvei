package na.kephas.kitvei

import android.app.Application
//import com.codemonkeylabs.fpslibrary.TinyDancer
//import com.squareup.leakcanary.LeakCanary
import na.kephas.kitvei.data.Prefs
import na.kephas.kitvei.theme.Theme

val Prefs: Prefs by lazy { App.prefs }

class App : Application() {

    companion object {
        lateinit var prefs: Prefs
        lateinit var instance: App
        lateinit var currentTheme: Theme
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(applicationContext)
        instance = this
        currentTheme = enumValues<Theme>()[0]
        /*if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) return
            LeakCanary.install(this)
            TinyDancer.create().show(applicationContext)
        }*/
    }

    fun getCurrentTheme(): Theme {
        return currentTheme
    }

    /**
     * Sets the theme of the app. If the new theme is the same as the current theme, nothing happens.
     * Otherwise, an event is sent to notify of the theme change.
     */
    fun setCurrentTheme(theme: Theme) {
        if (theme !== currentTheme) {
            currentTheme = theme
            Prefs.themeId = currentTheme.marshallingId
            //bus.post(ThemeChangeEvent())
        }
    }
    fun setFontSizeMultiplier(multiplier: Int): Boolean {
        var multiplier = multiplier
        val minMultiplier = resources.getInteger(R.integer.minTextSizeMultiplier)
        val maxMultiplier = resources.getInteger(R.integer.maxTextSizeMultiplier)
        if (multiplier < minMultiplier) {
            multiplier = minMultiplier
        } else if (multiplier > maxMultiplier) {
            multiplier = maxMultiplier
        }
        if (multiplier.toFloat() != Prefs.textSizeMultiplier) {
            Prefs.textSizeMultiplier = multiplier.toFloat()
            //bus.post(ChangeTextSizeEvent())
            return true
        }
        return false
    }
}
