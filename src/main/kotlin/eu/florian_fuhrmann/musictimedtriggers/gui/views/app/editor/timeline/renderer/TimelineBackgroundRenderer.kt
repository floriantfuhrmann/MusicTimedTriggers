package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.Spectrogram
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import kotlin.math.roundToInt

object TimelineBackgroundRenderer {

    private const val DEBUG_IMAGE_PARTS = false

    // If double leads to too quirky behavior maybe consider BigDecimal instead (https://www.baeldung.com/java-bigdecimal-biginteger)

    private val destinationPixelsPerSecondOptions = arrayOf(6, 9, 13, 20, 30, 44, 67, 100, 150, 225, 338, 506, 759, 1139)
    private var destinationPixelsPerSecondSelectedOptionIndex = 7
    val destinationPixelsPerSecond
        get() = destinationPixelsPerSecondOptions[destinationPixelsPerSecondSelectedOptionIndex]

    fun zoomIn() {
        if(destinationPixelsPerSecondSelectedOptionIndex < destinationPixelsPerSecondOptions.size - 1) {
            destinationPixelsPerSecondSelectedOptionIndex++
            redrawTimeline()
        }
    }
    fun zoomOut() {
        if(destinationPixelsPerSecondSelectedOptionIndex > 0) {
            destinationPixelsPerSecondSelectedOptionIndex--
            redrawTimeline()
        }
    }

    fun timeToX(time: Double): Int {
        return (((time - currentFromTime) / currentTotalDisplayedDuration) * currentWidth).roundToInt()
    }
    fun xToTime(x: Int): Double {
        return currentFromTime + (x.toDouble() / destinationPixelsPerSecond)
    }
    fun durationToWidth(duration: Double): Int {
        return ((duration / currentTotalDisplayedDuration) * currentWidth).roundToInt()
    }
    fun widthToDuration(width: Int): Double {
        return (width.toDouble() / currentWidth) * currentTotalDisplayedDuration
    }

    private var currentTotalDisplayedDuration = 0.0
    private var currentFromTime = 0.0
    private var currentToTime = 0.0
    private var currentWidth = 0
    fun render(
        g: Graphics,
        spectrogram: Spectrogram, //spectrogram of the current song
        currentTimePosition: Double, //current time position in song
        totalDuration: Double, //total duration of the song
        x: Int = 0, //x offset
        y: Int = 0, //y offset
        width: Int, //destination width of the content drawn
        height: Int //destination height of the content drawn
    ) {
        //set currentWidth
        currentWidth = width
        //get destination pixels per second
        val dstPixelsPerSecond: Int = destinationPixelsPerSecond
        //calculate fromTime and toTime from current time position and dst pixels per second
        currentTotalDisplayedDuration = width / dstPixelsPerSecond.toDouble()
        currentFromTime = currentTimePosition - currentTotalDisplayedDuration / 2
        currentToTime = currentFromTime + currentTotalDisplayedDuration
        //render (if images are available)
        if(spectrogram.imagesAvailable) {
            render(g, spectrogram, totalDuration, currentFromTime, currentToTime, x, width - x, y, height, dstPixelsPerSecond)
        }
    }

