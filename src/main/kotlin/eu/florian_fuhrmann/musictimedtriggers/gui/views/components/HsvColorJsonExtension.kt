package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import com.godaddy.android.colorpicker.HsvColor
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

fun HsvColor.Companion.fromJson(jsonObject: JsonObject): HsvColor {
    return HsvColor(
        jsonObject.get("hue").asFloat,
        jsonObject.get("saturation").asFloat,
        jsonObject.get("value").asFloat,
        jsonObject.get("alpha").asFloat,
    )
}

fun HsvColor.toJson(): JsonObject {
    val json = JsonObject()
    json.add("hue", JsonPrimitive(hue))
    json.add("saturation", JsonPrimitive(saturation))
    json.add("value", JsonPrimitive(value))
    json.add("alpha", JsonPrimitive(alpha))
    return json
}
