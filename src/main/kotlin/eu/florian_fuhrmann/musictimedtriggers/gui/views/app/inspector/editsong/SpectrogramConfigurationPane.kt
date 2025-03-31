package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.PopupProperties
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.DoubleNumberFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.IntNumberFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.NumberField
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.calculateWindowSizeFromTargetDuration
import eu.florian_fuhrmann.musictimedtriggers.utils.number.isPowerOf2
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.PopupContainer
import org.jetbrains.jewel.ui.component.RadioButtonRow
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.popupContainerStyle
import kotlin.math.roundToInt

class SpectrogramConfigurationState(
    /**
     * Original spectrogram parameters, used to determine if any changes were made.
     */
    private val originalSpectrogramParameters: SpectrogramParameters
) {
    /**
     * Spectrogram parameters as modified by the user.
     */
    val spectrogramParameters = originalSpectrogramParameters.copy()

    /**
     * State to determine if any changes were made to the spectrogram parameters.
     * This is used to enable/disable the "Apply" button in the UI.
     * Needs to be manually updated by calling [refreshAnyChangesState] after any changes.
     */
    var anyChanges by mutableStateOf(false)

    /**
     * Updates the state of anyChanges based on the current spectrogram parameters.
     */
    fun refreshAnyChangesState() {
        anyChanges = spectrogramParameters.sha256Hash() != originalSpectrogramParameters.sha256Hash()
    }
}

@Composable
fun SpectrogramConfigurationPane(
    state: SpectrogramConfigurationState,
    referenceSampleRate: Float?
) {
    Column {
        // Window Size
        WindowSizeConfigurationRows(state.spectrogramParameters, referenceSampleRate) { state.refreshAnyChangesState() }
        Spacer(Modifier.fillMaxWidth().height(8.dp))
        // Window Function
        WindowFunctionConfigurationRows(state.spectrogramParameters) { state.refreshAnyChangesState() }
        Spacer(Modifier.fillMaxWidth().height(8.dp))
        // Overlap Factor
        OverlapFactorConfigurationRows(state.spectrogramParameters) { state.refreshAnyChangesState() }
        Spacer(Modifier.fillMaxWidth().height(8.dp))
        // Max Amp Range
        MaxAmpRangeConfigurationRows(state.spectrogramParameters) { state.refreshAnyChangesState() }
        Spacer(Modifier.fillMaxWidth().height(8.dp))
        // Y-Axis
        YAxisConfigurationRows(state.spectrogramParameters) { state.refreshAnyChangesState() }
        Spacer(Modifier.fillMaxWidth().height(8.dp))
    }
}