    private fun render(
        g: Graphics,
        spectrogram: Spectrogram, //spectrogram of the current song
        totalDuration: Double, //total duration of the song
        fromTime: Double, //earliest time visible (time at the most left point / time at dstX1)
        toTime: Double, //latest time visible (time at the most right point / time at dstX2)
        dstX1: Int, //left destination x coordinate
        dstX2: Int, //right destination x coordinate
        yOffset: Int = 0, //y offset at the top
        height: Int, //height of the destination content
        dstPixelsPerSecond: Int //how many pixels should make up a second on the destination
    ) {
        //make sure input makes some sense
        if(toTime <= fromTime) throw IllegalArgumentException("toTime <= fromTime (toTime=$toTime, fromTime=$fromTime)")
        if(totalDuration <= 0) throw IllegalArgumentException("totalDuration <= 0 (totalDuration=$totalDuration)")
        if(height <= 0) throw IllegalArgumentException("height <= 0 (height=$height)")
        if(dstPixelsPerSecond <= 0) throw IllegalArgumentException("dstPixelsPerSecond <= 0 (dstPixelsPerSecond=$dstPixelsPerSecond)")
        if(dstX2 < dstX1) throw IllegalArgumentException("dstX2 < dstX1 (dstX2=$dstX2, dstX1=$dstX1)")
        //get source image total width and calculate source image pixels per second
        val imgTotalWidth = spectrogram.imagesTotalWidth!!
        val imgPixelsPerSecond = imgTotalWidth / totalDuration
        //calculate start and end x coordinates on source image
        val startX: Int = (fromTime * imgPixelsPerSecond).roundToInt()
        val endX: Int = (toTime * imgPixelsPerSecond).roundToInt()
        //adjust start and end x coordinates so they are in bonds
        if (startX < 0) { //handle case where startX is < 0 (to the left of the left most point on the source image)
            //calculate overlap time (duration of period to the left of the left most point on source image -> duration
            // of the time which can't be drawn because it is out of bounds)
            val overlapTime: Double = (-startX) / imgPixelsPerSecond
            if (overlapTime <= 0) throw IllegalStateException("Calculated overlapTime (on the left) is not greater than zero (startX=$startX, imgPixelsPerSecond=$imgPixelsPerSecond, overlapTime=$overlapTime)")
            //recursively call render with adjusted from time and dstX1 coordinate
            render(
                g,
                spectrogram,
                totalDuration,
                fromTime + overlapTime,
                toTime,
                dstX1 + (overlapTime * dstPixelsPerSecond).roundToInt(),
                dstX2,
                yOffset,
                height,
                dstPixelsPerSecond
            )
        } else if (endX >= imgTotalWidth) { //handle case when endX > total width of source image
            //calculate overlap time (duration of time period to the right of the right most point)
            val overlapTime: Double = (endX - imgTotalWidth + 1) / imgPixelsPerSecond
            if (overlapTime <= 0) throw IllegalStateException("Calculated overlapTime (on the right) is not greater than zero (endX=$endX, imgTotalWidth=$imgTotalWidth, imgPixelsPerSecond=$imgPixelsPerSecond, overlapTime=$overlapTime)")
            //recursively call render with adjusted to time and dstX2 coordinate
            render(
                g,
                spectrogram,
                totalDuration,
                fromTime,
                toTime - overlapTime,
                dstX1,
                dstX2 - (overlapTime * dstPixelsPerSecond).roundToInt(),
                yOffset,
                height,
                dstPixelsPerSecond
            )
        } else {
            //when this is reached startX and endX are within bounds
            //check from and to time
            if(toTime - fromTime > totalDuration) throw IllegalArgumentException("toTime - fromTime > totalDuration (toTime=$toTime, fromTime=$fromTime, totalDuration=$totalDuration)")
            //call render function with startX and endX
            render(
                g,
                spectrogram,
                totalDuration,
                startX,
                endX,
                fromTime,
                toTime,
                dstX1,
                dstX2,
                yOffset,
                height,
                dstPixelsPerSecond,
                imgPixelsPerSecond
            )
        }
    }

