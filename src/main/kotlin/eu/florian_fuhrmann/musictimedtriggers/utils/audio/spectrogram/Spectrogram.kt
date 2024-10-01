package eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.isPcmEncoding
import eu.florian_fuhrmann.musictimedtriggers.utils.hash.sha256
import kotlinx.coroutines.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.coroutines.coroutineContext

class Spectrogram(val project: Project, private val audioFile: File, private val params: SpectrogramParameters) {

    private val dimensions: SpectrogramDimensions = calculateSpectrogramDimensions(audioFile, params)
    val imagesCount: Int = (dimensions.width / MIN_IMAGE_WIDTH).coerceAtLeast(1)
    val images: Array<BufferedImage?> = Array(imagesCount) { null }
    var imagesTotalWidth: Int? = null
    var imagesAvailable = false

    fun getIdString(): String {
        return "spectrogram_${sha256(audioFile.canonicalPath)}_${params.sha512Hash()}"
    }

    private var loadOrGenerateImagesJob: Job? = null
    @OptIn(DelicateCoroutinesApi::class)
    fun loadOrGenerateImages(forceGenerate: Boolean = false) {
        loadOrGenerateImagesJob = GlobalScope.launch {
            //get spectrogram id
            val spectrogramId = getIdString()
            //load all images from file
            var needToGenerate = forceGenerate
            if (!needToGenerate) {
                for (i in 0 until imagesCount) {
                    ensureActive() // ensure active before reading another file
                    //get file
                    val file = File(project.getCacheDirectory(), "${spectrogramId}_${i}.png")
                    //check if the file exists
                    if (file.exists()) {
                        //if so load and put at correct position
                        images[i] = ImageIO.read(file)
                    } else {
                        //if not the spectrogram is incomplete
                        needToGenerate = true //remember that images need to be generated after all
                        break //break the for loop
                    }
                }
            }
            //generate the spectrogram images
            if (needToGenerate) {
                ensureActive() // ensure active before generating images
                println("$imagesCount Spectrogram Images will be generated...")
                generateImages(spectrogramId)
            } else {
                //all files seem to have been loaded from files successfully -> images available
                setImagesAvailable()
                println("$imagesCount Spectrogram Images have been loaded from files successfully")
            }
        }
    }

    private suspend fun generateImages(spectrogramId: String = getIdString()) {
        //create images
        createImagesFromSpectrogramData(
            spectrogramData = calculateSpectrogramData(audioFile, params),
            minImageWidth = MIN_IMAGE_WIDTH
        ).forEachIndexed { index, image ->
            coroutineContext.ensureActive() // ensure active before modifying array
            //put into array
            images[index] = image
            //get file
            val file = File(project.getCacheDirectory(), "${spectrogramId}_${index}.png")
            coroutineContext.ensureActive() // ensure active before writing file
            //write to file
            ImageIO.write(image, "PNG", file)
        }
        //mark as available
        setImagesAvailable()
        println("$imagesCount Spectrogram Images have been generated successfully")
    }

    fun unloadImages() {
        //cancel loading
        runBlocking {
            loadOrGenerateImagesJob?.cancel(CancellationException("Unloading Spectrogram Images"))
            loadOrGenerateImagesJob?.join()
        }
        //unset images (fill array with null)
        imagesAvailable = false
        images.fill(null)
    }

    private suspend fun setImagesAvailable() {
        //ensure active before setting images available
        coroutineContext.ensureActive()
        //calculate images total width
        imagesTotalWidth = images.sumOf { i -> i!!.width }
        //mark as available
        imagesAvailable = true
        //redraw timeline
        redrawTimeline()
    }

    companion object {
        const val MIN_IMAGE_WIDTH = 5000

        /**
         * Returns a new Spectrogram or null if audio file is invalid
         */
        fun createSpectrogram(project: Project, audioFile: File, params: SpectrogramParameters) : Spectrogram? {
            return if(isPcmEncoding(audioFile)) {
                Spectrogram(project, audioFile, params)
            } else {
                null
            }
        }
    }

}

data class SpectrogramDimensions(val width: Int, val height: Int)