@Composable
fun WindowSizeConfigurationRows(spectrogramParameters: SpectrogramParameters, referenceSampleRate: Float?, refreshAnyChangesState: () -> Unit) {
    // Header
    Row {
        Text("Window Size:")
    }
    // Selected Option State
    var fromDurationSelected by remember { mutableStateOf(spectrogramParameters.calculateWindowSizeFromDuration) }

    // Target Duration Option
    RadioButtonRow(
        text = "Target Duration",
        selected = fromDurationSelected,
        onClick = { fromDurationSelected = true }
    )
    // Target Duration State
    val targetDurationState = remember {
        IntNumberFieldState(
            initialValue = (spectrogramParameters.windowDurationInSeconds * 1000).roundToInt(),
            validRange = 5..500
        )
    }
    // Target Duration Input with Label
    Row(Modifier.padding(start = 24.dp).height(IntrinsicSize.Min)) {
        Column {
            NumberField(
                state = targetDurationState,
                modifier = Modifier.width(60.dp),
                enabled = fromDurationSelected
            )
        }
        Column(Modifier.padding(start = 2.dp).fillMaxHeight(), Arrangement.Center) {
            Text("milliseconds", maxLines = 1)
        }
    }
    // Duration and sample count calculated from target duration
    if(referenceSampleRate != null && targetDurationState.value != null && targetDurationState.isValid) {
        Row(Modifier.padding(start = 24.dp)) {
            val windowSize = targetDurationState.value.let {
                if (it != null)
                    calculateWindowSizeFromTargetDuration(it / 1000.0, referenceSampleRate)
                else null
            }
            val actualDurationInMilliseconds = windowSize?.let { (it / referenceSampleRate * 1000).roundToInt() }
            Text(
                "Actual: ${actualDurationInMilliseconds}ms (${windowSize} samples)",
                fontSize = 0.8.em,
                color = JewelTheme.globalColors.text.disabled
            )
        }
    }

    // Fixed Size Option
    // Radio Button
    RadioButtonRow(
        text = "Fixed",
        selected = !spectrogramParameters.calculateWindowSizeFromDuration,
        onClick = { fromDurationSelected = false }
    )
    // Fixed Size States
    val fixedSamplesCountState = remember {
        IntNumberFieldState(
            initialValue = spectrogramParameters.windowSize,
            validRange = 16..65536
        )
    }
    val fixedSamplesCountValid by derivedStateOf { fixedSamplesCountState.isValid && fixedSamplesCountState.value?.isPowerOf2() ?: false }
    // Fixed Size Input with Label
    Row(Modifier.padding(start = 24.dp).height(IntrinsicSize.Min)) {
        Column {
            if(fixedSamplesCountState.isValid && !fixedSamplesCountValid) {
                // show power of 2 error (because we know it's not a range error since the number field state is valid)
                PopupContainer(
                    onDismissRequest = {},
                    horizontalAlignment = Alignment.Start,
                    popupProperties = PopupProperties(focusable = false)
                ) {
                    Box(modifier = Modifier.background(JewelTheme.globalColors.outlines.error).padding(5.dp)) {
                        Text("must be power of 2", color = JewelTheme.globalColors.text.error)
                    }
                }
            }
            NumberField(
                state = fixedSamplesCountState,
                modifier = Modifier.width(60.dp),
                enabled = !fromDurationSelected,
                outline = if(fixedSamplesCountValid) Outline.None else Outline.Error
            )
        }
        Column(Modifier.padding(start = 2.dp).fillMaxHeight(), Arrangement.Center) {
            Text("samples", maxLines = 1)
        }
    }
    // Duration calculated from fixed size
    if(referenceSampleRate != null && fixedSamplesCountState.value != null && fixedSamplesCountState.isValid && fixedSamplesCountValid) {
        Row(Modifier.padding(start = 24.dp)) {
            val actualDurationInMilliseconds = fixedSamplesCountState.value?.let { (it / referenceSampleRate * 1000).roundToInt() }
            Text(
                "Duration: ${actualDurationInMilliseconds}ms",
                fontSize = 0.8.em,
                color = JewelTheme.globalColors.text.disabled
            )
        }
    }

    // Export State back to spectrogramParameters
    LaunchedEffect(Unit) {
        snapshotFlow { fromDurationSelected }.collect {
            spectrogramParameters.calculateWindowSizeFromDuration = it
            println("spectrogramParameters.calculateWindowSizeFromDuration = ${spectrogramParameters.calculateWindowSizeFromDuration}")
            refreshAnyChangesState()
        }
    }
    LaunchedEffect(targetDurationState) {
        snapshotFlow { targetDurationState.value }.collect { targetDuration ->
            if(!targetDurationState.isValid || targetDuration == null) return@collect
            spectrogramParameters.windowDurationInSeconds = targetDuration / 1000.0
            println("spectrogramParameters.windowDurationInSeconds = ${spectrogramParameters.windowDurationInSeconds}")
            refreshAnyChangesState()
        }
    }
    LaunchedEffect(fixedSamplesCountState) {
        snapshotFlow { fixedSamplesCountState.value }.collect { samplesCount ->
            if(!fixedSamplesCountState.isValid || samplesCount == null) return@collect
            if(!samplesCount.isPowerOf2()) return@collect
            spectrogramParameters.windowSize = samplesCount
            println("spectrogramParameters.windowSize = ${spectrogramParameters.windowSize}")
            refreshAnyChangesState()
        }
    }
}

@Composable
fun WindowFunctionConfigurationRows(spectrogramParameters: SpectrogramParameters, refreshAnyChangesState: () -> Unit) {
    // Header
    Row {
        Text("Window Function:")
    }
    // Hamming Window Function Selected State
    var hammingSelected by remember { mutableStateOf(spectrogramParameters.useHammingWindow) }

    // Hamming Window Option
    RadioButtonRow(
        text = "Hamming",
        selected = hammingSelected,
        onClick = { hammingSelected = true }
    )
    // Rectangle Option
    RadioButtonRow(
        text = "Rectangle",
        selected = !hammingSelected,
        onClick = { hammingSelected = false }
    )

    // Export State back to spectrogramParameters
    LaunchedEffect(Unit) {
        snapshotFlow { hammingSelected }.collect {
            spectrogramParameters.useHammingWindow = it
            println("spectrogramParameters.useHammingWindow = ${spectrogramParameters.useHammingWindow}")
            refreshAnyChangesState()
        }
    }
}

