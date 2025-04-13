package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editplacedtrigger

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.configuration.ConfigurationBox
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.InspectorOption
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.TriggerSelectionManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.ScrollableInspectorContentsContainer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.NumberField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.TimeNumberFieldState
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequenceLine
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ChangeListenerContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.lang.reflect.Field

/**
 * object to house global state for the edit placed trigger inspector (so
 * state which should be persistent across placed triggers)
 */
private object GlobalState {
    var moveStartWhenChangingDuration by mutableStateOf(false)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditPlacedTriggerInspector(project: Project) {
    ScrollableInspectorContentsContainer("Edit Placed Trigger") {
        Box(Modifier.padding(horizontal = 10.dp).fillMaxSize()) {
            // get trigger as a local variable, so smart casting works
            val trigger = TriggerSelectionManager.singleSelectedTriggerState.value
            val triggersLine by remember {
                derivedStateOf {
                    TriggerSelectionManager.singleSelectedTriggerState.value?.let {
                        project.currentSong?.sequence?.findLineOf(it)
                    }
                }
            }
            if(trigger == null) {
                Text(
                    text = "Select a single Placed Trigger to edit.",
                    modifier = Modifier.padding(horizontal = 10.dp).align(Alignment.TopCenter),
                    color = JewelTheme.globalColors.text.disabled
                )
            } else {
                Column {
                    // link to the trigger template
                    FlowRow {
                        Text("Instance of ")
                        Link(trigger.triggerTemplate.name(), overflow = TextOverflow.Ellipsis, onClick = {
                            // select the trigger template in the browser
                            project.browserState.selectGroupAndTemplate(trigger.triggerTemplate.group.uuid, trigger.triggerTemplate.uuid)
                            // open template inspector
                            MainUiState.inspectorOption = InspectorOption.TriggerTemplate
                        })
                    }
                    Spacer(Modifier.height(8.dp))
                    // trigger position settings
                    key(TriggerSelectionManager.singleSelectedTriggerStartTimeState.value, TriggerSelectionManager.singleSelectedTriggerDurationState.value) {
                        triggersLine?.let { triggersLine -> TriggerPlacementInputRow(project, trigger, triggersLine) }
                    }
                    // triggers configuration (with significant padding, so it is clearly separated from the other settings)
                    Spacer(Modifier.height(20.dp))
                    key(trigger) {
                        if(trigger.configuration != null) {
                            Row {
                                ConfigurationBox(
                                    modifier = Modifier.fillMaxSize(),
                                    configuration = trigger.configuration,
                                    context = object : AbstractPlacedTrigger.PlacedTriggerConfigurationContext(trigger), ChangeListenerContext {
                                        override fun onChange(field: Field, configurable: Configurable) {
                                            // redraw timeline so any changes to the trigger are visible
                                            redrawTimeline()
                                            // save the triggers line to file (should be debounced in the future)
                                            triggersLine?.saveToFile()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TriggerPlacementInputRow(project: Project, trigger: AbstractPlacedTrigger, triggersLine: TriggerSequenceLine) {
    // States
    val startTimeState = remember { TimeNumberFieldState(trigger.startTime) }
    val endTimeState = remember { TimeNumberFieldState(trigger.endTime) }
    val durationState = remember { TimeNumberFieldState(trigger.duration) }
    LaunchedEffect(startTimeState) {
        snapshotFlow { startTimeState.value }.collect { value ->
            if(!startTimeState.isValid) return@collect
            value?.let {
                // calculate new start time
                val newStartTime = it.coerceIn(
                    triggersLine.getFreeDurationUntil(trigger.startTime),
                    trigger.endTime - AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION
                )
                // update trigger
                val currentEndTime = trigger.endTime
                trigger.startTime = newStartTime
                trigger.duration = currentEndTime - newStartTime
                // redraw timeline, so change becomes visible
                redrawTimeline()
            }

        }
    }
    LaunchedEffect(endTimeState) {
        snapshotFlow { endTimeState.value }.collect { value ->
            if(!endTimeState.isValid) return@collect
            value?.let {
                // calculate new end time
                val newEndTime = it.coerceIn(
                    trigger.startTime + AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION,
                    trigger.endTime + triggersLine.getFreeDurationFrom(trigger.endTime, allowPastSequenceEnd = true)
                )
                // update trigger
                trigger.duration = newEndTime - trigger.startTime
                // redraw timeline, so change becomes visible
                redrawTimeline()
            }
        }
    }
    LaunchedEffect(durationState) {
        snapshotFlow { durationState.value }.collect { value ->
            if(!durationState.isValid) return@collect
            value?.let {
                if(GlobalState.moveStartWhenChangingDuration) {
                    // calculate new duration, which prevents overlapping
                    val newDuration = it.coerceIn(
                        AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION,
                        trigger.duration + triggersLine.getFreeDurationUntil(trigger.startTime, allowTriggerStartAtTimePosition = true)
                    )
                    // update trigger
                    val currentEndTime = trigger.endTime
                    trigger.duration = newDuration
                    trigger.startTime = currentEndTime - newDuration
                } else {
                    // calculate new duration, which prevents overlapping
                    val newDuration = it.coerceIn(
                        AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION,
                        trigger.duration + triggersLine.getFreeDurationFrom(trigger.endTime, allowPastSequenceEnd = true)
                    )
                    // update trigger
                    trigger.duration = newDuration
                }
                // redraw timeline, so change becomes visible
                redrawTimeline()
            }
        }
    }
    // Inputs
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(Modifier.fillMaxHeight()) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text("Start:")
            }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text("End:")
            }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text("Duration:")
            }
        }
        Column(Modifier.padding(start = 6.dp).height(IntrinsicSize.Min)) {
            Row(Modifier.padding(vertical = 6.dp)) {
                NumberField(state = startTimeState, modifier = Modifier.fillMaxWidth(), customTrailingIcon = {
                    IconButton(
                        onClick = {
                            val targetTime = if(startTimeState.isValid) startTimeState.value else null
                            if(targetTime == null) return@IconButton
                            project.currentSong?.jumpTo(targetTime)
                        }
                    ) {
                        Icon(AllIconsKeys.Actions.Undo, "Jump to start")
                    }
                })
            }
            Row(Modifier.padding(vertical = 6.dp)) {
                NumberField(state = endTimeState, modifier = Modifier.fillMaxWidth(), customTrailingIcon = {
                    IconButton(
                        onClick = {
                            val targetTime = if(endTimeState.isValid) endTimeState.value else null
                            if(targetTime == null) return@IconButton
                            project.currentSong?.jumpTo(targetTime)
                        }
                    ) {
                        Icon(AllIconsKeys.Actions.Redo, "Jump to end")
                    }
                })
            }
            Row(Modifier.padding(vertical = 6.dp)) {
                NumberField(state = durationState, modifier = Modifier.fillMaxWidth(), customTrailingIcon = {
                    Row {
                        SelectableIconButton(
                            modifier = Modifier.trackActivation(),
                            onClick = { GlobalState.moveStartWhenChangingDuration = true },
                            selected = GlobalState.moveStartWhenChangingDuration
                        ) {
                            Icon(AllIconsKeys.Stub, "Move start")
                        }
                        SelectableIconButton(
                            modifier = Modifier.trackActivation(),
                            onClick = { GlobalState.moveStartWhenChangingDuration = false },
                            selected = !GlobalState.moveStartWhenChangingDuration
                        ) {
                            Icon(AllIconsKeys.Stub, "Move end")
                        }
                    }
                })
            }
        }
    }
}