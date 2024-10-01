package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.Collapsible
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.DoubleField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SimpleIconButton
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.SelectableIconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.util.thenIf
import java.lang.reflect.Field
import kotlin.math.round

class KeyframesConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    visibleWhen: VisibleWhen?,
    private val context: ConfigurationContext
) : AbstractConfigurationEntry<String>(
    configuration,
    field,
    configurable,
    visibleWhen = visibleWhen
) {
    @Composable
    override fun Content() {
        if(context !is AbstractPlacedTrigger.PlacedTriggerConfigurationContext) {
            Row {
                Text("Configuration Context has to be PlacedTriggerConfigurationContext to configure ${configurable.displayName}", color = MainUiState.theme.errorTextColor())
            }
            return
        } else {
            // Some Space above Collapsable
            Spacer(modifier = Modifier.height(10.dp))
            // get underlying object (object, which is being configured)
            val keyframesObj = field.get(configuration) as Keyframes
            // UI State
            val collapsibleExtended = remember { mutableStateOf(true) }
            var selectedPositionDisplayFormat by mutableStateOf(PositionDisplayFormat.ProportionalPosition)
            var keyframes by mutableStateOf(
                createKeyframeConfigItems(
                    keyframesObj,
                    PositionDisplayFormat.ProportionalPosition,
                    context
                )
            )
            // Functions to edit Keyframes
            /**
             * Recreates the List of KeyframeConfigItems from underlying field and
             * sets state, thus updating the UI. This should be called, when changing
             * display format or adding/removing a Keyframe.
             */
            fun recreateKeyframeConfigItems() {
                keyframes = createKeyframeConfigItems(
                    keyframesObj, selectedPositionDisplayFormat, context
                )
            }

            /**
             * Inserts a new Keyframe at [insertionIndex] and recreates Keyframe Config
             * Items
             *
             * @param insertionIndex index the new Keyframe should have
             */
            fun insertAtIndex(insertionIndex: Int) {
                // Insert a new Keyframe at the given index in underlying object
                keyframesObj.insertNewAtIndex(insertionIndex)
                // Recreate Keyframe Config Items so UI updates
                recreateKeyframeConfigItems()
            }

            /**
             * Removes Keyframe at [removeIndex].
             *
             * @param removeIndex index of the Keyframe, which should be removed.
             */
            fun removeAtIndex(removeIndex: Int) {
                // Remove element from keyframeList in underlying object
                keyframesObj.keyframesList.removeAt(removeIndex)
                // Recreate Keyframe Config Items so UI updates
                recreateKeyframeConfigItems()
            }

            /**
             * Sets the position of Keyframe at [index] in underlying object
             *
             * @param index index of Keyframe in fields keyframeList
             * @param newPos new position value in currently selected position display
             *    format
             */
            fun writeBackPos(index: Int, newPos: Double) {
                keyframesObj.keyframesList[index].position =
                    selectedPositionDisplayFormat.convertBackToProportional(newPos, context.placedTrigger)
            }

            /**
             * Sets the value of Keyframe at [index] in underlying object
             *
             * @param index index of Keyframe in fields keyframeList
             * @param newValue new value for the Keyframe
             */
            fun writeBackValue(index: Int, newValue: Double) {
                keyframesObj.keyframesList[index].value = newValue
            }

            // UI
            Collapsible(
                extended = collapsibleExtended,
                header = {
                    // Heading
                    Text(configurable.displayName)
                },
                contentModifier = Modifier.padding(0.dp)
            ) {
                Column {
                    // Header Row for Keyframe List
                    Row(
                        modifier = Modifier.padding(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 0.dp),
                    ) {
                        // Position Display Format Selector
                        Column(
                            modifier = Modifier.fillMaxWidth().height(25.dp).weight(1f)
                        ) {
                            Dropdown(
                                modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                                menuContent = {
                                    PositionDisplayFormat.entries.forEach {
                                        selectableItem(selected = selectedPositionDisplayFormat == it, onClick = {
                                            // set positionDisplayFormat state
                                            selectedPositionDisplayFormat = it
                                            // also recreate keyframe items
                                            recreateKeyframeConfigItems()
                                        }) {
                                            Text(it.displayName)
                                        }
                                    }
                                }) {
                                Text(
                                    text = selectedPositionDisplayFormat.displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                        }
                        Spacer(Modifier.width(3.dp))
                        // Value Heading
                        Column(
                            modifier = Modifier.fillMaxWidth().height(25.dp).weight(1f)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Row {
                                Text(
                                    modifier = Modifier.padding(start = 5.dp),
                                    text = "Value"
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.width(3.dp))
                        // Empty column as spacing
                        Column(
                            modifier = Modifier.width(24.dp).height(25.dp)
                        ) {}
                    }
                    // Keyframe List
                    key(keyframes) { // wrapped in key(..), so UI refreshes when this state is set to a new value
                        // calculate proportion position distance required to allow insert (used later to determine, whether Insert Above/Bellow option is allowed)
                        val requiredProportionPositionDifferenceForInsert =
                            2 * minimumProportionDifference(context.placedTrigger)
                        // iterate keyframe items
                        keyframes.forEachIndexed { index, keyframe ->
                            // Check whether first / last
                            val isFirst = index == 0
                            val isLast = index == keyframes.size - 1
                            // Separator Line
                            Row(
                                modifier = Modifier.padding(top = 5.dp).fillMaxWidth().height(1.dp)
                                    .background(JewelTheme.globalColors.borders.normal)
                            ) {}
                            // Keyframe Item
                            Row(
                                modifier = Modifier.padding(start = 5.dp, top = 5.dp, end = 5.dp)
                            ) {
                                // Position
                                Column(
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                ) {
                                    // Create Double Field
                                    DoubleField(
                                        enabled = !(isFirst || isLast), // First and Last Keyframes have to be exactly at beginning / ending
                                        initialValue = keyframe.pos,
                                        minValue = keyframe.minPos,
                                        maxValue = keyframe.maxPos,
                                        plus = {
                                            val greaterValue = round(it * 1000 + 1) / 1000.0
                                            if (greaterValue > keyframe.maxPos) { it } else { greaterValue }
                                        },
                                        minus = {
                                            val smallerValue = round(it * 1000 - 1) / 1000.0
                                            if (smallerValue < keyframe.minPos) { it } else { smallerValue }
                                        },
                                        onChange = { newValue ->
                                            writeBackPos(index, newValue)
                                        }
                                    )
                                }
                                Spacer(Modifier.width(3.dp))
                                // Value
                                Column(
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                ) {
                                    DoubleField(
                                        initialValue = keyframe.value,
                                        minValue = 0.0,
                                        maxValue = 1.0,
                                        plus = { round(it * 100 + 1) / 100.0 },
                                        minus = { round(it * 100 - 1) / 100.0 },
                                        onChange = { newValue ->
                                            writeBackValue(index, newValue)
                                        }
                                    )
                                }
                                Spacer(Modifier.width(3.dp))
                                // Actions Button
                                Column(
                                    modifier = Modifier.height(36.dp).width(24.dp)
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Row {
                                        // Dropdown State
                                        var dropdownExpanded by remember { mutableStateOf(false) }
                                        // Options Button
                                        SimpleIconButton(
                                            forceHoverHandCursor = true,
                                            iconName = "more-options-icon",
                                            onClick = {
                                                dropdownExpanded = !dropdownExpanded
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                        // Options Dropdown
                                        DropdownMenu(
                                            expanded = dropdownExpanded,
                                            onDismissRequest = {
                                                dropdownExpanded = false
                                            },
                                            modifier = Modifier.background(color = JewelTheme.globalColors.paneBackground)
                                                .border(width = 1.dp, color = JewelTheme.globalColors.borders.normal)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                                            ) {
                                                // Insert Above Option
                                                DropdownOptionRow(
                                                    enabled = !isFirst && keyframe.proportionPosition - keyframes[index - 1].proportionPosition > requiredProportionPositionDifferenceForInsert,
                                                    onClick = {
                                                        insertAtIndex(index)
                                                        dropdownExpanded = false
                                                    }) {
                                                    Text("Insert Above")
                                                }
                                                // Insert Bellow Option
                                                DropdownOptionRow(
                                                    enabled = !isLast && keyframes[index + 1].proportionPosition - keyframe.proportionPosition > requiredProportionPositionDifferenceForInsert,
                                                    onClick = {
                                                        insertAtIndex(index + 1)
                                                        dropdownExpanded = false
                                                    }) {
                                                    Text("Insert Bellow")
                                                }
                                                // Delete Option
                                                DropdownOptionRow(
                                                    enabled = !(isFirst || isLast),
                                                    onClick = {
                                                        removeAtIndex(index)
                                                        dropdownExpanded = false
                                                    }) {
                                                    Text(text = "Delete", color = MainUiState.theme.errorTextColor())
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
private fun DropdownOptionRow(
    enabled: Boolean = true, onClick: () -> Unit, content: @Composable () -> Unit
) {
    Row {
        SelectableIconButton(enabled = enabled,
            selected = false,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                .thenIf(enabled) { pointerHoverIcon(PointerIcon.Hand) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxHeight().padding(5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(start = 5.dp).alpha(
                        if (enabled) {
                            1f
                        } else {
                            0.5f
                        }
                    ).fillMaxWidth()
                ) {
                    content()
                }
            }
        }
    }
}

enum class PositionDisplayFormat(val displayName: String) {
    /**
     * Keyframe position is given as what **proportion** of the triggers
     * duration has elapsed at the triggers position
     */
    ProportionalPosition("Proportion Position"),

    /** How many seconds after the triggers start time the Keyframe is placed */
    RelativeSecondPosition("Relative Seconds Position"),

    /** Keyframe position given by seconds from sequence start */
    AbsoluteSecondPosition("Absolute Seconds Position");

    fun convertFromProportional(proportionInput: Double, triggerContext: AbstractPlacedTrigger) =
        Companion.convertFromProportional(proportionInput, this, triggerContext)

    fun convertBackToProportional(input: Double, triggerContext: AbstractPlacedTrigger) =
        Companion.convertBackToProportional(input, this, triggerContext)

    companion object {
        /**
         * Converts a proportional input to position in [outputFormat] format
         *
         * @param proportionInput position in proportion input
         * @param outputFormat format, in which the return value should be
         * @param triggerContext trigger the Relative/Absolute format refer to
         * @return position converted to [outputFormat] format
         */
        fun convertFromProportional(
            proportionInput: Double,
            outputFormat: PositionDisplayFormat,
            triggerContext: AbstractPlacedTrigger
        ) = when (outputFormat) {
            ProportionalPosition -> proportionInput
            RelativeSecondPosition -> Keyframes.Keyframe.relativeSecondPosition(proportionInput, triggerContext)
            AbsoluteSecondPosition -> Keyframes.Keyframe.absoluteSecondPosition(proportionInput, triggerContext)
        }

        /**
         * Converts input in [inputFormat] format to a proportional position
         *
         * @param input position in [inputFormat] format
         * @param inputFormat format of [input]
         * @param triggerContext trigger the Relative/Absolute format refer to
         * @return position in proportional format
         */
        fun convertBackToProportional(
            input: Double,
            inputFormat: PositionDisplayFormat,
            triggerContext: AbstractPlacedTrigger
        ) = when (inputFormat) {
            ProportionalPosition -> input
            RelativeSecondPosition -> Keyframes.Keyframe.fromRelativeSecondPositionToProportion(input, triggerContext)
            AbsoluteSecondPosition -> Keyframes.Keyframe.fromAbsoluteSecondPositionToProportion(input, triggerContext)
        }
    }
}

/**
 * [pos], [minPos], [maxPos] are already converted into position display format
 */
private class KeyframeConfigItem(
    val proportionPosition: Double,
    val pos: Double,
    val minPos: Double,
    val maxPos: Double,
    val value: Double
)

private const val MIN_KEYFRAME_DISTANCE_SECONDS = 0.05 // 50ms
private fun minimumProportionDifference(contextTrigger: AbstractPlacedTrigger) = MIN_KEYFRAME_DISTANCE_SECONDS / contextTrigger.duration
private fun createKeyframeConfigItems(
    keyframes: Keyframes,
    positionDisplayFormat: PositionDisplayFormat,
    context: AbstractPlacedTrigger.PlacedTriggerConfigurationContext
): List<KeyframeConfigItem> {
    val minimumPositionDifference = if(positionDisplayFormat == PositionDisplayFormat.ProportionalPosition) {
        // calculate min proportion distance (minimum difference for keyframe proportion positions)
        MIN_KEYFRAME_DISTANCE_SECONDS / context.placedTrigger.duration
    } else {
        MIN_KEYFRAME_DISTANCE_SECONDS
    }
    // map to KeyframeConfig items
    return keyframes.keyframesList.mapIndexed { index, keyframe ->
        // calculate pos (position converted to display format)
        val pos = positionDisplayFormat.convertFromProportional(keyframe.position, context.placedTrigger)
        // return KeyframeConfigItem
        when (index) {
            0, (keyframes.keyframesList.size - 1) -> {
                // First Keyframes position has to be at position zero (trigger start) where it should already be!
                // Last Keyframes position has to be at position one (trigger end) where it should already be!
                KeyframeConfigItem(keyframe.position, pos, pos, pos, keyframe.value)
            }
            else -> {
                KeyframeConfigItem(
                    keyframe.position,
                    pos,
                    positionDisplayFormat.convertFromProportional(keyframes.keyframesList[index - 1].position, context.placedTrigger) + minimumPositionDifference,
                    positionDisplayFormat.convertFromProportional(keyframes.keyframesList[index + 1].position, context.placedTrigger) - minimumPositionDifference,
                    keyframe.value
                )
            }
        }
    }
}