package eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import eu.florian_fuhrmann.musictimedtriggers.utils.fft.Fft
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON
import eu.florian_fuhrmann.musictimedtriggers.utils.hash.sha256
import java.awt.image.BufferedImage
import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.math.*

fun calculateSpectrogramDimensions(audioFile: File, spectrogramParameters: SpectrogramParameters): SpectrogramDimensions {
//    //return fallback dimensions if the file is not in correct encoding
//    if(!isPcmEncoding(audioFile)) {
//        return SpectrogramDimensions(-1, -1)
//    }
    //calculate dimensions by doing calculations stolen from #calculateSpectrogramData()
    val audioInputStream = AudioSystem.getAudioInputStream(audioFile)
    val audioFormat = audioInputStream.format
    val framesCount = audioInputStream.frameLength
    audioInputStream.close()
    val windowSize = when (spectrogramParameters.calculateWindowSizeFromDuration) {
        true -> highestPowerOf2NotGreaterThan((audioFormat.sampleRate * spectrogramParameters.windowDurationInSeconds).toInt())
        else -> spectrogramParameters.windowSize
    }
    val windowStep = windowSize / spectrogramParameters.overlapFactor
    val nX = (framesCount - windowSize) / windowStep
    val nY = windowSize / 2 + 1
    return SpectrogramDimensions(nX.toInt(), nY)
}

/**
 * Generates spectrogram data for the audio file using the passed parameters
 * @return 2d array (outer array: time, inner array: frequency intensity (values 0.0 - 1.0) (highest at index 0))
 */
fun calculateSpectrogramData(audioFile: File, spectrogramParameters: SpectrogramParameters): Array<DoubleArray> {
    println("[Spectrogram Generation] Starting for ${audioFile.name} (parameters: $spectrogramParameters)...")
    // Part 1: Get Samples from Audio File
    //open audio input stream
    val audioInputStream = AudioSystem.getAudioInputStream(audioFile)
    //get audio format
    val audioFormat = audioInputStream.format
    //read bytes
    val rawData = audioInputStream.readAllBytes()
    //get some info about the format
    val numberOfChannels = audioFormat.channels
    val frameSizeInBytes = audioFormat.frameSize
    val sampleSizeInBytes = audioFormat.sampleSizeInBits / 8
    val isBigEndian = audioFormat.isBigEndian
    //calculate total amount of frames
    val framesCount = rawData.size / frameSizeInBytes
    println("[Spectrogram Generation] Frames Count: $framesCount")
    //read samples for every channel
    //init samples 2d array
    val samples = Array(numberOfChannels) {
        IntArray(framesCount) { 0 }
    }
    //read input stream into samples
    for (sampleIndex in 0..<framesCount) { //loop over every frame...
        for (channelIndex in 0..<numberOfChannels) { //for every channel... (so every sample)
            //calculate start index for this sample
            val startIndex = sampleIndex * frameSizeInBytes + channelIndex * sampleSizeInBytes
            //read sample
            val sampleBytes = rawData.copyOfRange(startIndex, startIndex + sampleSizeInBytes)
            //convert sample to int and save in array
            samples[channelIndex][sampleIndex] = bytesToInt(sampleBytes, isBigEndian)
        }
    }

    // Part 2: Use FFT to create spectrogram data (Most info from: https://stackoverflow.com/questions/39295589/creating-spectrogram-from-wav-using-fft-in-java)
    //calculate window size
    val windowSize = when (spectrogramParameters.calculateWindowSizeFromDuration) {
        true -> highestPowerOf2NotGreaterThan((audioFormat.sampleRate * spectrogramParameters.windowDurationInSeconds).toInt())
        else -> spectrogramParameters.windowSize
    }
    println("[Spectrogram Generation] Using windowSize=$windowSize samples (${windowSize / audioFormat.sampleRate} seconds)")
    //calculate window step
    val overlapFactor = spectrogramParameters.overlapFactor // overlap factor (how many windows overlap, greater -> wider image)
    val windowStep = windowSize / overlapFactor
    println("[Spectrogram Generation] Using overlapFactor=$overlapFactor -> windowStep=$windowStep")
    //init spectrogramData
    val nX = (framesCount - windowSize) / windowStep
    val nY = windowSize / 2 + 1
    println("[Spectrogram Generation] Using: nX=$nX  nY=$nY")
    var spectrogramData = Array(nX) { DoubleArray(nY) }
    //generate window
    val windowFunctionComputedValues: DoubleArray = when (spectrogramParameters.useHammingWindow) {
        true -> calculateHammingWindow(windowSize) //if hamming window should be used -> calculate it
        else -> DoubleArray(windowSize) { 1.0 } //else fill window with 1
    }
    //apply FFT and find MAX and MIN amplitudes
    var maxAmp = Double.MIN_VALUE
    var minAmp = Double.MAX_VALUE
    val ampThreshold = 1.0
    for (x in 0..<nX) {
        //calculate interval start index
        val startIndex = x * windowStep
        //extract samples for this interval
        val real = DoubleArray(windowSize) {
            (samples[0][startIndex + it] * windowFunctionComputedValues[it]) //always using channel 0 at the moment
        }
        val imag = DoubleArray(windowSize)
        //fft
        Fft.inverseTransform(real, imag)
        //for every frequency
        for (y in 0..<nY) {
            val ampSquare = real[y] * real[y] + imag[y] * imag[y]
            // limit values and convert to dB
            val value = 10 * log10(ampSquare.coerceAtLeast(ampThreshold))
            spectrogramData[x][nY - y - 1] = value
            //update minAmp and maxAmp
            if (value > maxAmp) {
                maxAmp = value
            }
            if (value < minAmp) {
                minAmp = value
            }
        }
    }
    //Normalization
    //limit maximum range (min will be adjusted, max will be kept)
    if (maxAmp - minAmp > spectrogramParameters.maxRange) {
        minAmp = maxAmp - spectrogramParameters.maxRange
    }
    val diff = maxAmp - minAmp
    println("[Spectrogram Generation] Normalization: minAmp=$minAmp  maxAmp=$maxAmp  diff=$diff")
    for (x in 0 until nX) {
        for (y in 0 until nY) {
            val data = spectrogramData[x][y]
            if (data < minAmp) {
                spectrogramData[x][y] = 0.0
            } else {
                spectrogramData[x][y] = ((data - minAmp) / diff)
            }
        }
    }
    //Convert to log10 y-axis
    if (spectrogramParameters.log10YAxis) {
        println("[Spectrogram Generation] Converting to log10 y-axis")
        val maxLog = log10((nY - 1).toDouble())
        val stretchMultiplier = (nY - 1) / maxLog
        println("[Spectrogram Generation] maximum value of log10: $maxLog  => stretchMultiplier=$stretchMultiplier")
        //factor to multiply y-axis length by
        val logNY = (nY * spectrogramParameters.log10YAxisLengthFactor).toInt()
        val logSpectrogramData = Array(nX) { DoubleArray(logNY) }
        for (x in 0 until nX) {
            for (y in 0 until logNY) {
                val lookupY = (log10(y / spectrogramParameters.log10YAxisLengthFactor) * stretchMultiplier).toInt()
                    .coerceAtLeast(0)
                logSpectrogramData[x][y] = spectrogramData[x][lookupY]
            }
        }
        spectrogramData = logSpectrogramData
    }
    //return spectrogramData
    return spectrogramData
}

