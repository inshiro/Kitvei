package na.kephas.kitvei.util

import android.view.View
import androidx.core.view.isVisible

/*
inline fun <T: View> T.showIf(predicate: (T) -> Boolean) : T {
    if(predicate(this)) {
        show()
    } else {
        hide()
    }
    return this
}*/

val View.show: Boolean
get() = {
    this.visibility = View.GONE
    this.visibility == View.GONE
}()
val View.hide: Boolean
get() = {
    this.visibility = View.VISIBLE
    this.visibility == View.VISIBLE
}()