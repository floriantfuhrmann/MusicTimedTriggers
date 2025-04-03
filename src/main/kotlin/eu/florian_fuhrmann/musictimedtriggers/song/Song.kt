package eu.florian_fuhrmann.musictimedtriggers.song

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TickingManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequence
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getDurationOrNull
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.openAudioPlayer
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.Spectrogram
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import java.io.File
import java.util.UUID

class Song (
    private val project: Project,
    name: String,
    var audioFile: File,
    var spectrogramParams: SpectrogramParameters,
    val sequence: TriggerSequence
) {

    var name: String by mutableStateOf(name)
    var spectrogram: Spectrogram? = Spectrogram.createSpectrogram(project, audioFile, spectrogramParams)

    fun edit(newName: String, newAudioFile: File, newSpectrogramParams: SpectrogramParameters) {
        //update values
        name = newName
        audioFile = newAudioFile
        spectrogramParams = newSpectrogramParams
        //create new spectrogram
        val newSpectrogram = Spectrogram.createSpectrogram(project, newAudioFile, newSpectrogramParams)
        //check if spectrogram has changed
        if(spectrogram?.getIdString().orEmpty() != newSpectrogram?.getIdString().orEmpty()) {
            //if so update spectrogram and generate new images (if song is opened)
            val oldSpectrogram = spectrogram //remember old spectrogram
            spectrogram = newSpectrogram //update spectrogram object
            if(isOpened()) {
                spectrogram?.loadOrGenerateImages() //generate images (if song is opened and it's not null)
            }
            oldSpectrogram?.unloadImages() //unload images of old spectrogram (to cancel possibly still active generation)
        }
        //update triggerSequence duration
        sequence.duration = getDurationOrNull(audioFile) ?: 0.0
        //notify project (so project can be saved)
        project.updateSong(this)
    }

    fun updateName(newName: String) {
        //update value
        name = newName
        //save songlist
        project.updateSong(this, false)
    }

    fun updateSpectrogramParameters(newSpectrogramParams: SpectrogramParameters) {
        //update value
        spectrogramParams = newSpectrogramParams
        //create new spectrogram
        val newSpectrogram = Spectrogram.createSpectrogram(project, audioFile, spectrogramParams)
        require(newSpectrogram != null) { "Spectrogram creation failed!" }
        //remember old spectrogram
        val oldSpectrogram = spectrogram
        //update spectrogram reference
        spectrogram = newSpectrogram
        //generate images (if song is opened)
        if(isOpened()) {
            newSpectrogram.loadOrGenerateImages()
        }
        //unload images of old spectrogram (to also cancel possibly still active generation)
        oldSpectrogram?.unloadImages()
        //notify project (so project can be saved)
        project.updateSong(this)
    }

    // Opening and Closing

    /**
     * Called when this song will no longer be the currentSong (so when this song is being closed)
     * (called before currentSong reference has been set)
     */
    fun closing() {

    }

    /**
     * Called when this song will become the currentSong (so when this song is opened)
     * (called before currentSong reference has been set)
     */
    fun opening() {

    }

    /**
     * Called when this song is no longer the currentSong (so when this song was closed)
     * (called after currentSong reference has been set)
     */
    fun closed() {
        //unload spectrogram images
        spectrogram?.unloadImages()
    }

    /**
     * Called when this song has become the currentSong (so when this song is opened)
     * (called after currentSong reference has been set)
     */
    fun opened() {
        //load spectrogram images
        spectrogram?.loadOrGenerateImages()
        //open audio player
        openAudioPlayer(audioFile)
        //redraw timeline to show new song
        redrawTimeline()
    }

    fun isOpened(): Boolean {
        return ProjectManager.currentProject?.currentSong == this
    }

    // Playback

    /**
     * Starts audio playback and starts ticking triggers
     */
    fun play() {
        //start audio
        currentAudioPlayer.value?.start()
        //start ticking
        TickingManager.startTicking()
    }

    /**
     * Pauses audio playback and ticking
     */
    fun pause() {
        //stop audio
        currentAudioPlayer.value?.stop()
        //stop ticking
        TickingManager.stopTicking()
    }

    fun isPlaying(): Boolean {
        return currentAudioPlayer.value?.playing?.value ?: false
    }

    // Utility

    fun searchUsagesAfterTime(time: Double): List<TriggersManager.TriggerUsage> {
        return sequence.lines.flatMap { line ->
            line.getTriggersInPeriod(time, Double.POSITIVE_INFINITY, false).map {
                TriggersManager.TriggerUsage(it, line, this)
            }
        }
    }

    // Serialization

    /**
     * Creates json for entry in songlist containing properties like name, path
     * to audioFile and parameters for spectrogram generation. Does not contain
     * trigger sequence, but rather the sequences uuid as reference.
     */
    fun toSonglistEntryJson(): JsonObject {
        //create json
        val json = JsonObject()
        //add name
        json.add("name", JsonPrimitive(name))
        //add path to audioFile
        if(project.isFileInsideProjectDirectory(audioFile)) {
            json.add("path", JsonPrimitive(audioFile.relativeTo(project.projectDirectory).path))
        } else {
            json.add("path", JsonPrimitive(audioFile.canonicalPath))
            json.add("fileOutsideProjectDir", JsonPrimitive(true))
        }
        //add spectrogram params
        json.add("spectrogramParams", spectrogramParams.toJson())
        //add associated trigger sequence uuid
        json.addProperty("sequence", sequence.uuid.toString())
        //return json
        return json
    }

    companion object {
        fun createSong(project: Project, name: String, audioFile: File) {
            //create the songs trigger sequence
            val sequence = TriggerSequence.createSequence(project, getDurationOrNull(audioFile) ?: 0.0)
            //create song instance
            val song = Song(
                project,
                name,
                audioFile,
                SpectrogramParameters(),
                sequence
            )
            //save newly created sequence to file
            sequence.saveAll(createDirectory = true)
            //add to project
            project.addNewSongToSonglist(song)
        }

        /**
         * Creates a Song instance from properties in songlist entry json and loads
         * sequence from files using sequences uuid reference in songlist entry
         * json.
         */
        fun loadFromSonglistEntryJson(project: Project, json: JsonObject): Song {
            //get name
            val name = json.get("name").asString
            //get file
            val audioFile = if (json.has("fileOutsideProjectDir") && json.get("fileOutsideProjectDir").asBoolean) {
                File(json.get("path").asString)
            } else {
                File(project.projectDirectory, json.get("path").asString)
            }
            //get spectrogramParams
            val spectrogramParams = if(json.has("spectrogramParams")) {
                SpectrogramParameters.fromJson(json.get("spectrogramParams"))
            } else {
                SpectrogramParameters()
            }
            //load trigger sequence
            val sequenceUuid = UUID.fromString(json.get("sequence").asString)
            val triggerSequence = TriggerSequence.loadFromFiles(project, sequenceUuid)
            //return Song
            return Song(project, name, audioFile, spectrogramParams, triggerSequence)
        }
    }

}