package eu.florian_fuhrmann.musictimedtriggers.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON_PRETTY
import java.io.File
import java.nio.charset.StandardCharsets

class Project(
    val projectDirectory: File,
    val projectSettings: ProjectSettings,
    val triggersManager: TriggersManager
) {

    // Functions managing the Projects Files

    fun getProjectName(): String = projectDirectory.name
    fun getAudioDirectory(): File = File(projectDirectory, "Audio")
    fun getCacheDirectory(): File = File(projectDirectory, "Cache")
    fun isFileInsideProjectDirectory(file: File) =
        file.canonicalPath.startsWith(projectDirectory.canonicalPath + File.separator)
    fun isFileInsideAudioDirectory(file: File, allowSubDirectories: Boolean = true) =
        if(allowSubDirectories) {
            file.canonicalPath.startsWith(getAudioDirectory().canonicalPath + File.separator)
        } else {
            File(getAudioDirectory(), file.name).canonicalPath == file.canonicalPath
        }

    // UI States

    lateinit var browserState: BrowserState

    // Functions managing the Projects Songs

    var songs: List<Song> by mutableStateOf(emptyList()) // maybe use a mutable list instead
    var currentSong: Song? by mutableStateOf(null)

    /**
     * Adds the new song to the projects songlist making it appear in the
     * sidebar and saves the songlist to file.
     */
    fun addNewSongToSonglist(song: Song) {
        //add song
        songs = songs.toMutableList().apply {
            add(song)
        }
        //save project
        saveSonglistToFile()
    }

    /**
     * Moves a song in the songlist from one index to another and saves the
     * changed songlist to file.
     */
    fun moveSongInSongList(fromIndex: Int, toIndex: Int) {
        //update songs list
        songs = songs.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        //save project
        saveSonglistToFile()
    }

    /**
     * Deletes a song from the projects songlist and saves the songlist to
     * file.
     */
    fun deleteSong(song: Song) {
        //close song (if it is the current song)
        if (song == currentSong) {
            song.closing()
            currentSong = null
            song.closed()
        }
        //remove song
        songs = songs.toMutableList().apply {
            remove(song)
        }
        //save updated songlist
        saveSonglistToFile()
        //delete the corresponding sequence
        song.sequence.removeSaveFiles()
        //maybe show an unused files notification here in the future
    }

    /**
     * Handles changes to a songs' properties, which need to be saved in the
     * songlist (like name, audio file, spectrogram parameters) and saves the
     * songlist to file.
     */
    fun updateSong(song: Song, redraw: Boolean = true) {
        //refresh ui
        if(redraw) {
            redrawTimeline()
        }
        //save project
        saveSonglistToFile()
    }

    fun openSong(song: Song) {
        println("Opening Song ${song.name}...")
        //only open song if it will actually change anything
        if(song == currentSong) {
            return
        }
        //remember previous song
        val previousSong = currentSong
        //close old song and open new song
        previousSong?.closing() // 1. previous#closing
        song.opening() // 2. new#opening
        currentSong = song // 3. set currentSong reference
        previousSong?.closed() // 4. previous#closed
        song.opened() // 5. new#opened
    }

    fun openSongAtTime(song: Song, time: Double) {
        openSong(song)
        currentAudioPlayer.value?.secondPosition = time
        redrawTimeline()
    }

    // Functions to Save and Load Songlist

    /**
     * Saves the Songlist to a file in the project directory.
     * @warning Does not save the trigger sequence of the songs.
     */
    private fun saveSonglistToFile() {
        // create json
        val json = buildSonglistJson()
        // write json to file
        val file = File(projectDirectory, SONGLIST_SAVE_FILE_NAME)
        file.writeText(text = GSON_PRETTY.toJson(json), charset = Charsets.UTF_8)
    }

    private fun buildSonglistJson(): JsonObject {
        // create json
        val json = JsonObject()
        // add songs
        val songlistJsonArray = JsonArray()
        songs.forEach { songlistJsonArray.add(it.toSonglistEntryJson()) }
        json.add("songlist", songlistJsonArray)
        // return
        return json
    }

    /**
     * Loads the Songlist from songlist json file in the project directory.
     * Also loads the songs sequences.
     *
     * @warning While [saveSonglistToFile] does not save the trigger sequence,
     *    this function also loads the trigger sequences.
     */
    fun loadSonglistWithSequencesFromFile() {
        // get songlist file
        val file = File(projectDirectory, SONGLIST_SAVE_FILE_NAME)
        require(file.exists()) { "Songlist file not found" }
        // read json from file
        val jsonString: String = file.readText(charset = StandardCharsets.UTF_8)
        val json = JsonParser.parseString(jsonString).asJsonObject
        // load songs with sequences from json
        loadSonglistWithSequencesFromJson(json)
    }

    private fun loadSonglistWithSequencesFromJson(json: JsonObject) {
        // get songlist json array
        val songlistJsonArray = json.getAsJsonArray("songlist")
        // load songs with sequences from json
        songs = songlistJsonArray.map { Song.loadFromSonglistEntryJson(this, it.asJsonObject) }
    }

    companion object {
        private const val SONGLIST_SAVE_FILE_NAME = "songlist.json"
    }
}
