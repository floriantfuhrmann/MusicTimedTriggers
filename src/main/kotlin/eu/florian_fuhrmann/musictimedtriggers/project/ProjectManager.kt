package eu.florian_fuhrmann.musictimedtriggers.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserState
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import java.io.File

object ProjectManager {

    var currentProject: Project? by mutableStateOf(null)

    fun createProject(projectDirectory: File, projectColor: GenericColor) {
        // create new project settings
        val projectSettings = ProjectSettings.create(projectColor)
        // create new triggers manger
        val triggersManager = TriggersManager.create()
        // create new project instance
        val newProject = Project(projectDirectory, projectSettings, triggersManager)
        //create ui states
        newProject.browserState = BrowserState.create(triggersManager)
        // create Audio and Cache directory
        newProject.getAudioDirectory().mkdir()
        newProject.getCacheDirectory().mkdir()
        // save project settings for the first time
        projectSettings.save(newProject)
        // save triggers manager for the first time
        triggersManager.createTemplateGroupsDirectory(projectDirectory)
        triggersManager.saveAllTemplateGroupsToFile(projectDirectory)
        // save browser ui state for the first time
        newProject.browserState.saveToFile(projectDirectory)
        //update currentProject
        currentProject = newProject
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
        // Open Project
        // load project settings from file
        val projectSettings = ProjectSettings.loadFromFile(projectDirectory)
        // load triggers manager from files
        val triggersManager = TriggersManager.loadFromFiles(projectDirectory)
        // load browser ui state from file
        val browserState = BrowserState.loadFromFile(projectDirectory, triggersManager)
        // create project instance
        val project = Project(projectDirectory, projectSettings, TriggersManager.create())
        // set projects browser ui state
        project.browserState = browserState
        //make sure cache directory exists
        if(!project.getCacheDirectory().exists()) {
            project.getCacheDirectory().mkdir()
        }
        //update currentProject
        currentProject = project
    }

    private fun isValidProjectDirectory(projectDirectory: File): Boolean {
        return projectDirectory.exists()  && projectDirectory.isDirectory // exists and is a directory
                && projectDirectory.listFiles()?.any { it.name == ProjectSettings.SAVE_FILE_NAME } == true // contains project settings file
    }
}