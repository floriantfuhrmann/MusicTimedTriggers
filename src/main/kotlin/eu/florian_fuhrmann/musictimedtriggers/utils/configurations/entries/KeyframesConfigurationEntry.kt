package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.MoveTriggersManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.TriggerSelectionManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.DoubleNumberFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.InvalidInputPopup
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.NumberField
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequence
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequenceLine
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
    val state = (context as? AbstractPlacedTrigger.PlacedTriggerConfigurationContext)?.let { context ->
        KeyframesConfigurationState(context.sequence, context.placedTrigger, field.get(configuration) as Keyframes)
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
                    open = tableExpanded,
                    onOpenedChange = { tableExpanded = it },
                    text = configurable.displayName
                )
                // Keyframes table
                if(tableExpanded) {
                    LaunchedEffect(
                        TriggerSelectionManager.singleSelectedTriggerStartTimeState.value, // updated when the trigger is moved through the timeline editor
                        TriggerSelectionManager.singleSelectedTriggerDurationState.value, // updated when the trigger is moved through the timeline editor
                        MoveTriggersManager.endKeyframeMoveCounter, // updated when keyframes are moved through the timeline editor
                        reimportKeyframeTableCounter // updated by reimportKeyframeTable()
                    ) {
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

    private val rowHeight
        @Composable
        get() = 24.dp
    private val cellHorizontalPadding = 8.dp
    private val cellBackgroundSelected
        @Composable
        get() = JewelTheme.treeStyle.colors.backgroundSelectedFocused
    private val tableBorderColor
        @Composable
        get() = JewelTheme.groupHeaderStyle.colors.divider
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
                contentPadding = PaddingValues(horizontal = cellHorizontalPadding),
                cornerSize = CornerSize(0.dp),
                minSize = JewelTheme.textFieldStyle.metrics.minSize
            )
        )

    @Composable
    fun RowScope.KeyframesTableMainColumn(state: KeyframesConfigurationState) {
        // set focus manager
        state.currentFocusManager = LocalFocusManager.current
        state.currentCoroutineScope = rememberCoroutineScope()
        // Ui
        Column(Modifier.fillMaxWidth().border(Stroke.Alignment.Outside, 1.dp, tableBorderColor)) {
            // Toolbar
            Row(modifier = Modifier
                .height(IntrinsicSize.Min).fillMaxWidth()
                .border(Stroke.Alignment.Outside, 1.dp, tableBorderColor)
                .padding(horizontal = cellHorizontalPadding, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var addPopupExpanded by remember { mutableStateOf(false) }
                val addEnabled = state.selectedKeyframeState != null
                // Add Keyframe Button
                IconButton(
                    onClick = { addPopupExpanded = true },
                    focusable = false,
                    enabled = addEnabled,
                    modifier = Modifier.thenIf(!addEnabled) {
                        alpha(0.5f)
                    }
                ) {
                    Icon(AllIconsKeys.General.Add, null)
                    Icon(AllIconsKeys.General.Dropdown, null)
                }
                // Add Keyframe Popup Menu
                if(addPopupExpanded) {
                    PopupMenu(
                        onDismissRequest = {
                            addPopupExpanded = false
                            true
                        },
                        horizontalAlignment = Alignment.Start,
                        content = {
                            selectableItem(
                                selected = false,
                                onClick = { state.addKeyframeAboveSelected() },
                                enabled = state.selectedKeyframeState.let { it != null && !it.isFirstKeyframe() }
                            ) {
                                Text("Add Above")
                            }
                            selectableItem(
                                selected = false,
                                onClick = { state.addKeyframeBellowSelected() },
                                enabled = state.selectedKeyframeState.let { it != null && !it.isLastKeyframe() }
                            ) {
                                Text("Add Bellow")
                            }
                        }
                    )
                }
                // Remove Keyframe Button
                val removeEnabled = state.selectedKeyframeState.let { it != null && !it.isFirstOrLastKeyframe() }
                IconButton(
                    onClick = { state.removeSelectedKeyframe() },
                    focusable = false,
                    enabled = removeEnabled,
                    modifier = Modifier.thenIf(!removeEnabled) {
                        alpha(0.5f)
                    }
                ) {
                    Icon(AllIconsKeys.General.Remove, null)
                }
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

    @Composable
    fun RowScope.PositionColumn(state: KeyframesConfigurationState) {
        Column(Modifier.weight(0.65f)) {
            // Position Header
            PositionHeader(state)
            // Position Rows
            state.keyframeStates.forEach { keyframeState ->
                Divider(Orientation.Horizontal, Modifier.fillMaxWidth().zIndex(0.1f), tableBorderColor)
                Row(
                    modifier = Modifier.height(rowHeight),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // the number field for the position (with some styling hacks)
                    NumberField(
                        modifier = Modifier.fillMaxSize()
                            .focusRequester(keyframeState.positionFieldFocusRequester)
                            .thenIf(keyframeState.anyFieldFocused) {
                                background(cellBackgroundSelected)
                            }
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .onFocusChanged {
                                keyframeState.positionFieldFocused = it.hasFocus
                                if(it.hasFocus) {
                                    state.selectedKeyframeState = keyframeState
                                } else if(state.selectedKeyframeState == keyframeState) {
                                    state.selectedKeyframeState = null
                                }
                            }
                            .focusable()
                            .trackActivation()
                            .border(Stroke.Alignment.Outside, 1.dp, JewelTheme.globalColors.panelBackground, RectangleShape) // hack to hide the default outline border, which cannot be turned off
                            .border(
                                alignment = Stroke.Alignment.Inside,
                                width = 2.dp,
                                color = when {
                                    keyframeState.positionFieldFocused && keyframeState.positionNumberFieldState.isValid && keyframeState.positionDistanceValid -> JewelTheme.globalColors.outlines.focused
                                    keyframeState.positionFieldFocused && (!keyframeState.positionNumberFieldState.isValid || !keyframeState.positionDistanceValid) -> JewelTheme.globalColors.outlines.focusedError
                                    !keyframeState.positionFieldFocused && (!keyframeState.positionNumberFieldState.isValid || !keyframeState.positionDistanceValid) -> JewelTheme.globalColors.outlines.error
                                    else -> Color.Transparent
                                },
                                shape = RectangleShape
                            ) // cell inner border
                            .onKeyEvent {
                                // if not key up don't handle, but still intercept Tab and Enter
                                if(it.type != KeyEventType.KeyUp) {
                                    return@onKeyEvent it.key == Key.Tab || it.key == Key.Enter
                                }
                                // handle key events for key up
                                when (it.key) {
                                    Key.Tab -> {
                                        keyframeState.moveFocusToValueField()
                                        return@onKeyEvent true
                                    }
                                    Key.Enter -> {
                                        // find the next keyframe state
                                        val nextKeyframeState = state.keyframeStates.getOrNull(state.keyframeStates.indexOf(keyframeState) + 1)
                                        // either move focus to next keyframe state or to second keyframe state (first should be skipped because it is not editable)
                                        if(nextKeyframeState != null && nextKeyframeState != state.keyframeStates.last()) {
                                            nextKeyframeState.moveFocusToPositionField()
                                        } else {
                                            state.keyframeStates[1].moveFocusToPositionField()
                                        }
                                        return@onKeyEvent true
                                    }
                                    Key.Escape -> {
                                        // ESC clears focus
                                        state.currentFocusManager?.clearFocus()
                                        return@onKeyEvent true
                                    }
                                    else -> return@onKeyEvent false
                                }
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
                .height(rowHeight)
                .clickable { menuExpanded = true }
                .focusable().trackActivation(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // left padding
            Spacer(Modifier.width(cellHorizontalPadding))
            // Position Format Type header with dropdown chevron
            Column(Modifier.weight(1f)) {
                Text(selectedPositionFormat.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(Modifier.width(16.dp)) {
                Box(Modifier.size(16.dp)) {
                    Icon(AllIconsKeys.General.ChevronDown, "Chevron")
                }
            }
            // right padding
            Spacer(Modifier.width(cellHorizontalPadding))
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
                                selected = selectedPositionFormat == it,
                                onClick = {
                                    selectedPositionFormat = it
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
                // Divider to separate rows (with a left border to overlay the default text field border, which cannot be turned off)
                val dividerColor = tableBorderColor
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
                    modifier = Modifier.height(rowHeight),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // the number field for the value (with some styling hacks)
                    NumberField(
                        modifier = Modifier.fillMaxSize()
                            .focusRequester(keyframeState.valueFieldFocusRequester)
                            .thenIf(keyframeState.anyFieldFocused) {
                                background(cellBackgroundSelected)
                            }
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .onFocusChanged {
                                keyframeState.valueFieldFocused = it.hasFocus
                                if(it.hasFocus) {
                                    state.selectedKeyframeState = keyframeState
                                } else if(state.selectedKeyframeState == keyframeState) {
                                    state.selectedKeyframeState = null
                                }
                            }
                            .focusable().trackActivation()
                            // hack to hide the default outline border, which cannot be turned off:
                            // overlays the default border with a border, which has the same color as the neighboring
                            // cell on the right edge (so either background color or border color of neighboring cell)
                            .thenIf(keyframeState.valueFieldFocused) {
                                border(
                                    Stroke.Alignment.Outside,
                                    1.dp,
                                    if (keyframeState.positionFieldValid) cellBackgroundSelected else JewelTheme.globalColors.outlines.error,
                                    RectangleShape
                                )
                            }
                            .thenIf(keyframeState.positionFieldFocused) {
                                border(
                                    Stroke.Alignment.Outside,
                                    1.dp,
                                    if (keyframeState.positionFieldValid) JewelTheme.globalColors.outlines.focused else JewelTheme.globalColors.outlines.focusedError,
                                    RectangleShape
                                )
                            }
                            .thenIf(!keyframeState.anyFieldFocused) {
                                border(
                                    Stroke.Alignment.Outside,
                                    1.dp,
                                    if (keyframeState.positionFieldValid) JewelTheme.globalColors.panelBackground else JewelTheme.globalColors.outlines.error,
                                    RectangleShape
                                )
                            }
                            // inner border of this cell:
                            .border(
                                alignment = Stroke.Alignment.Inside,
                                width = 2.dp,
                                color = when {
                                    keyframeState.valueFieldFocused && keyframeState.valueNumberFieldState.isValid -> JewelTheme.globalColors.outlines.focused
                                    keyframeState.valueFieldFocused && (!keyframeState.valueNumberFieldState.isValid) -> JewelTheme.globalColors.outlines.focusedError
                                    !keyframeState.valueFieldFocused && (!keyframeState.valueNumberFieldState.isValid) -> JewelTheme.globalColors.outlines.error
                                    else -> Color.Transparent
                                },
                                shape = RectangleShape
                            )
                            .onKeyEvent {
                                // if not key up don't handle, but still intercept Tab and Enter
                                if(it.type != KeyEventType.KeyUp) {
                                    return@onKeyEvent it.key == Key.Tab || it.key == Key.Enter
                                }
                                // handle key events for key up
                                when (it.key) {
                                    Key.Tab -> {
                                        // move focus back to the position field (if not first or last keyframe)
                                        if(!keyframeState.isFirstOrLastKeyframe()) {
                                            keyframeState.moveFocusToPositionField()
                                        }
                                        return@onKeyEvent true
                                    }
                                    Key.Enter -> {
                                        // find the next keyframe state
                                        val nextKeyframeState = state.keyframeStates.getOrNull(state.keyframeStates.indexOf(keyframeState) + 1)
                                        // either move focus to next keyframe state or to top value field
                                        if(nextKeyframeState != null) {
                                            nextKeyframeState.moveFocusToValueField()
                                        } else {
                                            state.keyframeStates.first().moveFocusToValueField()
                                        }
                                        return@onKeyEvent true
                                    }
                                    Key.Escape -> {
                                        // ESC clears focus
                                        state.currentFocusManager?.clearFocus()
                                        return@onKeyEvent true
                                    }
                                    else -> return@onKeyEvent false
                                }
                            },
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
                                // save to file
                                state.saveChangesToFile()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ColumnScope.ValueHeader() {
        Row(Modifier.height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
            // Divider to separate from the Position Type column
            Divider(Orientation.Vertical, Modifier.fillMaxHeight(), tableBorderColor)
            // Value header with left spacer
            Spacer(Modifier.width(cellHorizontalPadding))
            Column(Modifier.weight(1f)) {
                Text("Value", maxLines = 1, overflow = TextOverflow.Visible)
            }
        }
    }

    class KeyframesConfigurationState(val sequence: TriggerSequence, val trigger: AbstractPlacedTrigger, private val keyframesObject: Keyframes) {
        var currentFocusManager: FocusManager? = null
        var currentCoroutineScope: CoroutineScope? = null
        var keyframeStates = mutableStateListOf<KeyframeState>()
        var selectedKeyframeState by mutableStateOf<KeyframeState?>(null)

        fun handlePositionFormatChange() {
            // update the position input field states by reimporting from the keyframe object
            importFromKeyframesObject()
        }

        fun handlePositionValueChange(changedKeyframeState: KeyframeState): Boolean {
            // update whether the distance to other keyframes is valid for all keyframes
            // (This is O(n^2) and could/should be optimized! Especially since we are already doing the work of having
            // the list sorted. For example, by recursively checking neighbors. But realistically, you'll never have
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
            // if ordered correctly export position to the keyframe object
            if(orderedCorrectly) {
                // export position to the keyframe object
                changedKeyframeState.exportPositionToKeyframeObject(selectedPositionFormat, trigger)
            } else {
                // otherwise sort keyframe states
                keyframeStates.sortBy {
                    it.positionNumberFieldState.value ?: error("Position value may not be for sorting")
                }
                // and then export all positions to the keyframe object
                keyframeStates.forEach { it.exportPositionToKeyframeObject(selectedPositionFormat, trigger) }
                // copy order from keyframe states to the keyframes object
                keyframeStates.forEachIndexed { index, keyframeState ->
                    keyframesObject.keyframesList[index] = keyframeState.keyframeObject ?: error("Keyframe object may not be null")
                }
                // move focus to the new field
                changedKeyframeState.moveFocusToPositionField()
            }
            // redraw timeline so changes are visible
            redrawTimeline()
            // save to file
            saveChangesToFile()
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

        fun addKeyframeAboveSelected() = addKeyframeAboveBellowSelected(true)
        fun addKeyframeBellowSelected() = addKeyframeAboveBellowSelected(false)
        private fun addKeyframeAboveBellowSelected(above: Boolean) {
            // get current index
            val selectedKeyframeIndex = keyframeStates.indexOf(selectedKeyframeState)
            check(selectedKeyframeIndex != -1) { "Selected keyframe state should not be null!" }
            // calculate insertion index
            val insertionIndex = if(above) selectedKeyframeIndex else selectedKeyframeIndex + 1
            // remember which field is focused
            val valueFieldWasFocused = selectedKeyframeState?.valueFieldFocused ?: false
            // insert keyframe in keyframes object
            keyframesObject.insertNewAtIndex(insertionIndex)
            // reimport keyframes so the change is reflected here
            importFromKeyframesObject()
            // move focus to the new keyframe
            currentFocusManager?.clearFocus()
            if(valueFieldWasFocused) {
                keyframeStates[insertionIndex].moveFocusToValueField()
            } else {
                keyframeStates[insertionIndex].moveFocusToPositionField()
            }
            selectedKeyframeState = keyframeStates[insertionIndex]
        }

        fun removeSelectedKeyframe() {
            // get current index
            val selectedKeyframeIndex = keyframeStates.indexOf(selectedKeyframeState)
            check(selectedKeyframeIndex != -1) { "Selected keyframe state should not be null!" }
            // remove keyframe in keyframes object
            keyframesObject.removeAtIndex(selectedKeyframeIndex)
            // reimport keyframes so the change is reflected here
            importFromKeyframesObject()
            // clear the selected keyframe, because the removed keyframe should not be selected anymore
            clearSelectedKeyframe()
        }

        fun clearSelectedKeyframe() {
            currentFocusManager?.clearFocus()
            selectedKeyframeState = null
        }

        private var lastKnownLine: TriggerSequenceLine? = null
        private var lastKnownTriggerIndex: Int? = null
        private fun getTriggersLine(): TriggerSequenceLine? {
            // check whether the trigger is in the same line as the last known line
            lastKnownLine?.let { lastLine ->
                lastKnownTriggerIndex?.let { lastIndex ->
                    if(lastLine.getTriggerByIndex(lastIndex) == trigger) {
                        return lastLine
                    }
                }
            }
            // otherwise we need to find the line again
            val pair = sequence.findLineAndIndexOf(trigger) ?: return null
            lastKnownLine = pair.first
            lastKnownTriggerIndex = pair.second
            return lastKnownLine
        }

        fun saveChangesToFile() {
            val line = getTriggersLine() ?: error("Line of trigger not found")
            line.saveToFile() // in future this should be debounced to avoid too many writing operations
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
        val positionFieldValid by derivedStateOf { positionNumberFieldState.isValid && positionDistanceValid }

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
            // calculate the required distance to other keyframes
            val requiredDistance = when(format) {
                PositionFormat.Proportional -> Keyframes.MINIMUM_POSITION_DISTANCE_IN_SECONDS / trigger.duration
                PositionFormat.Relative, PositionFormat.Absolute -> Keyframes.MINIMUM_POSITION_DISTANCE_IN_SECONDS
            }
            // check that no other keyframe is to close to our position
            return parent.keyframeStates.filter { it != this }.none {
                val otherPositionFieldValue = it.positionNumberFieldState.value ?: return@none false
                (ownPositionFieldValue - otherPositionFieldValue).absoluteValue < requiredDistance
            }
        }

        fun updatePositionDistanceValid(format: PositionFormat, trigger: AbstractPlacedTrigger) {
            positionDistanceValid = isPositionDistanceValid(format, trigger)
        }

        fun isFirstKeyframe() = this == parent.keyframeStates.first()
        fun isLastKeyframe() = this == parent.keyframeStates.last()
        fun isFirstOrLastKeyframe() = isFirstKeyframe() || isLastKeyframe()

        fun moveFocusToPositionField(alsoMoveFocusInsideTextField: Boolean = !isFirstKeyframe() && !isLastKeyframe()) = moveFocusToField(
            fieldFocusRequester = positionFieldFocusRequester,
            alsoMoveFocusInsideTextField = alsoMoveFocusInsideTextField
        )
        fun moveFocusToValueField(alsoMoveFocusInsideTextField: Boolean = true) = moveFocusToField(
            fieldFocusRequester = valueFieldFocusRequester,
            alsoMoveFocusInsideTextField = alsoMoveFocusInsideTextField
        )
        private fun moveFocusToField(
            fieldFocusRequester: FocusRequester,
            alsoMoveFocusInsideTextField: Boolean = true
        ) {
            fieldFocusRequester.requestFocus()
            parent.currentCoroutineScope?.launch {
                delay(1)
                fieldFocusRequester.requestFocus()
                if (alsoMoveFocusInsideTextField) {
                    parent.currentFocusManager?.moveFocus(FocusDirection.Next)
                }
            }
        }

        fun importPositionFromKeyframeObject(format: PositionFormat, trigger: AbstractPlacedTrigger) {
            keyframeObject.let { kObj ->
                check(kObj != null) { "keyframeObject may not be null when updating position from keyframe" }
                positionNumberFieldState.textFieldState.setTextAndPlaceCursorAtEnd(when(format) {
                    PositionFormat.Proportional -> (kObj.position * 100_000).roundToInt() / 100_000.0
                    PositionFormat.Relative -> (kObj.relativeSecondPosition(trigger) * 1000).roundToInt() / 1000.0
                    PositionFormat.Absolute -> (kObj.absoluteSecondPosition(trigger) * 1000).roundToInt() / 1000.0
                }.toString())
                // set valid range if not first or last keyframe, which are not editable anyway
                if(!isFirstKeyframe() && !isLastKeyframe()) {
                    // set valid range for position field
                    positionNumberFieldState.validRange = when(format) {
                        PositionFormat.Proportional -> 0.0..1.0
                        PositionFormat.Relative -> 0.0..trigger.duration
                        PositionFormat.Absolute -> trigger.startTime..trigger.startTime+trigger.duration
                    }
                } else {
                    positionNumberFieldState.validRange = Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY
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
            // set position on the keyframe object
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

    companion object {
        private var tableExpanded by mutableStateOf(false)
        private var selectedPositionFormat by mutableStateOf(PositionFormat.Relative)
        private var reimportKeyframeTableCounter by mutableStateOf(0)

        /**
         * Can be used to manually trigger a reimport of the keyframe table, when
         * changing attributes of a trigger, which are not wrapped in a state.
         */
        fun reimportKeyframeTable() {
            reimportKeyframeTableCounter++
        }
    }

}