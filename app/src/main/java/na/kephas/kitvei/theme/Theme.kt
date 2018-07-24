package na.kephas.kitvei.theme

import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import na.kephas.kitvei.R
import na.kephas.kitvei.model.EnumCode


enum class Theme constructor(marshallingId: Int, funnelName: String, @StyleRes resourceId: Int, @StringRes nameId: Int) : EnumCode {

    LIGHT(0, "light", R.style.ThemeLight, R.string.color_theme_light),
    DARK(1, "dark", R.style.ThemeDark, R.string.color_theme_dark),
    BLACK(2, "black", R.style.ThemeBlack, R.string.color_theme_black);

    var marshallingId: Int = 0

    @get:NonNull
    var funnelName: String

    @StyleRes
    @get:StyleRes
    var resourceId: Int = 0

    @StringRes
    @get:StringRes
    var nameId: Int = 0

    val fallback: Theme
        get() = LIGHT


    val isDefault: Boolean
        get() = this == fallback

    val isDark: Boolean
        get() = (this == DARK || this == BLACK)

    override fun code(): Int {
        return marshallingId
    }

    init {
        this.marshallingId = marshallingId
        this.funnelName = funnelName
        this.resourceId = resourceId
        this.nameId = nameId
    }

    companion object {
        fun ofMarshallingId(id: Int): Theme? {
            for (theme in values())
                if (theme.marshallingId == id)
                    return theme
            return null
        }
    }
}