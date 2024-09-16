package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.utils

import com.godaddy.android.colorpicker.HsvColor

data class ConfigurationColor(val red: Int, val green: Int, val blue: Int, val alpha: Int) {

    fun toAwtColor(): java.awt.Color {
        return java.awt.Color(red, green, blue, alpha)
    }
    fun toComposeColor(): androidx.compose.ui.graphics.Color {
        return androidx.compose.ui.graphics.Color(red, green, blue, alpha)
    }
    fun toHsvColor(): HsvColor {
        return HsvColor.from(toComposeColor())
    }

    companion object {
        fun fromAtwColor(awtColor: java.awt.Color): ConfigurationColor {
            return ConfigurationColor(awtColor.red, awtColor.green, awtColor.blue, awtColor.alpha)
        }
        fun fromComposeColor(composeColor: androidx.compose.ui.graphics.Color): ConfigurationColor {
            return ConfigurationColor(
                (composeColor.red * 255).toInt(),
                (composeColor.green * 255).toInt(),
                (composeColor.blue * 255).toInt(),
                (composeColor.alpha * 255).toInt()
            )
        }
        fun fromHsvColor(hsvColor: HsvColor): ConfigurationColor {
            return fromComposeColor(hsvColor.toColor())
        }
        fun fromAnyColor(colorObject: Any): ConfigurationColor {
            return when (colorObject) {
                is java.awt.Color -> { fromAtwColor(colorObject) }
                is androidx.compose.ui.graphics.Color -> { fromComposeColor(colorObject) }
                is HsvColor -> { fromHsvColor(colorObject) }
                is ConfigurationColor -> { colorObject }
                else -> {
                    throw IllegalArgumentException("Color type ${colorObject.javaClass.name} is not supported")
                }
            }
        }
    }

}