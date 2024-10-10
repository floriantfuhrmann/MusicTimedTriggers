package eu.florian_fuhrmann.musictimedtriggers.project

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON_PRETTY
import java.io.File
import java.nio.charset.StandardCharsets

class ProjectSettings(val projectColor: GenericColor) {

    companion object {
        const val SAVE_FILE_NAME = "project_settings.json"
        fun create(projectColor: GenericColor): ProjectSettings {
            return ProjectSettings(projectColor)
        }

        private fun fromJson(jsonObject: JsonObject): ProjectSettings {
            return ProjectSettings(
                projectColor = GenericColor.fromJson(jsonObject.getAsJsonObject("projectColor"))
            )
        }

        /**
         * Load the project settings from the project directory.
         */
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
        return jsonObject
    }

}