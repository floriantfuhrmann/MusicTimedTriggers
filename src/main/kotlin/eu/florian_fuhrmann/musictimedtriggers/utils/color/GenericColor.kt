package eu.florian_fuhrmann.musictimedtriggers.utils.color

import com.godaddy.android.colorpicker.HsvColor
import com.google.gson.JsonObject

/**
 * Because there a multiple color types used in the project, this class
 * is used to convert between them. This object can also be used in
 * configurations.
 */
data class GenericColor(val red: Int, val green: Int, val blue: Int, val alpha: Int) {

    fun toAwtColor(): java.awt.Color {
        return java.awt.Color(red, green, blue, alpha)
    }

    fun toComposeColor(): androidx.compose.ui.graphics.Color {
        return androidx.compose.ui.graphics.Color(red, green, blue, alpha)
    }

    fun toHsvColor(): HsvColor {
        return HsvColor.from(toComposeColor())
    }

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("red", red)
        jsonObject.addProperty("green", green)
        jsonObject.addProperty("blue", blue)
        jsonObject.addProperty("alpha", alpha)
        return jsonObject
    }

    companion object {
        fun fromAtwColor(awtColor: java.awt.Color): GenericColor {
            return GenericColor(awtColor.red, awtColor.green, awtColor.blue, awtColor.alpha)
        }

        fun fromComposeColor(composeColor: androidx.compose.ui.graphics.Color): GenericColor {
            return GenericColor(
                (composeColor.red * 255).toInt(),
                (composeColor.green * 255).toInt(),
                (composeColor.blue * 255).toInt(),
                (composeColor.alpha * 255).toInt()
            )
        }

        fun fromHsvColor(hsvColor: HsvColor): GenericColor {
            return fromComposeColor(hsvColor.toColor())
        }

        fun fromAnyColor(colorObject: Any): GenericColor {
            return when (colorObject) {
                is java.awt.Color -> {
                    fromAtwColor(colorObject)
                }

                is androidx.compose.ui.graphics.Color -> {
                    fromComposeColor(colorObject)
                }

                is HsvColor -> {
                    fromHsvColor(colorObject)
                }

                is GenericColor -> {
                    colorObject
                }

                else -> {
                    throw IllegalArgumentException("Color type ${colorObject.javaClass.name} is not supported")
                }
            }
        }

        fun fromJson(jsonObject: JsonObject): GenericColor {
            return GenericColor(
                jsonObject.get("red").asInt,
                jsonObject.get("green").asInt,
                jsonObject.get("blue").asInt,
                jsonObject.get("alpha").asInt
            )
        }
    }

}