package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.MoveTriggersManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.TriggerSelectionManager
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedIntensityTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequence
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequenceLine
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import java.awt.*
import kotlin.math.roundToInt

object TimelineSequenceRenderer {

    // Values needed for conversions (updated during rendering)
    // arrays contain top y coordinates and heights of every sequence line (same index in this array as in TriggerSequence#lines)
    private var lineTopYs: Array<Int?> = Array(0) { null } // must be ascending
    private var lineHeights: Array<Int?> = Array(0) { null }

    fun getSequenceLineTopY(lineIndex: Int) = lineTopYs[lineIndex]
    fun getSequenceLineHeight(lineIndex: Int) = lineHeights[lineIndex]

    /**
     * Finds which Sequence Line is at the [y] coordinate
     */
    fun getSequenceLineAt(y: Int): TriggerSequenceLine? {
        // get current sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return null
        // return the line at the index
        return sequence.lines[getSequenceLineIndexAt(y) ?: return null]
    }

    /**
     * Finds the index of the Sequence Line at the [y] coordinate
     */
    fun getSequenceLineIndexAt(y: Int): Int? {
        // check for every line if y is in bounds
        for (i in lineTopYs.indices) {
            val topY = lineTopYs[i] ?: continue
            val height = lineHeights[i] ?: continue
            if(y >= topY && y <= topY + height) {
                return i
            }
        }
        // y is out of bounds
        return null
    }

    fun drawSequence(
        g: Graphics2D,
        x: Int,
        y: Int,
        width: Int, //total width of the content drawn
        height: Int, //total height of the content drawn
        sequence: TriggerSequence
    ) {
        // reset line topYs and heights
        lineTopYs = Array(sequence.lines.size) { null }
        lineHeights = Array(sequence.lines.size) { null }
        // calculate from and to time
        val fromTime = TimelineBackgroundRenderer.xToTime(x)
        val toTime = TimelineBackgroundRenderer.xToTime(x + width)
        // calculate heights
        val heightOfSeparatorLines = sequence.lines.size - 1 // one separator line between each sequence line
        val heightForAllSequenceLines = height - heightOfSeparatorLines // total height of all sequence lines
        val heightPerSequenceLine = heightForAllSequenceLines.toDouble() / sequence.lines.size // height of one sequence line (excluding separators) (not an integer!)
        // first draw separator lines only (so they appear bellow the placed triggers)
        g.color = Color.white
        var currentY = y.toDouble()
        sequence.lines.forEachIndexed { index, _ ->
            // draw separator (if not first line)
            if(index != 0) {
                val separatorY = currentY.roundToInt()
                g.drawLine(x, separatorY, x + width, separatorY)
                currentY += 1.0 // add height of separator line
            }
            // add (average) height of sequence line
            currentY += heightPerSequenceLine
        }
        // draw placed triggers and rest of sequence line above
        currentY = y.toDouble() // reset y
        sequence.lines.forEachIndexed { index, line ->
            // add height of separator line (if this is not the first line)
            if(index != 0) {
                currentY += 1.0
            }
            val topY = currentY.roundToInt()
            currentY += heightPerSequenceLine // add (average) height of sequence line
            val bottomY = currentY.roundToInt() - 1
            val lineHeight = bottomY - topY + 1
            drawSequenceLine(g, x, topY, width, lineHeight, fromTime, toTime, line)
            // save top y and height
            lineTopYs[index] = topY
            lineHeights[index] = lineHeight
        }
    }

    private fun drawSequenceLine(
        g: Graphics2D,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        fromTime: Double,
        toTime: Double,
        line: TriggerSequenceLine
    ) {
        // draw sequence triggers
        var currentTriggerIndex = line.getIndexOfTriggerAtOrIndexOfTriggerAfter(fromTime)
        while (true) {
            // get trigger at index
            val trigger = line.getTriggerByIndex(currentTriggerIndex)
            // make sure the trigger exists and is still in bounds
            if(trigger == null || trigger.startTime >= toTime) break
            currentTriggerIndex++
            // calculate trigger x coordinates
            val triggerX1 = TimelineBackgroundRenderer.timeToX(trigger.startTime)
            val triggerX2 = TimelineBackgroundRenderer.timeToX(trigger.endTime)
            // draw that trigger
            drawTrigger(g, triggerX1, y, triggerX2 - triggerX1 + 1, height, x, width, trigger)
        }
        //draw sequence name / label
        if(line.name.isNotEmpty()) {
            RenderUtils.drawStringOnRect(
                g,
                x,
                y,
                line.name,
                Color(0, 0, 0, 128),
                Color.white,
                paddingLeft = 3,
                paddingTop = 0,
                paddingRight = 3,
                paddingBottom = 0
            )
        }
    }

    private fun drawTrigger(
        g: Graphics2D,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        lineX: Int,
        lineWidth: Int,
        trigger: AbstractPlacedTrigger
    ) {
        drawTrigger(g,
            x, y,
            width, height,
            lineX, lineWidth,
            trigger.triggerTemplate.configuration.color,
            trigger.name(),
            if (TriggerSelectionManager.isVisuallySelected(trigger)) {
                TriggerStateStyle.Selected
            } else {
                TriggerStateStyle.Normal
            },
            MoveTriggersManager.isTriggerHovered(trigger),
            TriggerSelectionManager.isVisuallySelected(trigger),
            if (trigger is AbstractPlacedIntensityTrigger) {
                trigger.keyframes()
            } else {
                null
            }
        )
    }

