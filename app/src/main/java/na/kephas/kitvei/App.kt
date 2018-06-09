package na.kephas.kitvei

import android.app.Application
//import com.codemonkeylabs.fpslibrary.TinyDancer
import com.squareup.leakcanary.LeakCanary
import na.kephas.kitvei.data.Prefs

val prefs: Prefs by lazy { App.prefs }

class App : Application() {

    companion object {
        lateinit var prefs: Prefs
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(applicationContext)
        instance = this
        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) return
            LeakCanary.install(this)
            //TinyDancer.create().show(applicationContext)
        }
    }

}
