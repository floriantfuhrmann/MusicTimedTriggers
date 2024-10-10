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

    // array contains from y coordinate for every sequence line (same index in this array as in TriggerSequence#lines)
    private var linesFromY: IntArray = IntArray(0)
    private var linesHeight: IntArray = IntArray(0)

    fun getSequenceLineFromY(lineIndex: Int) = linesFromY[lineIndex]
    fun getSequenceLineHeight(lineIndex: Int) = linesHeight[lineIndex]

    /**
     * Finds which Sequence Line is at the [y] coordinate by checking fromY for every line
     */
    fun getSequenceLineAt(y: Int): TriggerSequenceLine? {
        //get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return null
        //check in reverse order on which sequence line the y coordinate is
        for (i in linesFromY.indices.reversed()) {
            if(y >= linesFromY[i]) {
                return sequence.lines[i]
            }
        }
        //y is so small, that no sequence line matches
        return null
    }

    /**
     * Finds the index of the Sequence Line at the [y] coordinate by checking fromY for every line
     */
    fun getSequenceLineIndexAt(y: Int): Int? {
        //check in reverse order on which sequence line the y coordinate is
        for (i in linesFromY.indices.reversed()) {
            if(y >= linesFromY[i]) {
                return i
            }
        }
        //y is so small, that no sequence line matches
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
        //reset linesFromY array
        linesFromY = IntArray(sequence.lines.size)
        linesHeight = IntArray(sequence.lines.size)
        //calculate times
        val fromTime = TimelineBackgroundRenderer.xToTime(0)
        val toTime = TimelineBackgroundRenderer.xToTime(width)
        //calculate heights
        val heightOfSeparatorLines = sequence.lines.size - 1
        val heightForSequenceLines = height - heightOfSeparatorLines
        val heightPerSequenceLine = heightForSequenceLines.toDouble() / sequence.lines.size
        //draw separator lines only
        var currentY = y.toDouble()
        sequence.lines.forEachIndexed { index, _ ->
            //draw separator if not first line
            val separatorY = currentY.roundToInt()
            if(index != 0) {
                g.color = Color.white
                g.drawLine(0, separatorY, width, separatorY)
                currentY += 1 // add height of separator line
            }
            currentY += heightPerSequenceLine // add height of sequence line
        }
        //draw triggers and rest of sequence line on top
        currentY = y.toDouble()
        sequence.lines.forEachIndexed { index, line ->
            //calculate y coordinate of separator
            val separatorY = currentY.roundToInt()
            if(index != 0) {
                currentY += 1 // add height of separator line
            }
            //draw sequence line
            val fromY = if(index != 0) {
                separatorY + 1
            } else {
                separatorY
            }
            currentY += heightPerSequenceLine // add height of sequence line
            val toY = currentY.roundToInt() - 1
            val lineHeight = toY - fromY + 1
            drawSequenceLine(g, 0, fromY, width, lineHeight, fromTime, toTime, line)
            linesFromY[index] = fromY
            linesHeight[index] = lineHeight
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
        //draw sequence triggers
        var currentTriggerIndex = line.getIndexOfTriggerAtOrIndexOfTriggerAfter(fromTime)
        while (true) {
            //get trigger at index
            val trigger = line.getTriggerByIndex(currentTriggerIndex)
            //make sure the trigger exists and is still in bounds
            if(trigger == null || trigger.startTime >= toTime) break
            currentTriggerIndex++
            //calculate trigger x coordinates
            val triggerX1 = TimelineBackgroundRenderer.timeToX(trigger.startTime)
            val triggerX2 = TimelineBackgroundRenderer.timeToX(trigger.endTime)
            //draw that trigger
            drawTrigger(
                g,
                triggerX1,
                y,
                triggerX2 - triggerX1 + 1,
                height,
                trigger
            )
        }
        //draw sequence name / label
        RenderUtils.drawStringOnRect(
            g,
            0,
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

    private fun drawTrigger(
        g: Graphics2D,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        trigger: AbstractPlacedTrigger
    ) {
        drawTrigger(
            g,
            x,
            y,
            width,
            height,
            trigger.triggerTemplate.configuration.color,
            trigger.name(),
            if (TriggerSelectionManager.isVisuallySelected(trigger)) {
                TriggerStateStyle.Selected
            } else {
                TriggerStateStyle.Normal
            },
            MoveTriggersManager.isTriggerHovered(trigger),
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
        triggerColor: GenericColor,
        name: String,
        style: TriggerStateStyle = TriggerStateStyle.Normal,
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
        RenderUtils.drawStringVerticallyCentered(g, x + 3, y, height, name)
        g.clip = null
        // draw keyframes (only if trigger is hovered)
        if(hovered && keyframes != null) {
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
            g.color = if(MoveTriggersManager.isKeyframeHovered(it)) {
                Color.red
            } else {
                Color.yellow
            }
            g.fillPolygon(keyframeRhombus)
            // draw border
            g.color = Color.black
            val restoreStroke = g.stroke
            g.stroke = BasicStroke(1.5f)
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
        val halfHeight = (triggerHeight / 8.0).coerceIn(5.0, 7.0)
        val kfX = (triggerX + keyframe.position * triggerWidth)
        val kfY = (triggerY + (1 - keyframe.value) * triggerHeight)
        return Polygon(
            intArrayOf((kfX - halfHeight).roundToInt(), kfX.roundToInt(), (kfX + halfHeight).roundToInt(), kfX.roundToInt()),
            intArrayOf(kfY.roundToInt(), (kfY - halfHeight).roundToInt(), kfY.roundToInt(), (kfY + halfHeight).roundToInt()),
            4
        )
    }

}