    enum class TriggerStateStyle(val overrideBorderColor: Color?, val overrideBorderWidth: Float?) {
        Normal(null, null),
        Selected(Color.white, 5f),
        // Active(?, ?), // future
        GhostValid(Color.green, null),
        GhostInvalid(Color.red, null)
    }

    fun drawTrigger(
        g: Graphics2D,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        lineX: Int?,
        lineWidth: Int?,
        triggerColor: GenericColor,
        name: String,
        style: TriggerStateStyle = TriggerStateStyle.Normal,
        selected: Boolean = false,
        hovered: Boolean = false,
        keyframes: Keyframes? = null
    ) {
        // choose best text color
        val textColor = if(triggerColor.red * 0.299 + triggerColor.green * 0.587 + triggerColor.blue * 0.114 > 186) {
            Color.black
        } else {
            Color.white
        }
        // get background color
        val backgroundColor = Color(
            triggerColor.red, triggerColor.green, triggerColor.blue, if (keyframes != null) {
                if (hovered) {
                    92
                } else {
                    64
                }
            } else {
                if (hovered) {
                    224
                } else {
                    192
                }
            }
        )
        // get border color and width
        val borderColor = style.overrideBorderColor ?: Color(triggerColor.red, triggerColor.green, triggerColor.blue, 255)
        val borderWidth = style.overrideBorderWidth ?: if(hovered) { 2.5f } else { 1f }
        // get arc diameter
        val arcDiameter = if (keyframes != null) {
            1
        } else {
            (height / 2).coerceAtMost(16)
        }
        // fill background
        g.color = backgroundColor
        g.fillRoundRect(x, y, width - 1, height - 1, arcDiameter, arcDiameter)
        // draw intensity
        if(keyframes != null) {
            val intensityPoly = getTriggerIntensityPolygone(g, x, y, width, height, keyframes)
            g.color = Color(triggerColor.red, triggerColor.green, triggerColor.blue, 128)
            g.fillPolygon(intensityPoly)
            g.color = Color(triggerColor.red, triggerColor.green, triggerColor.blue, 255)
            g.drawPolygon(intensityPoly)
        }
        // draw border
        g.color = borderColor
        val restoreStroke = g.stroke
        g.stroke = BasicStroke(borderWidth)
        g.drawRoundRect(x, y, width - 1, height - 1, arcDiameter, arcDiameter)
        g.stroke = restoreStroke
        // draw trigger name
        g.color = textColor
        g.setClip(x, y, width, height)
        var nameX = x + 3
        if (lineX != null && nameX < lineX + 3) {
            nameX = lineX + 3
        }
        RenderUtils.drawStringVerticallyCentered(g, nameX, y, height, name)
        g.clip = null
        // draw keyframes (only if trigger is hovered or visually selected)
        if((hovered || selected) && keyframes != null) {
            drawKeyframes(g, x, y, width, height, keyframes)
        }
    }

    private fun getTriggerIntensityPolygone(
        g: Graphics2D,
        x: Int,
        y: Int,
        width: Int,
        height: Int, keyframes: Keyframes
    ): Polygon {
        val polygon = Polygon()
        polygon.addPoint(x, y + height - 1)
        keyframes.keyframesList.forEach {
            val keyframeX = (x + it.position * (width - 1)).roundToInt()
            val keyframeY = (y + (1 - it.value) * (height - 1)).roundToInt()
            polygon.addPoint(keyframeX, keyframeY)
        }
        polygon.addPoint(x + width - 1, y + height - 1)
        return polygon
    }

    private fun drawKeyframes(
        g: Graphics2D,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        keyframes: Keyframes
    ) {
        keyframes.keyframesList.forEach {
            val keyframeRhombus = getKeyframeShape(x, y, width, height, it)
            val hovered = MoveTriggersManager.isKeyframeHovered(it)
            val selected = TriggerSelectionManager.isVisuallySelected(it)
            g.color = if(hovered || selected) { Color.red } else { Color.yellow }
            g.fillPolygon(keyframeRhombus)
            // draw border
            g.color = if (selected) { Color.white } else { Color.black }
            val restoreStroke = g.stroke
            g.stroke = BasicStroke(if (selected) { 2.5f } else { 1.5f })
            g.drawPolygon(keyframeRhombus)
            g.stroke = restoreStroke
        }
    }

    fun getKeyframeShape(
        triggerX: Int,
        triggerY: Int,
        triggerWidth: Int,
        triggerHeight: Int,
        keyframe: Keyframes.Keyframe
    ): Polygon {
        val halfHeight = (triggerHeight / 8.0).roundToInt().coerceIn(5, 7)
        val kfX = (triggerX + keyframe.position * triggerWidth).roundToInt()
        val kfY = (triggerY + (1 - keyframe.value) * triggerHeight).roundToInt()
        return Polygon(
            intArrayOf(kfX - halfHeight, kfX, kfX + halfHeight, kfX),
            intArrayOf(kfY, kfY - halfHeight, kfY, kfY + halfHeight),
            4
        )
    }

}