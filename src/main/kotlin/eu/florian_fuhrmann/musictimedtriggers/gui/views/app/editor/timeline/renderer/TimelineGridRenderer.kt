package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import java.awt.Color
import java.awt.Graphics
import java.awt.Polygon

object TimelineGridRenderer {

    private const val TEXT_MARGIN = 2

    fun calculateSecondsGridHeight(g: Graphics): Int {
        // for some reason ascent - descent is the height of the font, then we just add margin on top and bottom
        return g.fontMetrics.ascent - g.fontMetrics.descent + 2 * TEXT_MARGIN
    }

    /**
     * Draws the seconds grid at the top of the content
     * @param g Graphics object to draw on
     * @param x x position of the content
     * @param y y position of the content
     * @param width total width of the content drawn
     * @param height total height of the content drawn
     * @param secondsGridHeight height of the seconds grid at the top
     * @param drawLinesFullHeight if true the lines will be drawn from top to bottom, otherwise only to the secondsGridHeight
     */
    fun drawSecondGrid(
        g: Graphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        secondsGridHeight: Int,
        drawLinesFullHeight: Boolean = false
    ) {
        //draw background
        g.color = Color(192, 192, 192, 128)
        g.fillRect(x, y, width, secondsGridHeight)
        //draw second grid
        g.color = Color.darkGray
        // calculate first second by rounding down from the time at x
        var second = TimelineBackgroundRenderer.xToTime(x).toInt()
        while (true) {
            // calculate x position of the second
            val secondX = TimelineBackgroundRenderer.timeToX(second.toDouble())
            // break if the second is out of bounds
            if(secondX > x + width) {
                break
            }
            // draw line
            g.drawLine(secondX, y, secondX, y + if(drawLinesFullHeight) {height} else {secondsGridHeight})
            // draw text (for positive seconds)
            if(second >= 0) {
                g.drawString(
                    formattedSecondsString(second),
                    secondX + TEXT_MARGIN,
                    y + secondsGridHeight - TEXT_MARGIN
                )
            }
            second++
        }
    }

    /**
     * Draws the play head at the current time position
     * @param g Graphics object to draw on
     * @param x x position of the content
     * @param y y position of the content
     * @param width total width of the content drawn
     * @param height total height of the content drawn
     * @param timePosition current time position in seconds
     * @param secondsGridHeight height of the seconds grid at the top
     */
    fun drawPlayHead(
        g: Graphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        timePosition: Double,
        secondsGridHeight: Int
    ) {
        //draw play head
        val playHeadX = TimelineBackgroundRenderer.timeToX(timePosition)
        g.color = Color.white
        g.drawLine(playHeadX, y, playHeadX, y + height)
        g.fillPolygon(Polygon().apply {
            addPoint(playHeadX - 5, y)
            addPoint(playHeadX + 5, y)
            addPoint(playHeadX, y + 6)
        })
        g.drawString(
            formattedSecondsString(timePosition.toInt()),
            playHeadX + TEXT_MARGIN,
            y + secondsGridHeight - TEXT_MARGIN
        )
    }

    private fun formattedSecondsString(seconds: Int): String {
        val minutes = seconds / 60
        val leftOverSeconds = seconds % 60
        return "${if(minutes<10){"0"}else{""}}$minutes:${if(leftOverSeconds<10){"0"}else{""}}$leftOverSeconds"
    }

}