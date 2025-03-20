package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.DebugOptions
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.Spectrogram
import java.awt.*
import kotlin.math.roundToInt


object TimelineBackgroundRenderer {

    // If double leads to too quirky behavior maybe consider BigDecimal instead (https://www.baeldung.com/java-bigdecimal-biginteger)

    // Zooming
    /**
     * @return The width of one second in pixels
     */
    var pixelsPerSecond: Int = Zooming.DEFAULT_PIXELS_PER_SECOND

    object Zooming {
        const val DEFAULT_PIXELS_PER_SECOND = 100 // Must be in options
        private val options = arrayOf(6, 9, 13, 20, 30, 44, 67, 100, 150, 225, 338, 506, 759, 1139)
        private var selectedIndex = options.indexOf(DEFAULT_PIXELS_PER_SECOND)
        fun zoomIn() {
            if (selectedIndex < options.lastIndex) {
                selectedIndex++
                pixelsPerSecond = options[selectedIndex]
                redrawTimeline()
            }
        }
        fun zoomOut() {
            if (selectedIndex > 0) {
                selectedIndex--
                pixelsPerSecond = options[selectedIndex]
                redrawTimeline()
            }
        }
    }

    // Conversion functions (also used by other renderers)
    fun timeToX(time: Double): Int {
        return TimelineRenderer.timelineCoreX + (((time - currentFromTime) / currentTotalDisplayedDuration) * TimelineRenderer.timelineCoreWidth).roundToInt()
    }
    fun xToTime(x: Int): Double {
        return currentFromTime + ((x - TimelineRenderer.timelineCoreX).toDouble() / pixelsPerSecond)
    }
    fun durationToWidth(duration: Double): Int {
        return ((duration / currentTotalDisplayedDuration) * TimelineRenderer.timelineCoreWidth).roundToInt()
    }
    fun widthToDuration(width: Int): Double {
        return (width.toDouble() / TimelineRenderer.timelineCoreWidth) * currentTotalDisplayedDuration
    }
    // Values needed for conversions (updated during rendering)
    var currentTotalDisplayedDuration = 0.0
        private set
    var currentFromTime = 0.0
        private set
    var currentToTime = 0.0
        private set

    /**
     * Renders the background (the spectrogram) of the timeline
     *
     * @param g Graphics object to draw on
     * @param spectrogram Spectrogram of the current song
     * @param middleTimePosition What time should be in the middle of the area
     *    drawn to (the displayed time window will be calculated around this
     *    time depending on the current zoom level)
     * @param totalDuration Total duration of the song/sequence
     * @param x X position of the area to draw on
     * @param y Y position of the area to draw on
     * @param width Width of the area to draw on
     * @param height Height of the area to draw on
     */
    fun render(
        g: Graphics,
        spectrogram: Spectrogram,
        middleTimePosition: Double,
        totalDuration: Double,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        // calculate fromTime and toTime from middle time position and dst pixels per second
        currentTotalDisplayedDuration = width / pixelsPerSecond.toDouble()
        currentFromTime = middleTimePosition - currentTotalDisplayedDuration / 2
        currentToTime = currentFromTime + currentTotalDisplayedDuration
        // render (if images are available)
        if(spectrogram.imagesAvailable) {
            render(g, spectrogram, totalDuration, currentFromTime, currentToTime, x, x + width, y, height, pixelsPerSecond)
        }
    }