fun createImagesFromSpectrogramData(
    spectrogramData: Array<DoubleArray>,
    minImageWidth: Int? = null
): List<BufferedImage> {
    //find width and height
    val width = spectrogramData.size
    val height = spectrogramData[0].size
    //init result
    val result: MutableList<BufferedImage> = mutableListOf()
    //create images
    var currentStartX = 0
    while (true) {
        //calculate end X for this image
        var endX: Int
        if (minImageWidth == null) {
            endX = width
        } else {
            endX = currentStartX + minImageWidth
            //calculate remaining width
            val remainingWidth = width - endX
            //if remaining is smaller than min width take remaining onto the current image
            if (remainingWidth < minImageWidth) {
                endX = width
            }
        }
        // ( Here starts a part which could be done in parallel )
        //generate image
        //init image
        val imagePartWidth = endX - currentStartX
        val image = BufferedImage(imagePartWidth, height, BufferedImage.TYPE_INT_RGB)
        //set pixel colors
        for (x in currentStartX until endX) {
            for (y in 0 until height) {
                //get data value for this coordinate
                var data = spectrogramData[x][y]
                // data pow 4 (data = data*data*data*data)
                data *= data
                data *= data
                //set pixel color
                val c: Int = (data * 255).toInt()
                image.setRGB(x-currentStartX, y, java.awt.Color(c, c, c).rgb)
            }
        }
        //add image to result
        result.add(image)
        // ( Here ends the part which could be done in parallel )
        //check if this was the last image
        if (endX == width) {
            break // if so break the while loop
        }
        //update start x to this parts end x
        currentStartX = endX
    }
    //return result
    return result
}

/**
 * Converts the byte array to an Int either interpreting the bytes as big or little endian
 */
private fun bytesToInt(bytes: ByteArray, isBigEndian: Boolean): Int {
    if (isBigEndian) {
        //get sign
        val sign = bytes.first() < 0 //true when sign bit is set
        //fill result with sign bit
        var result = if(sign) { -1 } else { 0 }
        //init mask
        val mask = 0x000000FF
        for (i in bytes.indices) {
            //shift left to make space for the next byte
            result = result shl 8
            //write bits of current byte into last 8 bits
            result = result or (bytes[i].toInt() and mask)
        }
        //return result
        return result
    } else {
        //just call as big endian with reversed array
        return bytesToInt(bytes.reversedArray(), true)
    }
}

/**
 * Finds the highest power of 2 that is still not greater than the target
 */
