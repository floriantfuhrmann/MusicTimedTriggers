package eu.florian_fuhrmann.musictimedtriggers.utils.color

import androidx.compose.ui.graphics.Color

fun Color.mix(other: Color, proportion: Float): Color {
    return Color(
        alpha = alpha,
        red = proportion*red + (1-proportion)*other.red,
        green = proportion*green + (1-proportion)*other.green,
        blue = proportion*blue + (1-proportion)*other.blue,
    )
}

fun Color.getContrasting(lightColor: Color, darkColor: Color): Color {
    return if(red * 0.299 + green * 0.587 + blue * 0.114 > 0.7294117647058824) {
        darkColor
    } else {
        lightColor
    }
}
