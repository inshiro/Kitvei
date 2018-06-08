package na.kephas.kitvei.util

import android.os.Build

fun isM(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isN(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun isO(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isOMR1(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
fun isP(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

// Not totally safe to use yet
// https://issuetracker.google.com/issues/64550633
inline fun sdk(level: Int, func: () -> Unit) {
    if (Build.VERSION.SDK_INT >= level) func.invoke()
}
