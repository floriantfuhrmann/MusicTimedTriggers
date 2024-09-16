package eu.florian_fuhrmann.musictimedtriggers.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.godaddy.android.colorpicker.HsvColor
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserState
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import java.io.File
import java.nio.charset.StandardCharsets

object ProjectManager {
    const val PROJECT_JSON_FILE_NAME = "mttProject.json"
    var currentProject: Project? by mutableStateOf(null)

    fun createProject(projectDirectory: File, projectColor: HsvColor) {
        //create new project instance
        currentProject = Project(projectDirectory, projectColor, TriggersManager.create())
        //create Audio directory
        currentProject!!.getAudioDirectory().mkdir()
        //create Cache directory
        currentProject!!.getCacheDirectory().mkdir()
        //create ui states
        currentProject!!.browserState = BrowserState.create(currentProject!!)
        //save project for the first time
        currentProject!!.save()
    }

    fun openProject(projectDirectory: File) {
        //make sure the file is a valid project directory
        if (!isValidProjectDirectory(projectDirectory)) {
            DialogManager.alert(
                Alert(
                    title = "Invalid Project Directory",
                    text = "The directory you selected is not a valid Project Directory",
                    onDismiss = {}
                )
            )
            return
        }
        //Open Project
        val projectJsonFile = File(projectDirectory, PROJECT_JSON_FILE_NAME)
        val jsonString: String = projectJsonFile.readText(charset = StandardCharsets.UTF_8)
        val project = Project.fromJson(projectDirectory, JsonParser.parseString(jsonString).asJsonObject)
        //make sure cache directory exists
        if(!project.getCacheDirectory().exists()) {
            project.getCacheDirectory().mkdir()
        }
        //update currentProject
        currentProject = project
    }

    private fun isValidProjectDirectory(file: File): Boolean {
        return file.exists() && file.isDirectory && file.listFiles()?.any { it.name == PROJECT_JSON_FILE_NAME } == true
    }
}