    /**
     * This function assumes startX, endX, fromTime and toTime are within bounds
     */
    private fun render(
        g: Graphics,
        spectrogram: Spectrogram, //spectrogram of the current song
        totalDuration: Double, //total duration of the song
        startX: Int,
        endX: Int,
        fromTime: Double, //earliest time visible (time at the most left point / time at dstX1)
        toTime: Double, //latest time visible (time at the most right point / time at dstX2)
        dstX1: Int, //left destination x coordinate
        dstX2: Int, //right destination x coordinate
        yOffset: Int = 0, //y offset at the top
        height: Int, //height of the destination content
        dstPixelsPerSecond: Int, //how many pixels should make up a second on the destination
        imgPixelsPerSecond: Double //how many pixels make up a second on the source image
    ) {
        //make sure values make some sense (so it should fail here if some prior calculations messed up)
        if(endX < startX) throw IllegalArgumentException("endX < startX (endX=$endX, startX=$startX)")
        if(toTime <= fromTime) throw IllegalArgumentException("toTime <= fromTime (toTime=$toTime, fromTime=$fromTime)")
        if(totalDuration <= 0) throw IllegalArgumentException("totalDuration <= 0 (totalDuration=$totalDuration)")
        if(toTime - fromTime > totalDuration) throw IllegalArgumentException("toTime - fromTime > totalDuration (toTime=$toTime, fromTime=$fromTime, totalDuration=$totalDuration)")
        if(height <= 0) throw IllegalArgumentException("height <= 0 (height=$height)")
        if(dstPixelsPerSecond <= 0) throw IllegalArgumentException("dstPixelsPerSecond <= 0 (dstPixelsPerSecond=$dstPixelsPerSecond)")
        if(dstX2 < dstX1) throw IllegalArgumentException("dstX2 < dstX1 (dstX2=$dstX2, dstX1=$dstX1)")
        //get first image part
        val currentImgPartIndex = (startX / Spectrogram.MIN_IMAGE_WIDTH).coerceAtMost(spectrogram.imagesCount - 1)
        val currentImgPartBufferedImage = spectrogram.images[currentImgPartIndex]!!
        //calculate start and end x coords on first image
        val partStartX = startX - currentImgPartIndex * Spectrogram.MIN_IMAGE_WIDTH
        val partEndX = endX - currentImgPartIndex * Spectrogram.MIN_IMAGE_WIDTH
        //make sure part end x is in bounds (part start x is automatically in bonds because the image part is selected
        // depending on start x)
        if(partEndX > currentImgPartBufferedImage.width) {
            //Calculating from overlap time:
            //calculate overlap time
            val overlapTime: Double = (partEndX - currentImgPartBufferedImage.width) / imgPixelsPerSecond
            val firstImageDstX2 = dstX2 - (overlapTime * dstPixelsPerSecond).roundToInt()
            //draw first image
            g.drawImage(
                currentImgPartBufferedImage,
                dstX1, // dx1
                yOffset, // dy1
                firstImageDstX2, // dx2
                yOffset + height, // dy2
                partStartX, // sx1
                0, // sy1
                currentImgPartBufferedImage.width, // sx2
                currentImgPartBufferedImage.height, // sy2
                null
            )
            //draw debug box
            drawDebugImgPartBox(g, currentImgPartIndex, partStartX, currentImgPartBufferedImage.width, dstX1, firstImageDstX2, yOffset, height)
            //recursively call render to draw the rest of the spectrogram with the next images
            //calculate new from time by calculating the time position of the first pixel of the next image
            val newFromTime: Double = ((currentImgPartIndex + 1) * Spectrogram.MIN_IMAGE_WIDTH) / imgPixelsPerSecond
            render(
                g = g,
                spectrogram = spectrogram,
                totalDuration = totalDuration,
                //new start x coordinate is the absolute coordinate of the first pixel on the image after the first image
                startX = (currentImgPartIndex + 1) * Spectrogram.MIN_IMAGE_WIDTH,
                endX = endX,
                fromTime = newFromTime, //old: fromTime + partUtilizedTime
                toTime = toTime,
                dstX1 = firstImageDstX2,
                dstX2 = dstX2,
                yOffset = yOffset,
                height = height,
                dstPixelsPerSecond = dstPixelsPerSecond,
                imgPixelsPerSecond = imgPixelsPerSecond
            )
        } else {
            //draw this image (first image is the only image which needs to be drawn)
            g.drawImage(
                currentImgPartBufferedImage,
                dstX1,
                yOffset,
                dstX2,
                yOffset + height,
                partStartX,
                0,
                partEndX + 1, // is +1 really correct here? couldn't that be out of bounds?
                currentImgPartBufferedImage.height,
                null
            )
            //draw debug box
            drawDebugImgPartBox(g, currentImgPartIndex, partStartX, partEndX + 1, dstX1, dstX2, yOffset, height)
        }
    }

    private fun drawDebugImgPartBox(
        g: Graphics,
        partIndex: Int,
        sourceX1: Int,
        sourceX2: Int,
        destinationX1: Int,
        destinationX2: Int,
        destinationY: Int,
        destinationHeight: Int
    ) {
        if (!DEBUG_IMAGE_PARTS) {
            return
        }
        //draw debug text
        RenderUtils.drawStringOnRect(
            g,
            destinationX1 + 1,
            destinationY,
            "source part #$partIndex (from $sourceX1 to $sourceX2) rendered from $destinationX1 to $destinationX2",
            Color.black,
            Color.white,
            1
        )
        //draw debug box
        g.color = when (partIndex % 3) {
            0 -> Color.red
            1 -> Color.green
            2 -> Color.blue
            else -> throw IllegalStateException("any number mod 3 should not result in any number other then 0, 1 or 2")
        }
        val g2d = g as Graphics2D
        val restoreStroke = g2d.stroke
        g2d.stroke = BasicStroke(2.5f)
        g.drawRect(destinationX1 + 1, destinationY, destinationX2 - destinationX1 - 2, destinationHeight)
        g.stroke = restoreStroke
    }

}