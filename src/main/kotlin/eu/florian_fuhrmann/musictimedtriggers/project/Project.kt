package eu.florian_fuhrmann.musictimedtriggers.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.godaddy.android.colorpicker.HsvColor
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.unusedfiles.UnusedFilesDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.fromJson
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.toJson
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON_PRETTY
import java.io.File
import java.nio.charset.StandardCharsets

class Project(val projectDirectory: File, var projectColor: HsvColor, val triggersManager: TriggersManager) {

    // Functions managing the Projects Files

    fun getProjectName(): String = projectDirectory.name

    fun getAudioDirectory(): File = File(projectDirectory, "Audio")

    fun getCacheDirectory(): File = File(projectDirectory, "Cache")

    fun isFileInsideProjectDirectory(file: File) =
        file.canonicalPath.startsWith(projectDirectory.canonicalPath + File.separator)

    fun scanForUnusedAudioFiles() {
        //find unused files
        val unusedAudioFiles = getAudioDirectory().listFiles()?.filter { file ->
            //check if no song has this file as audio file
            songs.none { song ->
                song.audioFile == file
            }
        }?.toList()
        if(unusedAudioFiles?.isNotEmpty() == true) {
            //Open UnusedFiles Dialog
            DialogManager.openDialog(UnusedFilesDialog(unusedAudioFiles))
        } else {
            //alert
            DialogManager.alert(Alert(
                title = "No unused files found",
                text = "No unused Audio Files where found in ${getAudioDirectory().canonicalPath}",
                onDismiss = {}
            ))
        }
    }

    // Functions managing the Triggers

    // ...

    // Functions managing the Projects Songs

    var songs: List<Song> by mutableStateOf(emptyList())
    var currentSong: Song? by mutableStateOf(null)

    fun addSong(song: Song) {
        //add song
        songs = songs.toMutableList().apply {
            add(song)
        }
        //save project
        save()
    }
    fun moveSong(fromIndex: Int, toIndex: Int) {
        //update songs list
        songs = songs.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        //save project
        save()
    }
    fun deleteSong(song: Song) {
        //remove song
        songs = songs.toMutableList().apply {
            remove(song)
        }
        //save project
        save()
        //alert
        DialogManager.alert(Alert(
            title = "Song deleted",
            text = "Song ${song.name} has been deleted. Do you want to scan the Audio directory for unused files?",
            dismissText = "No",
            onDismiss = {},
            confirmText = "Yes",
            onConfirm = { scanForUnusedAudioFiles() }
        ))
    }
    fun updateSong(song: Song) {
        //refresh ui
        redrawTimeline()
        //save project
        save()
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

    // UI States

    var browserState: BrowserState? = null

    // Functions to Save and Load Project

    fun save() {
        //create json
        val json = toJson()
        //write json to file
        val file = File(projectDirectory, ProjectManager.PROJECT_JSON_FILE_NAME)
        file.writeText(text = GSON_PRETTY.toJson(json), charset = StandardCharsets.UTF_8)
        //verbose
        println("Project saved to file")
    }

    private fun toJson(): JsonObject {
        //create json
        val json = JsonObject()
        //add color
        json.add("color", projectColor.toJson())
        //add triggers manager
        json.add("triggersManager", triggersManager.toJson())
        //add songs
        val songsJsonArray = JsonArray()
        songs.forEach { songsJsonArray.add(it.toJson()) }
        json.add("songs", songsJsonArray)
        //add ui state
        json.add("uiBrowserState", browserState!!.toJson())
        //return
        return json
    }

    companion object {
        fun fromJson(projectDirectory: File, json: JsonObject): Project {
            //get color
            val color: HsvColor = HsvColor.fromJson(json.get("color").asJsonObject)
            //get triggers manager
            val triggersManager = if(json.has("triggersManager")) {
                TriggersManager.fromJson(json.get("triggersManager").asJsonObject)
            } else {
                TriggersManager.create()
            }
            //create project instance
            val project = Project(projectDirectory, color, triggersManager)
            //set songs
            if(json.has("songs")) {
                project.songs = json.get("songs").asJsonArray.map {
                    Song.fromJson(project, it.asJsonObject)
                }
            }
            //set ui state
            project.browserState = if (json.has("uiBrowserState")) {
                BrowserState.fromJson(project, json.get("uiBrowserState").asJsonObject)
            } else {
                BrowserState.create(project)
            }
            //return project
            return project
        }
    }
}
