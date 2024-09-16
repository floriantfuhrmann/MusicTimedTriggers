package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import java.awt.Color
import java.awt.Graphics
import kotlin.math.roundToInt

object RenderUtils {

    fun drawStringOnRect(
        g: Graphics,
        x: Int,
        y: Int,
        str: String,
        backgroundColor: Color,
        textColor: Color,
        padding: Int = 0
    ) {
        drawStringOnRect(g, x, y, str, backgroundColor, textColor, padding, padding, padding, padding)
    }

    fun drawStringOnRect(
        g: Graphics,
        x: Int,
        y: Int,
        str: String,
        backgroundColor: Color,
        textColor: Color,
        paddingLeft: Int = 0,
        paddingTop: Int = 0,
        paddingRight: Int = 0,
        paddingBottom: Int = 0
    ) {
        val bounds = g.fontMetrics.getStringBounds(str, g)
        g.color = backgroundColor
        g.fillRect(
            x,
            y,
            bounds.width.roundToInt() + paddingLeft + paddingRight,
            bounds.height.roundToInt() + paddingTop + paddingBottom
        )
        g.color = textColor
        g.drawString(str, x + paddingLeft, y + g.fontMetrics.ascent + paddingTop)
    }

    fun drawStringVerticallyCentered(
        g: Graphics,
        x: Int,
        y: Int,
        height: Int,
        str: String
    ) {
        val bounds = g.fontMetrics.getStringBounds(str, g)
        g.drawString(str, x, y + ((height - bounds.height) / 2 + g.fontMetrics.ascent).roundToInt())
    }

}