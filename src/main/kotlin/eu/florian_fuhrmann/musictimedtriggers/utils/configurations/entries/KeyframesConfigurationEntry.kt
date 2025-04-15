package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.DoubleNumberFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.InvalidInputPopup
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.NumberField
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.VisibleWhen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.Stroke
import org.jetbrains.jewel.foundation.modifier.border
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.component.styling.TextFieldColors
import org.jetbrains.jewel.ui.component.styling.TextFieldMetrics
import org.jetbrains.jewel.ui.component.styling.TextFieldStyle
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.*
import org.jetbrains.jewel.ui.util.thenIf
import java.lang.reflect.Field
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class KeyframesConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    context: ConfigurationContext,
    visibleWhen: VisibleWhen?
) : AbstractConfigurationEntry<String>(
    configuration = configuration,
    field = field,
    configurable = configurable,
    context = context,
    visibleWhen = visibleWhen
) {

    // init state when configuration entry is created
    val state = (if(context is AbstractPlacedTrigger.PlacedTriggerConfigurationContext) context.placedTrigger else null)?.let { trigger ->
        KeyframesConfigurationState(trigger, field.get(configuration) as Keyframes)
    }

    @Composable
    override fun Content() {
        Row {
            // Require PlacedTriggerConfigurationContext
            if(context !is AbstractPlacedTrigger.PlacedTriggerConfigurationContext) {
                Text(
                    text = "Configuration Context has to be PlacedTriggerConfigurationContext to configure ${configurable.displayName}",
                    color = JewelTheme.globalColors.text.error
                )
                return
            }
            check(state != null) { "Since the context is valid state should not be null!" }
            // Column with header and keyframes table
            Column {
                // Header
                OpenableGroupHeader(
                    open = state.tableExpanded,
                    onOpenedChange = { state.tableExpanded = it },
                    text = configurable.displayName
                )
                // Keyframes table
                if(state.tableExpanded) {
                    LaunchedEffect(Unit) {
                        // import keyframes from keyframes object
                        state.importFromKeyframesObject()
                    }
                    Row(Modifier.padding(top = 10.dp, start = 20.dp)) {
                        KeyframesTableMainColumn(state)
                    }
                }
            }
        }
    }

    @Composable
    fun RowScope.KeyframesTableMainColumn(state: KeyframesConfigurationState) {
        // set focus manager
        state.currentFocusManager = LocalFocusManager.current
        state.currentCoroutineScope = rememberCoroutineScope()
        // Ui
        Column(Modifier.fillMaxWidth().border(Stroke.Alignment.Outside, 1.dp, JewelTheme.globalColors.borders.normal)) {
            // Toolbar
            Row(Modifier.fillMaxWidth().border(Stroke.Alignment.Outside, 1.dp, JewelTheme.globalColors.borders.normal)) {
                Text("Todo: Toolbar")
            }
            // Table with Position Type and Value columns
            Row(Modifier.fillMaxWidth()) {
                // Position Column
                PositionColumn(state)
                // Value Column
                ValueColumn(state)
            }
        }
    }

    private val cellTextFieldStyle
        @Composable
        get() = TextFieldStyle(
            colors = TextFieldColors(
                background = Color.Transparent,
                backgroundDisabled = Color.Transparent,
                backgroundFocused = Color.Transparent,
                backgroundPressed = Color.Transparent,
                backgroundHovered = Color.Transparent,
                content = JewelTheme.textFieldStyle.colors.content,
                contentDisabled = JewelTheme.textFieldStyle.colors.contentDisabled,
                contentFocused = JewelTheme.textFieldStyle.colors.contentFocused,
                contentPressed = JewelTheme.textFieldStyle.colors.contentPressed,
                contentHovered = JewelTheme.textFieldStyle.colors.contentHovered,
                border = Color.Transparent,
                borderDisabled = Color.Transparent,
                borderFocused = Color.Transparent,
                borderPressed = Color.Transparent,
                borderHovered = Color.Transparent,
                caret = JewelTheme.textFieldStyle.colors.caret,
                caretDisabled = JewelTheme.textFieldStyle.colors.caretDisabled,
                caretFocused = JewelTheme.textFieldStyle.colors.caretFocused,
                caretPressed = JewelTheme.textFieldStyle.colors.caretPressed,
                caretHovered = JewelTheme.textFieldStyle.colors.caretHovered,
                placeholder = JewelTheme.textFieldStyle.colors.placeholder
            ),
            metrics = TextFieldMetrics(
                borderWidth = JewelTheme.textFieldStyle.metrics.borderWidth,
                contentPadding = PaddingValues(horizontal = 6.dp), // PaddingValues(1.dp)
                cornerSize = CornerSize(0.dp),
                minSize = JewelTheme.textFieldStyle.metrics.minSize
            )
        )

    @Composable
    fun RowScope.PositionColumn(state: KeyframesConfigurationState) {
        Column(Modifier.weight(0.65f)) {
            // Position Header
            PositionHeader(state)
            // Position Rows
            state.keyframeStates.forEach { keyframeState ->
                Divider(Orientation.Horizontal, Modifier.fillMaxWidth().zIndex(0.1f), JewelTheme.globalColors.borders.normal)
                Row(
                    modifier = Modifier.height(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // the number field for the position (with some styling hacks)
                    NumberField(
                        modifier = Modifier.fillMaxSize()
                            .focusRequester(keyframeState.positionFieldFocusRequester)
                            .thenIf(keyframeState.anyFieldFocused) {
                                background(JewelTheme.simpleListItemStyle.colors.backgroundSelected)
                            }
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .onFocusChanged {
                                keyframeState.positionFieldFocused = it.hasFocus
                            }
                            .focusable()
                            .trackActivation()
                            .border(Stroke.Alignment.Outside, 1.dp, JewelTheme.globalColors.panelBackground, RectangleShape) // hack to hide the default outline border, which can not be turned off
                            .thenIf(keyframeState.positionFieldFocused) {
                                border(
                                    alignment = Stroke.Alignment.Inside,
                                    width = 2.dp,
                                    color = if (keyframeState.positionNumberFieldState.isValid && keyframeState.positionDistanceValid) {
                                        JewelTheme.globalColors.outlines.focused
                                    } else {
                                        JewelTheme.globalColors.outlines.focusedError
                                    },
                                    shape = RectangleShape
                                )
                            },
                        state = keyframeState.positionNumberFieldState,
                        style = cellTextFieldStyle,
                        outline = if(keyframeState.positionDistanceValid) Outline.None else Outline.Error,
                        enabled = keyframeState != state.keyframeStates.first() && keyframeState != state.keyframeStates.last()
                    )
                    // export position value
                    LaunchedEffect(keyframeState.positionNumberFieldState) {
                        snapshotFlow { keyframeState.positionNumberFieldState.value }.collect {
                            // handle position value change
                            state.handlePositionValueChange(keyframeState)
                        }
                    }
                    // invalid position popup
                    if(!keyframeState.positionDistanceValid && keyframeState.positionFieldFocused) {
                        InvalidInputPopup("too close to neighbor keyframe")
                    }
                }
            }
        }
    }

    @Composable
    fun ColumnScope.PositionHeader(state: KeyframesConfigurationState) {
        var menuExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .height(20.dp)
                .clickable { menuExpanded = true }
                .focusable().trackActivation(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // left padding
            Spacer(Modifier.width(6.dp))
            // Position Format Type header with dropdown chevron
            Column(Modifier.weight(1f)) {
                Text(state.selectedPositionFormat.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(Modifier.width(16.dp)) {
                Box(Modifier.size(16.dp)) {
                    Icon(AllIconsKeys.General.ChevronDown, "Chevron")
                }
            }
            // right padding
            Spacer(Modifier.width(6.dp))
            // Position Type dropdown menu
            if(menuExpanded) {
                PopupMenu(
                    onDismissRequest = {
                        menuExpanded = false
                        return@PopupMenu true
                    },
                    horizontalAlignment = Alignment.Start,
                    content = {
                        PositionFormat.entries.forEach {
                            selectableItem(
                                selected = state.selectedPositionFormat == it,
                                onClick = {
                                    state.selectedPositionFormat = it
                                    state.handlePositionFormatChange()
                                }
                            ) {
                                Text(it.displayName)
                            }
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun RowScope.ValueColumn(state: KeyframesConfigurationState) {
        Column(Modifier.weight(0.35f).defaultMinSize(minWidth = 120.dp)) {
            // Value Header
            ValueHeader()
            // Value Rows
            state.keyframeStates.forEach { keyframeState ->
                // Divider to separate rows (with left border to overlay the default text field border, which can not be turned off)
                val dividerColor = JewelTheme.globalColors.borders.normal
                Divider(
                    orientation = Orientation.Horizontal,
                    modifier = Modifier.fillMaxWidth().zIndex(0.1f)
                        .drawWithContent {
                            drawContent()
                            drawLine(
                                color = dividerColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 10f,
                            )
                        },
                    color = dividerColor
                )
                // Row with Value NumberField
                Row(
                    modifier = Modifier.height(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // the number field for the value (with some styling hacks)
                    NumberField(
                        modifier = Modifier.fillMaxSize()
                            .focusRequester(keyframeState.valueFieldFocusRequester)
                            .thenIf(keyframeState.anyFieldFocused) {
                                background(JewelTheme.simpleListItemStyle.colors.backgroundSelected)
                            }
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .onFocusChanged {
                                keyframeState.valueFieldFocused = it.hasFocus
                            }
                            .focusable().trackActivation()
                            .thenIf(keyframeState.valueFieldFocused) {
                                border(Stroke.Alignment.Inside, 2.dp, if(keyframeState.valueNumberFieldState.isValid) JewelTheme.globalColors.outlines.focused else JewelTheme.globalColors.outlines.focusedError, RectangleShape)
                                    .border(Stroke.Alignment.Outside, 1.dp, JewelTheme.simpleListItemStyle.colors.backgroundSelected, RectangleShape) // hack to hide the default outline border, which can not be turned off
                            }
                        ,
                        state = keyframeState.valueNumberFieldState,
                        style = cellTextFieldStyle
                    )
                    // export value
                    LaunchedEffect(keyframeState.valueNumberFieldState) {
                        snapshotFlow { keyframeState.valueNumberFieldState.value }.collect {
                            if(it != null && keyframeState.valueNumberFieldState.isValid) {
                                // export value to keyframe object
                                keyframeState.exportValueToKeyframeObject()
                                // and redraw timeline, so changes are visible
                                redrawTimeline()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ColumnScope.ValueHeader() {
        Row(Modifier.height(20.dp), verticalAlignment = Alignment.CenterVertically) {
            // Divider to separate from Position Type column
            Divider(Orientation.Vertical, Modifier.fillMaxHeight(), JewelTheme.globalColors.borders.normal)
            // Value header with left spacer
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text("Value", maxLines = 1, overflow = TextOverflow.Visible)
            }
        }
    }

    class KeyframesConfigurationState(val trigger: AbstractPlacedTrigger, val keyframesObject: Keyframes) {
        var currentFocusManager: FocusManager? = null
        var currentCoroutineScope: CoroutineScope? = null
        var tableExpanded by mutableStateOf(false)
        var selectedPositionFormat by mutableStateOf(PositionFormat.Absolute)
        var keyframeStates = mutableStateListOf<KeyframeState>()

        fun handlePositionFormatChange() {
            // update position input field states by reimporting from keyframe object
            importFromKeyframesObject()
        }

        fun handlePositionValueChange(changedKeyframeState: KeyframeState): Boolean {
            // update whether the distance to other keyframes is valid for all keyframes
            // (This is O(n^2) and could/should be optimized! Especially since we are already doing the work of having
            // the list sorted. For example by recursively checking neighbors. But realistically you'll never have
            // enough keyframes for this to remotely be a problem.)
            keyframeStates.forEach { it.updatePositionDistanceValid(selectedPositionFormat, trigger) }
            // abort if any position value is currently invalid
            if(keyframeStates.any {!it.positionNumberFieldState.isValid || !it.positionDistanceValid}) {
                return false
            }
            // check whether the keyframes are in correct order
            var orderedCorrectly = true
            for(i in 1 .. keyframeStates.lastIndex) {
                val ownPositionValue = keyframeStates[i].positionNumberFieldState.value
                val previousPositionValue = keyframeStates[i-1].positionNumberFieldState.value
                check(ownPositionValue != null && previousPositionValue != null) { "Position values should not be null!" }
                if(ownPositionValue < previousPositionValue) {
                    //not sorted!
                    orderedCorrectly = false
                    break
                }
            }
            // if ordered correctly just export position to keyframe object
            if(orderedCorrectly) {
                // export position to keyframe object
                changedKeyframeState.exportPositionToKeyframeObject(selectedPositionFormat, trigger)
            } else {
                // otherwise sort keyframe states
                keyframeStates.sortBy {
                    it.positionNumberFieldState.value ?: error("Position value may not be for sorting")
                }
                // and then export all positions to keyframe object
                keyframeStates.forEach { it.exportPositionToKeyframeObject(selectedPositionFormat, trigger) }
                // copy order from keyframe states to keyframes object
                keyframeStates.forEachIndexed { index, keyframeState ->
                    keyframesObject.keyframesList[index] = keyframeState.keyframeObject ?: error("Keyframe object may not be null")
                }
                // move focus to new field
                changedKeyframeState.positionFieldFocusRequester.requestFocus()
                currentCoroutineScope?.launch {
                    delay(1)
                    changedKeyframeState.positionFieldFocusRequester.requestFocus()
                    currentFocusManager?.moveFocus(FocusDirection.Next)
                }
            }
            // redraw timeline so changes are visible
            redrawTimeline()
            return true
        }

        fun importFromKeyframesObject() {
            // ensure there are enough keyframe states
            val additionallyNeededKeyframeStates = keyframesObject.keyframesList.size - keyframeStates.size
            repeat(additionallyNeededKeyframeStates) {
                keyframeStates.add(KeyframeState(this, null))
            }
            // update keyframe states
            keyframesObject.keyframesList.forEachIndexed { index, keyframeObject ->
                // set keyframe object and import values
                keyframeStates[index].keyframeObject = keyframeObject
                keyframeStates[index].importPositionFromKeyframeObject(selectedPositionFormat, trigger)
                keyframeStates[index].importValueFromKeyframeObject()
            }
            // remove excess keyframe states
            if (keyframeStates.size > keyframesObject.keyframesList.size) {
                keyframeStates.removeRange(keyframesObject.keyframesList.size, keyframeStates.size)
            }
        }
    }

    class KeyframeState(private val parent: KeyframesConfigurationState, var keyframeObject: Keyframes.Keyframe?) {
        var positionNumberFieldState = DoubleNumberFieldState(-1.0)
        var valueNumberFieldState = DoubleNumberFieldState(-1.0, 0.0..1.0)
        var positionFieldFocused by mutableStateOf(false)
        var valueFieldFocused by mutableStateOf(false)
        val anyFieldFocused
            get() = positionFieldFocused || valueFieldFocused
        var positionFieldFocusRequester: FocusRequester = FocusRequester()
        var valueFieldFocusRequester: FocusRequester = FocusRequester()

        /**
         * Whether enough distance is kept to the other keyframe positions. Only
         * calculated from valid position values, so this state can be valid, even
         * when any field is invalid.
         * Needs to be manually recalculated when the position value changes!
         */
        var positionDistanceValid by mutableStateOf(true)

        /**
         * Check whether the distance to other keyframes is valid. Only compares
         * valid states, so will return true even if own state is invalid.
         */
        private fun isPositionDistanceValid(format: PositionFormat, trigger: AbstractPlacedTrigger): Boolean {
            // get own position value
            val ownPositionFieldValue = positionNumberFieldState.value ?: return true
            // calculate required distance to other keyframes
            val requiredDistance = when(format) {
                PositionFormat.Proportional -> Keyframes.MINIMUM_POSITION_DISTANCE_IN_SECONDS / trigger.duration
                PositionFormat.Relative, PositionFormat.Absolute -> Keyframes.MINIMUM_POSITION_DISTANCE_IN_SECONDS
            }
            // check that no other keyframe is to close to our position
            return parent.keyframeStates.filter { it != this }.none {
                val otherPositionFieldValue = it.positionNumberFieldState.value ?: return false
                (ownPositionFieldValue - otherPositionFieldValue).absoluteValue < requiredDistance
            }
        }

        fun updatePositionDistanceValid(format: PositionFormat, trigger: AbstractPlacedTrigger) {
            positionDistanceValid = isPositionDistanceValid(format, trigger)
        }

        fun importPositionFromKeyframeObject(format: PositionFormat, trigger: AbstractPlacedTrigger) {
            keyframeObject.let { kObj ->
                check(kObj != null) { "keyframeObject may not be null when updating position from keyframe" }
                positionNumberFieldState.textFieldState.setTextAndPlaceCursorAtEnd(when(format) {
                    PositionFormat.Proportional -> (kObj.position * 100_000).roundToInt() / 100_000.0
                    PositionFormat.Relative -> (kObj.relativeSecondPosition(trigger) * 1000).roundToInt() / 1000.0
                    PositionFormat.Absolute -> (kObj.absoluteSecondPosition(trigger) * 1000).roundToInt() / 1000.0
                }.toString())
                positionNumberFieldState.validRange = when(format) {
                    PositionFormat.Proportional -> 0.0..1.0
                    PositionFormat.Relative -> 0.0..trigger.duration
                    PositionFormat.Absolute -> trigger.startTime..trigger.startTime+trigger.duration
                }
            }
        }

        fun importValueFromKeyframeObject() {
            keyframeObject.let { kObj ->
                check(kObj != null) { "keyframeObject may not be null when updating value from keyframe" }
                valueNumberFieldState.textFieldState.setTextAndPlaceCursorAtEnd(((kObj.value * 1000).roundToInt() / 1000.0).toString())
            }
        }

        fun exportPositionToKeyframeObject(format: PositionFormat, trigger: AbstractPlacedTrigger) {
            // get current position value from field state
            val positionInFormat = positionNumberFieldState.value
            check(positionInFormat != null) { "position field must be valid to be exported" }
            // convert to proportional value
            val proportionalPosition = when(format) {
                PositionFormat.Proportional -> positionInFormat
                PositionFormat.Relative -> Keyframes.Keyframe.fromRelativeSecondPositionToProportion(positionInFormat, trigger)
                PositionFormat.Absolute -> Keyframes.Keyframe.fromAbsoluteSecondPositionToProportion(positionInFormat, trigger)
            }.coerceIn(0.0, 1.0)
            // set keyframe object position
            keyframeObject?.position = proportionalPosition
        }

        fun exportValueToKeyframeObject() {
            // get current value from field state
            val value = valueNumberFieldState.value
            check(value != null) { "value field must be valid to be exported" }
            // set keyframe object value
            keyframeObject?.value = value
        }
    }

    enum class PositionFormat(val displayName: String) {
        Proportional("Proportional"),
        Relative("Relative Seconds"),
        Absolute("Absolute Seconds")
    }

}