package na.kephas.kitvei.util

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
fun AppCompatActivity.isTranslucentNavBar(): Boolean {
    val flags = window.attributes.flags
    if ((flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) == WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        return true
    return false
}

fun AppCompatActivity.setTranslucentNavBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }
}

fun AppCompatActivity.cancelTranslucentNavBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }
}
@TargetApi(Build.VERSION_CODES.M)
inline fun <reified T> Context.getSystemService(): T {
    if (isM()) {
        //return systemService<T>()!! //this as T
        // return getSystemService()
        return getSystemService<T>()!!
    } else {
        return when (T::class) {
            android.view.WindowManager::class -> Context.WINDOW_SERVICE
            android.view.LayoutInflater::class -> Context.LAYOUT_INFLATER_SERVICE
            android.app.ActivityManager::class -> Context.ACTIVITY_SERVICE
            android.os.PowerManager::class -> Context.POWER_SERVICE
            android.app.AlarmManager::class -> Context.ALARM_SERVICE
            android.app.NotificationManager::class -> Context.NOTIFICATION_SERVICE
            android.app.KeyguardManager::class -> Context.KEYGUARD_SERVICE
            android.location.LocationManager::class -> Context.LOCATION_SERVICE
            android.app.SearchManager::class -> Context.SEARCH_SERVICE
            android.os.Vibrator::class -> Context.VIBRATOR_SERVICE
            android.net.ConnectivityManager::class -> Context.CONNECTIVITY_SERVICE
            android.net.wifi.WifiManager::class -> Context.WINDOW_SERVICE
            android.media.AudioManager::class -> Context.AUDIO_SERVICE
            android.media.MediaRouter::class -> Context.MEDIA_ROUTER_SERVICE
            android.telephony.TelephonyManager::class -> Context.TELEPHONY_SERVICE
            android.telephony.SubscriptionManager::class -> Context.TELEPHONY_SUBSCRIPTION_SERVICE
            android.view.inputmethod.InputMethodManager::class -> Context.INPUT_METHOD_SERVICE
            android.app.UiModeManager::class -> Context.UI_MODE_SERVICE
            android.app.DownloadManager::class -> Context.DOWNLOAD_SERVICE
            android.os.BatteryManager::class -> Context.BATTERY_SERVICE
            android.app.job.JobScheduler::class -> Context.JOB_SCHEDULER_SERVICE
            android.app.usage.NetworkStatsManager::class -> Context.NETWORK_STATS_SERVICE
            else -> throw UnsupportedOperationException("Unsupported service: ${T::class.java}")
        }.let { getSystemService(it) as T }
    }
}