private fun highestPowerOf2NotGreaterThan(target: Int): Int {
    var current = 1
    while (current * 2 < target) {
        current *= 2
    }
    return current
}

private fun calculateHammingWindow(length: Int): DoubleArray {
    val a = 0.54
    val oneMinusA = 1 - a
    return DoubleArray(length) { n ->
        a - oneMinusA * cos(((2 * PI) / length) * n)
    }
}

data class SpectrogramParameters(
    // True when the window size should be calculated from sample rate and time duration (windowLengthInSeconds) instead
    // of using a static value
    @Configurable("Calculate Window Size from Duration", "When enabled the window size is calculated from the duration instead of using a set value")
    var calculateWindowSizeFromDuration: Boolean = true,
    // The Window Length in seconds (used to calculate the windowSize dynamically)
    @Configurable("Target Window Duration", "The Window Size will be calculated to be the highest possible power of 2 without being longer than this duration.")
    @RequireDoubleRange(min = 0.005, max = 1.0)
    @PlusMinusButtons(step = 0.005)
    @VisibleWhen(FromDurationEnabledChecker::class, inverted = false)
    var windowDurationInSeconds: Double = 0.050,
    // Static Window Size (should be a power of 2) (greater value -> higher frequency accuracy, worse time accuracy)
    @Configurable("Window Size", "Window Size for generating the Spectrogram (greater value -> higher frequency resolution, worse time resolution)")
    @RequireIntRange(min = 16, max = 16384)
    @RequireCustom(PowOf2Checker::class)
    @VisibleWhen(FromDurationEnabledChecker::class, inverted = true)
    var windowSize: Int = 2048,
    // How many windows should overlap (greater value -> wider image (due to smaller step size))
    @Configurable("Overlap Factor", "How many Windows should overlap (greater value -> wider image (due to smaller step size))")
    @RequireIntRange(min = 1, max = 1024)
    @PlusMinusButtons
    var overlapFactor: Int = 16,
    // Weither to apply the Hamming Window Function on the samples in a window
    @Configurable("Hamming Window", "Weither to apply the Hamming Window Function on the samples in a Window")
    var useHammingWindow: Boolean = true,
    // Maximum range for amp values from the highest value (the min value will be set to max-maxRange if range would be
    // too big otherwise)
    @Configurable("Max Amp Range", "Maximum range for amp values from the highest value (the min value will be set to max-maxRange if range would be too big otherwise)")
    @RequireIntRange(min = 1, max = 99999)
    @PlusMinusButtons
    var maxRange: Int = 99999,
    // Weither to use a log10 y-axis instead of a linear one (true means a log10 y-axis will be used)
    @Configurable("log10 Y-Axis", "Weither to use a log10 y-axis instead of a linear one")
    var log10YAxis: Boolean = true,
    // Factor which is multiplied with the linear y-axis length to get the log10 y-axis length (1 -> same length,
    // 2 -> doubled length, ...)
    @Configurable("log10 Y-Axis Length Factor", "Factor which is multiplied with the linear y-axis length to get the log10 y-axis length")
    @RequireDoubleRange(min = 0.1, max = 20.0)
    @PlusMinusButtons(step = 0.1)
    @VisibleWhen(Log10YAxisEnabledChecker::class, inverted = false)
    var log10YAxisLengthFactor: Double = 1.0
) : Configuration() {
    fun sha512Hash(): String {
        //just gerate a sha512 hash of all values concatenated
        return sha256(
            "$calculateWindowSizeFromDuration " //always factored into the hash
                    +
                    if (calculateWindowSizeFromDuration) {
                        "$windowDurationInSeconds " //only factor in the duration when using that to calculate the hash
                    } else {
                        "$windowSize " //only factor in the static value
                    }
                    + "$overlapFactor $useHammingWindow $maxRange $log10YAxis " //these values are always factored in
                    +
                    if (log10YAxis) {
                        "$log10YAxisLengthFactor" //only factor when actually using the log y-axis
                    } else {
                        ""
                    }
        )
    }

    fun toJson(): JsonObject {
        return JsonParser.parseString(GSON.toJson(this)).asJsonObject
    }

    companion object {
        fun fromJson(json: JsonElement): SpectrogramParameters {
            return GSON.fromJson(json, SpectrogramParameters::class.java)
        }
    }
}

object PowOf2Checker : CustomChecker<Int>() {
    override fun check(value: Int): CheckResult {
        return CheckResult((value and (value - 1) == 0), "Has to be a power of 2")
    }
}

object FromDurationEnabledChecker : VisibleChecker<SpectrogramParameters>() {
    override fun check(value: SpectrogramParameters): Boolean = value.calculateWindowSizeFromDuration
}
object Log10YAxisEnabledChecker : VisibleChecker<SpectrogramParameters>() {
    override fun check(value: SpectrogramParameters): Boolean = value.log10YAxis
}

/* Old (best) Values: WL = 50ms OF = 16 */
/* Also good Values: WL = 100ms OF = 64 */
