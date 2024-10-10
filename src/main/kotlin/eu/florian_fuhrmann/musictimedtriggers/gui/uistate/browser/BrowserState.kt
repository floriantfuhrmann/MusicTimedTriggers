package eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.ReceiveDraggedTemplatesManger
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON_PRETTY
import eu.florian_fuhrmann.musictimedtriggers.windowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * Stores state of trigger templates browser, like selected and opened
 * groups. Used for UI and to store the state of the browser to file. Also
 * provides functions to manipulate the state. Does not have a reference
 * to the parent project only to the projects triggers manager.
 *
 * @param triggersManager TriggersManager of the project this browser state
 *    is for
 */
class BrowserState(
    private val triggersManager: TriggersManager
) {

    // Saving and Loading

    companion object {
        private const val SAVE_FILE_NAME = "trigger_browser_ui_state.json"

        fun create(triggersManager: TriggersManager): BrowserState {
            //create instance of new browser state
            val browserState = BrowserState(triggersManager)
            //add all groups from triggers manager to browser state
            browserState.allGroups.value = triggersManager.getAllTemplateGroups().map {
                BrowserGroup(it.uuid, it.name) //create instance of BrowserGroup
            }
            //return
            return browserState
        }

        private fun fromJson(triggersManager: TriggersManager, json: JsonObject): BrowserState {
            //create instance of new browser state
            val browserState = BrowserState(triggersManager)
            //init temporary map of browser groups
            val tempGroupMap: MutableMap<UUID, BrowserGroup> = mutableMapOf()
            //add all groups from triggers manager to browser state
            browserState.allGroups.value = triggersManager.getAllTemplateGroups().map {
                val browserGroup = BrowserGroup(it.uuid, it.name) //create instance of BrowserGroup
                tempGroupMap[it.uuid] = browserGroup //save in map
                browserGroup
            }
            //fill opened groups
            browserState.openedGroups.value = json.get("openedGroups").asJsonArray.mapNotNull {
                tempGroupMap[UUID.fromString(it.asString)] //take from map, so it is the same instance as in allGroups
            }
            //set selected group
            if(json.has("selectedGroup")) {
                //set selected group on browser state
                val selectedGroupUuid = UUID.fromString(json.get("selectedGroup").asString)
                browserState.selectedGroup.value = tempGroupMap[selectedGroupUuid]
                //update trigger templates on browser state
                browserState.updateAllGroupTriggers(triggersManager.getTemplateGroup(selectedGroupUuid))
            }
            //return
            return browserState
        }

        /**
         * Load the browser state from the project directory.
         * If the file does not exist, a new browser state is created.
         * @param projectDirectory the project directory
         * @param triggersManager the triggers manager of the project
         */
        fun loadFromFile(projectDirectory: File, triggersManager: TriggersManager): BrowserState {
            // get json file in project directory
            val file = File(projectDirectory, SAVE_FILE_NAME)
            // fallback to create new browser state if file does not exist
            if (!file.exists()) {
                return create(triggersManager)
            }
            // read json file and deserialize from json
            val jsonString = file.readText()
            val json = JsonParser.parseString(jsonString).asJsonObject
            return fromJson(triggersManager, json)
        }
    }

    /**
     * Save the browser state to the project directory.
     * @param projectDirectory the parent projects' project directory
     */
    fun saveToFile(projectDirectory: File) {
        val json = toJson()
        val file = File(projectDirectory, SAVE_FILE_NAME)
        file.writeText(GSON_PRETTY.toJson(toJson()))
    }

    private fun toJson(): JsonObject {
        val json = JsonObject()
        //add opened groups
        val openedGroupUuidsJsonArray = JsonArray()
        openedGroups.value.forEach {
            openedGroupUuidsJsonArray.add(JsonPrimitive(it.uuid.toString()))
        }
        json.add("openedGroups", openedGroupUuidsJsonArray)
        //add selected group
        if(selectedGroup.value != null) {
            json.add("selectedGroup", JsonPrimitive(selectedGroup.value!!.uuid.toString()))
        }
        return json
    }

    var currentCoroutineScope: CoroutineScope? = null

    // State
    var openedGroups: MutableState<List<BrowserGroup>> = mutableStateOf(emptyList())
    var selectedGroup: MutableState<BrowserGroup?> = mutableStateOf(null)
    var allGroups: MutableState<List<BrowserGroup>> = mutableStateOf(emptyList())

    val templates: MutableList<BrowserTemplate> = mutableStateListOf()
    val templatesLazyListState = LazyListState(0, 0)
    val selectedTemplates: MutableList<BrowserTemplate> = mutableStateListOf()
    val hoveredTemplate: MutableState<BrowserTemplate?> = mutableStateOf(null)
    private var clipboard: List<AbstractTriggerTemplate> = emptyList()

    // Groups

    fun getSelectedTriggerTemplateGroup(): TriggerTemplateGroup? {
        if(selectedGroup.value == null) return null
        return triggersManager.getTemplateGroup(selectedGroup.value!!.uuid)
    }

    /**
     * Checks if group with uuid [uuid] is opened
     */
    fun isGroupOpened(uuid: UUID) = openedGroups.value.any { it.uuid == uuid }

    /**
     * Updates all groups (also closes all currently opened groups).
     * Should only be used to initialize groups
     */
    fun updateGroups(groups: List<TriggerTemplateGroup>) {
        openedGroups.value = emptyList()
        allGroups.value = groups.map { BrowserGroup.fromTriggerTemplateGroup(it) }.toMutableList()
    }

    /**
     * Opens a group, if it is not already opened. Also Saves the Project.
     */
    fun openGroup(browserGroup: BrowserGroup) {
        if (!openedGroups.value.contains(browserGroup)) {
            //add group to opened list
            openedGroups.value = openedGroups.value.toMutableList().apply { add(browserGroup) }
            //set selected group (so the newly opened group is also selected)
            selectedGroup.value = browserGroup
            //update trigger templates
            updateAllGroupTriggers(triggersManager.getTemplateGroup(browserGroup.uuid)!!)
            //also make sure initially no triggers are selected
            unselectAllTemplates()
            //save project
            // TODO
        }
    }

    /**
     * Moves a group from [fromIndex] to [toIndex] in the Tabs Bar. Also Saves the Project.
     */
    fun moveGroup(fromIndex: Int, toIndex: Int) {
        openedGroups.value = openedGroups.value.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
            //save project
            // TODO
        }
    }

    /**
     * Closes the [browserGroup]'s Tab. Also Saves the Project.
     */
    fun closeGroup(browserGroup: BrowserGroup) {
        //remove the group from opened groups list
        openedGroups.value = openedGroups.value.toMutableList().apply { remove(browserGroup) }
        //make sure the group is not selected
        if(selectedGroup.value?.uuid == browserGroup.uuid) {
            selectedGroup.value = null
            //and if it was selected then also unselect all triggers
            unselectAllTemplates()
        }
        //save project
        // TODO
    }

    /**
     * Selects the [browserGroup]'s Tab in the Tab Bar. Also Saves the Project.
     */
    fun selectGroup(browserGroup: BrowserGroup) {
        if(selectedGroup.value != browserGroup) {
            //set selected group
            selectedGroup.value = browserGroup
            //update trigger templates
            updateAllGroupTriggers(triggersManager.getTemplateGroup(browserGroup.uuid)!!)
            //and make sure no templates are selected anymore
            unselectAllTemplates()
            //save project
            // TODO
        }
    }

    /**
     * Used to update the UI for a new TriggerTemplateGroup (adds a new BrowserGroup)
     */
    fun newGroup(triggerTemplateGroup: TriggerTemplateGroup) {
        //create browser group (because it's a new group there is no instance for it yet)
        val browserGroup = BrowserGroup.fromTriggerTemplateGroup(triggerTemplateGroup)
        //add to all and opened groups
        allGroups.value = allGroups.value.toMutableList().apply { add(browserGroup) }
        openedGroups.value = openedGroups.value.toMutableList().apply { add(browserGroup) }
        //and select the new group
        selectedGroup.value = browserGroup
        //update trigger templates
        updateAllGroupTriggers(triggersManager.getTemplateGroup(browserGroup.uuid)!!)
        //also make sure no templates are selected
        unselectAllTemplates()
        //no need to save the project because that will be done anyway (because not only the ui changed)
    }

    /**
     * Used to update the ui for an already existing TriggerTemplateGroup (updates the BrowserGroup)
     */
    fun updateGroup(triggerTemplateGroup: TriggerTemplateGroup) {
        //find the browser group
        val browserGroup = allGroups.value.find { it.uuid == triggerTemplateGroup.uuid }
        require(browserGroup != null) { "Couldn't find BrowserGroup with uuid ${triggerTemplateGroup.uuid}" }
        //update the name
        browserGroup.name.value = triggerTemplateGroup.name
    }

    /**
     * Used to update the ui for a deleted group (removes the BrowserGroup)
     */
    fun removeGroup(triggerTemplateGroup: TriggerTemplateGroup) {
        //remove from all groups
        allGroups.value = allGroups.value.toMutableList().apply {
            removeIf {
                it.uuid == triggerTemplateGroup.uuid
            }
        }
        //remove from opened groups
        openedGroups.value = openedGroups.value.toMutableList().apply {
            removeIf {
                it.uuid == triggerTemplateGroup.uuid
            }
        }
        //unselect if selected
        if(selectedGroup.value?.uuid == triggerTemplateGroup.uuid) {
            selectedGroup.value = null
            //then also update the triggers for null group (so no triggers will be displayed)
            updateAllGroupTriggers(null)
            //and make sure they are no longer selected
            unselectAllTemplates()
        }
    }

    // Templates

    private fun getBrowserTemplateByUuid(uuid: UUID) = templates.find { it.uuid == uuid }

    /**
     * Updates all trigger templates.
     */
    fun updateAllGroupTriggers(triggerTemplateGroup: TriggerTemplateGroup?) {
        templates.clear()
        if(triggerTemplateGroup != null) {
            templates.addAll(triggerTemplateGroup.templates.map {
                BrowserTemplate.fromTriggerTemplate(it)
            })
        }
    }

    /**
     * Updates a single trigger template. (Updates name and color)
     */
    fun updateTriggerTemplate(triggerTemplate: AbstractTriggerTemplate) {
        val browserTemplate = getBrowserTemplateByUuid(triggerTemplate.uuid)
        require(browserTemplate != null) { "Couldn't find BrowserTemplate with uuid ${triggerTemplate.uuid}" }
        browserTemplate.name.value = triggerTemplate.name()
        browserTemplate.composeColor.value = triggerTemplate.configuration.color.toComposeColor()
    }

    /**
     * Adds a new trigger template
     */
    fun newTriggerTemplate(triggerTemplate: AbstractTriggerTemplate, scrollTo: Boolean = true) {
        newTriggerTemplates(listOf(triggerTemplate), scrollTo)
    }

    /**
     * Adds multiple new trigger template
     * @param scrollTo if true scrolls to the first new template
     */
    fun newTriggerTemplates(newTriggerTemplates: List<AbstractTriggerTemplate>, scrollTo: Boolean = true) {
        //add templates to templates list
        templates.addAll(newTriggerTemplates.map { BrowserTemplate.fromTriggerTemplate(it) })
        if(scrollTo) {
            currentCoroutineScope?.launch {
                templatesLazyListState.animateScrollToItem(templates.size - newTriggerTemplates.size, 0)
            }
        }
    }

    /**
     * Removes the trigger templates from the list of displayed templates
     */
    fun removeTriggerTemplates(removedTriggerTemplates: List<AbstractTriggerTemplate>) {
        val removedUuids = removedTriggerTemplates.map { it.uuid }.toHashSet()
        templates.removeIf { removedUuids.contains(it.uuid) }
    }

    fun moveTemplate(fromIndex: Int, toIndex: Int) {
        //move template
        val movedTemplate = templates.removeAt(fromIndex)
        templates.add(toIndex, movedTemplate)
    }

    // Tracking hovered Template

    fun onTemplateHoverEnter(template: BrowserTemplate) {
        hoveredTemplate.value = template
    }

    fun onTemplateHoverExit(template: BrowserTemplate) {
        if(hoveredTemplate.value == template) {
            hoveredTemplate.value = null
        }
    }

    // Template Selection

    fun selectTemplate(template: BrowserTemplate, keepOthers: Boolean) {
        if(keepOthers) {
            if(selectedTemplates.contains(template)) {
                //selecting something which was already selected unselects again
                selectedTemplates.remove(template)
            } else {
                //add to selected templates
                selectedTemplates.add(template)
            }
        } else {
            //selectedTemplates will only contain the one template
            selectedTemplates.clear()
            selectedTemplates.add(template)
        }
    }

    fun unselectAllTemplates() {
        selectedTemplates.clear()
    }

    fun isSelected(browserTemplate: BrowserTemplate): Boolean {
        return selectedTemplates.contains(browserTemplate)
    }

    /**
     * Returns the selected BrowserTemplate in the same order in which they are displayed by ordering them if the order
     * could have been broken
     */
    private fun getSelectedOrdered(): List<BrowserTemplate> {
        //sort by index in the templates list
        selectedTemplates.sortBy {
            templates.indexOf(it)
        }
        return selectedTemplates
    }

    // Clipboard

    fun copy() {
        clipboard = getSelectedOrdered().map {
            val triggerTemplate = it.getTriggerTemplate()
            triggerTemplate.copy()
        }
    }

    fun paste() {
        val targetGroup = getSelectedTriggerTemplateGroup() ?: return
        triggersManager.addTriggerTemplates(clipboard.map {
            //update group for trigger template in clipboard
            it.group = targetGroup
            //return trigger template
            it
        })
        //replace all with copies so templates can be pasted again
        clipboard = clipboard.map { it.copy() }
    }

    // Drag and Drop

    val draggingSelectedTemplates = mutableStateOf(false)
    val draggingOnTimeline = mutableStateOf(false)
    val pointerOffset: MutableState<Offset> = mutableStateOf(Offset.Zero)

    fun startDragging() {
        draggingSelectedTemplates.value = true
        draggingOnTimeline.value = false
    }

    fun updateDrag() {
        //ignore if not dragging
        if(!draggingSelectedTemplates.value) return
        //get absolute pointer position
        val absolutePointerPosition = Offset(java.awt.MouseInfo.getPointerInfo().location.x.toFloat(), java.awt.MouseInfo.getPointerInfo().location.y.toFloat())
        //update pointer offset
        pointerOffset.value = Offset(
            absolutePointerPosition.x - windowState.position.x.value,
            absolutePointerPosition.y - windowState.position.y.value - 45 // 45 should be TitleBar height
        )
        //redraw timeline (if dragging on timeline)
        if(draggingOnTimeline.value) {
            redrawTimeline()
        }
    }

    fun stopDragging() {
        //ignore if not dragging
        if(!draggingSelectedTemplates.value) return
        draggingSelectedTemplates.value = false
        val wasOnTimeline = draggingOnTimeline.value
        draggingOnTimeline.value = false
        //handle drag destination if dragging stopped on timeline
        if(wasOnTimeline) {
            ReceiveDraggedTemplatesManger.receive()
        }
    }

    fun handleTimelineEnter() {
        //ignore if not dragging
        if(!draggingSelectedTemplates.value) return
        draggingOnTimeline.value = true
        //show drag indicator
        redrawTimeline()
    }

    fun handleTimelineExit() {
        //ignore if not dragging
        if(!draggingSelectedTemplates.value) return
        draggingOnTimeline.value = false
        //drag indicator no longer needs to be shown
        redrawTimeline()
    }

}

class BrowserGroup(
    val uuid: UUID,
    name: String
) {
    var name = mutableStateOf(name)

    companion object {
        fun fromTriggerTemplateGroup(triggerTemplateGroup: TriggerTemplateGroup): BrowserGroup {
            return BrowserGroup(triggerTemplateGroup.uuid, triggerTemplateGroup.name)
        }
    }
}

class BrowserTemplate(
    val uuid: UUID,
    val type: TriggerType,
    name: String,
    composeColor: Color,
) {
    var name = mutableStateOf(name)
    var composeColor = mutableStateOf(composeColor)

    fun getTriggerTemplate() = ProjectManager.currentProject!!.triggersManager.getTriggerTemplate(uuid)!!

    companion object {
        fun fromTriggerTemplate(triggerTemplate: AbstractTriggerTemplate): BrowserTemplate {
            return BrowserTemplate(
                triggerTemplate.uuid,
                triggerTemplate.getType(),
                triggerTemplate.name(),
                triggerTemplate.configuration.color.toComposeColor()
            )
        }
    }
}