    private fun render(
        g: Graphics,
        spectrogram: Spectrogram, //spectrogram of the current song
        totalDuration: Double, //total duration of the song/sequence
        fromTime: Double, //earliest time visible (time at the most left point / time at dstX1)
        toTime: Double, //latest time visible (time at the most right point / time at dstX2)
        dstX1: Int, //left destination x coordinate
        dstX2: Int, //right destination x coordinate
        yOffset: Int, //y offset at the top
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
        val srcImgTotalWidth = spectrogram.imagesTotalWidth!!
        val srcImgPixelsPerSecond = srcImgTotalWidth / totalDuration
        //calculate start and end x coordinates on source image
        val srcStartX = fromTime * srcImgPixelsPerSecond
        val srcEndX = toTime * srcImgPixelsPerSecond
        //adjust start and end x coordinates so they are in bonds
        if (srcStartX < 0) { //handle case where startX is < 0 (to the left of the left most point on the source image)
            //calculate overlap time (duration of period to the left of the left most point on source image -> duration
            // of the time which can't be drawn because it is out of bounds)
            val overlapTime: Double = (-srcStartX) / srcImgPixelsPerSecond
            require(overlapTime > 0) { "Calculated overlapTime (on the left) is not greater than zero (startX=$srcStartX, imgPixelsPerSecond=$srcImgPixelsPerSecond, overlapTime=$overlapTime)" }
            //recursively call render with adjusted from time and dstX1 coordinate
            render(
                g,
                spectrogram,
                totalDuration,
                0.0, // from time is 0 because we are starting at the beginning (should be same as fromTime + overlapTime)
                toTime,
                dstX1 + (overlapTime * dstPixelsPerSecond).roundToInt(),
                dstX2,
                yOffset,
                height,
                dstPixelsPerSecond
            )
        } else if (srcEndX > srcImgTotalWidth) { //handle case when endX > total width of source image
            //calculate overlap time (duration of time period to the right of the right most point)
            val overlapTime: Double = (srcEndX - srcImgTotalWidth + 1) / srcImgPixelsPerSecond
            require(toTime > totalDuration) { "srcEndX($srcEndX) >= srcImgTotalWidth($srcImgTotalWidth) ; toTime($toTime) <= totalDuration($totalDuration); overlapTime=$overlapTime. Why are we adjusting??" }
            require(overlapTime > 0) { "Calculated overlapTime (on the right) is not greater than zero (endX=$srcEndX, imgTotalWidth=$srcImgTotalWidth, imgPixelsPerSecond=$srcImgPixelsPerSecond, overlapTime=$overlapTime)" }
            //recursively call render with adjusted to time and dstX2 coordinate
            render(
                g,
                spectrogram,
                totalDuration,
                fromTime,
                totalDuration, // to time is total duration because we are drawing until the end (should be same as toTime - overlapTime)
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
                srcStartX.roundToInt(), // now it's also time to round
                srcEndX.roundToInt(),
                fromTime,
                toTime,
                dstX1,
                dstX2,
                yOffset,
                height,
                dstPixelsPerSecond,
                srcImgPixelsPerSecond
            )
            //draw debug box
            drawDebugTotalBox(g, dstX1, dstX2, yOffset, height)
        }
    }

