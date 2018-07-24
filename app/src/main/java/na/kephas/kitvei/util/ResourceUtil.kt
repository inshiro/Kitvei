package na.kephas.kitvei.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.annotation.Nullable

object ResourceUtil {


    @Nullable
    fun getThemedAttribute(@NonNull context: Context, @AttrRes id: Int): TypedValue? {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(id, typedValue, true)) {
            typedValue
        } else null
    }
    @ColorInt
    fun getThemedColor(@NonNull context: Context, @AttrRes id: Int): Int {
        val typedValue = getThemedAttribute(context, id)
                ?: throw IllegalArgumentException("Attribute not found; ID=$id")
        return typedValue.data
    }

}