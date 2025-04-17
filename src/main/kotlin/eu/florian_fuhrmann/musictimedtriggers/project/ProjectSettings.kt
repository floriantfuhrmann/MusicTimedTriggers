package eu.florian_fuhrmann.musictimedtriggers.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.Theme
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON_PRETTY
import java.io.File
import java.nio.charset.StandardCharsets

class ProjectSettings(
    projectColor: GenericColor,
    selectedTheme: Theme
) {

    var projectColor: GenericColor by mutableStateOf(projectColor)
    var selectedTheme: Theme by mutableStateOf(selectedTheme)

    companion object {
        const val SAVE_FILE_NAME = "project_settings.json"
        fun create(projectColor: GenericColor): ProjectSettings {
            return ProjectSettings(projectColor, Theme.System)
        }

        private fun fromJson(jsonObject: JsonObject): ProjectSettings {
            return ProjectSettings(
                projectColor = GenericColor.fromJson(jsonObject.getAsJsonObject("projectColor")),
                selectedTheme = if (jsonObject.has("theme")) {
                    jsonObject.get("theme").asString.let { Theme.valueOf(it) }
                } else {
                    Theme.System
                },
            )
        }

        /** Load the project settings from the project directory. */
        fun loadFromFile(projectDirectory: File): ProjectSettings {
            val file = File(projectDirectory, SAVE_FILE_NAME)
            require(file.exists()) { "Project settings file not found" }
            val jsonString: String = file.readText(charset = StandardCharsets.UTF_8)
            return fromJson(JsonParser.parseString(jsonString).asJsonObject)
        }
    }

    /**
     * Save the project settings to the project directory. Call when the
     * project settings are changed.
     */
    fun save(project: Project) {
        val file = File(project.projectDirectory, SAVE_FILE_NAME)
        file.writeText(text = GSON_PRETTY.toJson(toJson()), charset = StandardCharsets.UTF_8)
    }

    private fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.add("projectColor", projectColor.toJson())
        jsonObject.addProperty("theme", selectedTheme.name)
        return jsonObject
    }

}