    /**
     * This function assumes startX, endX, fromTime and toTime are within bounds
     */
    private fun render(
        g: Graphics,
        spectrogram: Spectrogram, //spectrogram of the current song
        totalDuration: Double, //total duration of the song
        srcStartX: Int, // x coordinate of the earliest time visible on the source image (will be mapped to destX1)
        srcEndX: Int, // x coordinate of the latest time visible on the source image (will be mapped to destX2)
        fromTime: Double, //earliest time visible (time at the most left point / time at dstX1)
        toTime: Double, //latest time visible (time at the most right point / time at dstX2)
        dstX1: Int, //left destination x coordinate
        dstX2: Int, //right destination x coordinate
        yOffset: Int = 0, //y offset at the top
        height: Int, //height of the destination content
        dstPixelsPerSecond: Int, //how many pixels should make up a second on the destination
        srcPixelsPerSecond: Double //how many pixels make up a second on the source image
    ) {
        //make sure values make some sense (so it should fail here if some prior calculations messed up)
        if(srcEndX < srcStartX) throw IllegalArgumentException("endX < startX (endX=$srcEndX, startX=$srcStartX)")
        if(toTime <= fromTime) throw IllegalArgumentException("toTime <= fromTime (toTime=$toTime, fromTime=$fromTime)")
        if(totalDuration <= 0) throw IllegalArgumentException("totalDuration <= 0 (totalDuration=$totalDuration)")
        if(toTime - fromTime > totalDuration) throw IllegalArgumentException("toTime - fromTime > totalDuration (toTime=$toTime, fromTime=$fromTime, totalDuration=$totalDuration)")
        if(height <= 0) throw IllegalArgumentException("height <= 0 (height=$height)")
        if(dstPixelsPerSecond <= 0) throw IllegalArgumentException("dstPixelsPerSecond <= 0 (dstPixelsPerSecond=$dstPixelsPerSecond)")
        if(dstX2 < dstX1) throw IllegalArgumentException("dstX2 < dstX1 (dstX2=$dstX2, dstX1=$dstX1)")
        //get first image part
        val currentImgPartIndex = (srcStartX / Spectrogram.MIN_IMAGE_WIDTH).coerceAtMost(spectrogram.imagesCount - 1)
        val currentImgPartBufferedImage = spectrogram.images[currentImgPartIndex]!!
        //calculate start and end x coords on first image
        val partStartX = srcStartX - currentImgPartIndex * Spectrogram.MIN_IMAGE_WIDTH
        val partEndX = srcEndX - currentImgPartIndex * Spectrogram.MIN_IMAGE_WIDTH
        //make sure part end x is in bounds (part start x is automatically in bonds because the image part is selected
        // depending on start x)
        if(partEndX > currentImgPartBufferedImage.width) {
            //Calculating from overlap time:
            //calculate overlap time
            val overlapTime: Double = (partEndX - currentImgPartBufferedImage.width) / srcPixelsPerSecond
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
            val newFromTime: Double = ((currentImgPartIndex + 1) * Spectrogram.MIN_IMAGE_WIDTH) / srcPixelsPerSecond
            render(
                g = g,
                spectrogram = spectrogram,
                totalDuration = totalDuration,
                //new start x coordinate is the absolute coordinate of the first pixel on the image after the first image
                srcStartX = (currentImgPartIndex + 1) * Spectrogram.MIN_IMAGE_WIDTH,
                srcEndX = srcEndX,
                fromTime = newFromTime, //old: fromTime + partUtilizedTime
                toTime = toTime,
                dstX1 = firstImageDstX2,
                dstX2 = dstX2,
                yOffset = yOffset,
                height = height,
                dstPixelsPerSecond = dstPixelsPerSecond,
                srcPixelsPerSecond = srcPixelsPerSecond
            )
        } else {
            //draw this image (first image is the only image which needs to be drawn)
            g.drawImage(
                /*img = */currentImgPartBufferedImage,
                /*dx1 = */dstX1,
                /*dy1 = */yOffset,
                /*dx2 = */dstX2,
                /*dy2 = */yOffset + height,
                /*sx1 = */partStartX,
                /*sy1 = */0,
                /*sx2 = */partEndX + 0, // would +1 be correct here?
                /*sy2 = */currentImgPartBufferedImage.height,
                null
            )
            //draw debug box
            drawDebugImgPartBox(g, currentImgPartIndex, partStartX, partEndX + 0, dstX1, dstX2, yOffset, height)
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
        if (!DebugOptions.IMAGE_PARTS) {
            return
        }
        //draw debug text
        RenderUtils.drawStringOnRect(g, destinationX1 + 1, destinationY,
            "part #$partIndex",
            Color.black, Color.white, 1)
        RenderUtils.drawStringOnRect(g, destinationX1 + 1, destinationY + 17,
            "src: $sourceX1 - $sourceX2 (width: ${sourceX2 - sourceX1})",
            Color.black, Color.white, 1)
        RenderUtils.drawStringOnRect(g, destinationX1 + 1, destinationY + 2 * 17,
            "dst: $destinationX1 - $destinationX2 (width: ${destinationX2 - destinationX1})",
            Color.black, Color.white, 1)
        val scaleX = (destinationX2 - destinationX1).toDouble() / (sourceX2 - sourceX1)
        RenderUtils.drawStringOnRect(g, destinationX1 + 1, destinationY + 3 * 17,
            "scaleX: $scaleX",
            Color.black, Color.white, 1)
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

    private fun drawDebugTotalBox(
        g: Graphics,
        destinationX1: Int,
        destinationX2: Int,
        destinationY: Int,
        destinationHeight: Int
    ) {
        if (!DebugOptions.IMAGE_PARTS) {
            return
        }
        //draw debug box
        g.color = Color.white
        g.drawRect(destinationX1 - 1, destinationY - 1, destinationX2 - destinationX1 + 2, destinationHeight + 2)
    }

}