@Composable
fun OverlapFactorConfigurationRows(spectrogramParameters: SpectrogramParameters, refreshAnyChangesState: () -> Unit) {
    // Overlap Factor State
    val overlapFactorState = remember {
        IntNumberFieldState(
            initialValue = spectrogramParameters.overlapFactor,
            validRange = 1..256
        )
    }
    // Overlap Factor Input with Label
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(Modifier.fillMaxHeight().padding(end = 2.dp), Arrangement.Center) {
            Text("Overlap Factor:", maxLines = 1)
        }
        Column {
            NumberField(
                state = overlapFactorState,
                modifier = Modifier.width(60.dp)
            )
        }
    }
    // Export State back to spectrogramParameters
    LaunchedEffect(overlapFactorState) {
        snapshotFlow { overlapFactorState.value }.collect { overlapFactor ->
            if(!overlapFactorState.isValid || overlapFactor == null) return@collect
            spectrogramParameters.overlapFactor = overlapFactor
            println("spectrogramParameters.overlapFactor = ${spectrogramParameters.overlapFactor}")
            refreshAnyChangesState()
        }
    }
}

// Maybe remove this option in the future?
@Composable
fun MaxAmpRangeConfigurationRows(spectrogramParameters: SpectrogramParameters, refreshAnyChangesState: () -> Unit) {
    // Max Amp Range State
    val maxAmpRangeState = remember {
        IntNumberFieldState(
            initialValue = spectrogramParameters.maxRange,
            validRange = 1..1000
        )
    }
    // Max Amp Range Input with Label
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(Modifier.fillMaxHeight().padding(end = 2.dp), Arrangement.Center) {
            Text("Max Amp Range:", maxLines = 1)
        }
        Column {
            NumberField(
                state = maxAmpRangeState,
                modifier = Modifier.width(60.dp)
            )
        }
    }
    // Export State back to spectrogramParameters
    LaunchedEffect(maxAmpRangeState) {
        snapshotFlow { maxAmpRangeState.value }.collect { maxAmpRange ->
            if(!maxAmpRangeState.isValid || maxAmpRange == null) return@collect
            spectrogramParameters.maxRange = maxAmpRange
            println("spectrogramParameters.maxRange = ${spectrogramParameters.maxRange}")
            refreshAnyChangesState()
        }
    }
}

@Composable
fun YAxisConfigurationRows(spectrogramParameters: SpectrogramParameters, refreshAnyChangesState: () -> Unit) {
    // Header
    Row {
        Text("Y-Axis:")
    }
    // Logarithmic Axis Selected State
    var log10YAxisSelected by remember { mutableStateOf(spectrogramParameters.log10YAxis) }
    // Log10 Y-Axis Scale State
    val log10YAxisScaleState = remember {
        DoubleNumberFieldState(
            initialValue = spectrogramParameters.log10YAxisLengthFactor,
            validRange = 0.1..10.0
        )
    }
    // Linear and Logarithmic Axis Options
    RadioButtonRow(
        text = "Linear",
        selected = !log10YAxisSelected,
        onClick = { log10YAxisSelected = false }
    )
    RadioButtonRow(
        text = "Logarithmic",
        selected = log10YAxisSelected,
        onClick = { log10YAxisSelected = true }
    )
    // Y-Axis Scale Factor Input with Label
    Row(Modifier.padding(start = 24.dp).height(IntrinsicSize.Min)) {
        Column(Modifier.fillMaxHeight().padding(end = 2.dp), Arrangement.Center) {
            Text("Scale:", maxLines = 1)
        }
        Column {
            NumberField(
                state = log10YAxisScaleState,
                modifier = Modifier.width(60.dp),
                enabled = log10YAxisSelected
            )
        }
    }
    // Export State back to spectrogramParameters
    LaunchedEffect(Unit) {
        snapshotFlow { log10YAxisSelected }.collect {
            spectrogramParameters.log10YAxis = it
            println("spectrogramParameters.log10YAxis = ${spectrogramParameters.log10YAxis}")
            refreshAnyChangesState()
        }
    }
    LaunchedEffect(log10YAxisScaleState) {
        snapshotFlow { log10YAxisScaleState.value }.collect { scale ->
            if(!log10YAxisScaleState.isValid || scale == null) return@collect
            spectrogramParameters.log10YAxisLengthFactor = scale
            println("spectrogramParameters.log10YAxisLengthFactor = ${spectrogramParameters.log10YAxisLengthFactor}")
            refreshAnyChangesState()
        }
    }
}

@Composable
@Preview
fun SpectrogramConfigurationPanePreview() {
    // Preview
    IntUiTheme(isDark = true) {
        Box(Modifier.background(Color.Black).padding(10.dp).background(JewelTheme.globalColors.panelBackground)) {
            SpectrogramConfigurationPane(
                SpectrogramConfigurationState(SpectrogramParameters()),
                referenceSampleRate = 44100f
            )
        }
    }
}