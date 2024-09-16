package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import java.awt.Color
import java.awt.Graphics
import java.awt.Polygon
import kotlin.math.roundToInt

object TimelineGridRenderer {

    private const val TEXT_MARGIN = 2

    fun calculateSecondsGridHeight(g: Graphics): Int {
        return g.fontMetrics.ascent - g.fontMetrics.descent + 2 * TEXT_MARGIN
    }

    fun drawSecondGrid(
        g: Graphics,
        secondsGridHeight: Int, //height of the seconds grid at the top
        drawLinesFullHeight: Boolean = false,
        width: Int, //total width of the content drawn
        height: Int //total height of the content drawn
    ) {
        //draw background
        g.color = Color(192, 192, 192, 128)
        g.fillRect(0, 0, width, secondsGridHeight)
        //draw second grid
        g.color = Color.darkGray
        var second = TimelineBackgroundRenderer.xToTime(0).toInt()
        while (true) {
            val x = TimelineBackgroundRenderer.timeToX(second.toDouble())
            if(x > width) {
                break
            }
            g.drawLine(x, 0, x, if(drawLinesFullHeight) {height} else {secondsGridHeight})
            if(second >= 0) {
                g.drawString(
                    formattedSecondsString(second),
                    x + TEXT_MARGIN,
                    secondsGridHeight - TEXT_MARGIN
                )
            }
            second++
        }
    }

    fun drawPlayHead(
        g: Graphics,
        currentTimePosition: Double,
        secondsGridHeight: Int, //height of the seconds grid at the top
        width: Int, //total width of the content drawn
        height: Int //total height of the content drawn
    ) {
        //draw play head
        val playHeadX = (width / 2.0).roundToInt()
        g.color = Color.white
        g.drawLine(playHeadX, 0, playHeadX, height)
        g.fillPolygon(Polygon().apply {
            addPoint(playHeadX - 5, 0)
            addPoint(playHeadX + 5, 0)
            addPoint(playHeadX, 6)
        })
        g.drawString(
            formattedSecondsString(currentTimePosition.toInt()),
            playHeadX + TEXT_MARGIN,
            secondsGridHeight - TEXT_MARGIN
        )
    }

    private fun formattedSecondsString(seconds: Int): String {
        val minutes = seconds / 60
        val leftOverSeconds = seconds % 60
        return "${if(minutes<10){"0"}else{""}}$minutes:${if(leftOverSeconds<10){"0"}else{""}}$leftOverSeconds"
    }

}