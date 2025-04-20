package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.DoubleNumberFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.IntNumberFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.InvalidInputPopup
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.NumberField
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.calculateWindowSizeFromTargetDuration
import eu.florian_fuhrmann.musictimedtriggers.utils.number.isPowerOf2
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.RadioButtonRow
import org.jetbrains.jewel.ui.component.Text
import kotlin.math.roundToInt

class SpectrogramConfigurationState(
    /**
     * Original spectrogram parameters, used to determine if any changes were made.
     */
    private var originalSpectrogramParameters: SpectrogramParameters
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

    /**
     * Sets the original spectrogram parameters to the current state. Should be called when changes have been applied.
     */
    fun handleChangesApplied() {
        originalSpectrogramParameters = spectrogramParameters.copy()
        anyChanges = false
    }

    internal var windowSizeInputValid by mutableStateOf(true)
    internal var overlapFactorValid by mutableStateOf(true)
    internal var maxAmpRangeValid by mutableStateOf(true)
    internal var yAxisValid by mutableStateOf(true)
    val allInputsValid by derivedStateOf { windowSizeInputValid && overlapFactorValid && maxAmpRangeValid && yAxisValid }
}

@Composable
fun SpectrogramConfigurationPane(
    state: SpectrogramConfigurationState,
    referenceSampleRate: Float?
) {
    Column {
        // Window Size
        WindowSizeConfigurationRows(state, referenceSampleRate)
        Spacer(Modifier.fillMaxWidth().height(8.dp))
        // Window Function
        WindowFunctionConfigurationRows(state)
        Spacer(Modifier.fillMaxWidth().height(8.dp))
        // Overlap Factor
        OverlapFactorConfigurationRows(state)
        Spacer(Modifier.fillMaxWidth().height(8.dp))
        // Max Amp Range
        MaxAmpRangeConfigurationRows(state)
        Spacer(Modifier.fillMaxWidth().height(8.dp))
        // Y-Axis
        YAxisConfigurationRows(state)
        Spacer(Modifier.fillMaxWidth().height(8.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WindowSizeConfigurationRows(spectrogramConfigurationState: SpectrogramConfigurationState, referenceSampleRate: Float?) {
    // Header
    Row {
        Text("Window Size:")
    }
    // Selected Option State
    var fromDurationSelected by remember { mutableStateOf(spectrogramConfigurationState.spectrogramParameters.calculateWindowSizeFromDuration) }

    // Target Duration State
    val targetDurationState = remember {
        IntNumberFieldState(
            initialValue = (spectrogramConfigurationState.spectrogramParameters.windowDurationInSeconds * 1000).roundToInt(),
            validRange = 5..500
        )
    }
    FlowRow(modifier = Modifier.padding(start = 20.dp), verticalArrangement = Arrangement.Center) {
        Column(Modifier.width(IntrinsicSize.Max).fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
            // Target Duration Option
            RadioButtonRow(
                text = "Target Duration",
                selected = fromDurationSelected,
                onClick = { fromDurationSelected = true }
            )
        }
        Column(Modifier.width(IntrinsicSize.Min).fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
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
        }
        Column(Modifier.width(IntrinsicSize.Max).fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
            // Duration and sample count calculated from target duration
            if(referenceSampleRate != null && targetDurationState.value != null && targetDurationState.isValid) {
                Row(Modifier.padding(start = 24.dp).height(IntrinsicSize.Max)) {
                    val windowSize = targetDurationState.value.let {
                        if (it != null)
                            calculateWindowSizeFromTargetDuration(it / 1000.0, referenceSampleRate)
                        else null
                    }
                    val actualDurationInMilliseconds = windowSize?.let { (it / referenceSampleRate * 1000).roundToInt() }
                    Text(
                        "Actual: ${actualDurationInMilliseconds}ms (${windowSize} samples)".replace(' ', '\u00A0'),
                        fontSize = 0.8.em,
                        color = JewelTheme.globalColors.text.disabled
                    )
                }
            }
        }
    }

    // Fixed Size Option
    // Fixed Size States
    val fixedSamplesCountState = remember {
        IntNumberFieldState(
            initialValue = spectrogramConfigurationState.spectrogramParameters.windowSize,
            validRange = 16..65536
        )
    }
    val fixedSamplesCountValid by derivedStateOf { fixedSamplesCountState.isValid && fixedSamplesCountState.value?.isPowerOf2() ?: false }
    FlowRow(modifier = Modifier.padding(start = 20.dp)) {
        Column(Modifier.width(IntrinsicSize.Max).fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
            // Radio Button
            RadioButtonRow(
                text = "Fixed",
                selected = !spectrogramConfigurationState.spectrogramParameters.calculateWindowSizeFromDuration,
                onClick = { fromDurationSelected = false }
            )
        }
        Column(Modifier.width(IntrinsicSize.Max).fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
            // Fixed Size Input with Label
            Row(Modifier.padding(start = 24.dp).height(IntrinsicSize.Min)) {
                Column {
                    if(fixedSamplesCountState.isValid && !fixedSamplesCountValid) {
                        // show power of 2 error (because we know it's not a range error since the number field state is valid)
                        InvalidInputPopup("must be power of 2")
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
        }
        Column(Modifier.width(IntrinsicSize.Max).fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
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
        }
    }

    // Export State back to spectrogramParameters
    LaunchedEffect(Unit) {
        snapshotFlow { fromDurationSelected }.collect {
            spectrogramConfigurationState.spectrogramParameters.calculateWindowSizeFromDuration = it
            spectrogramConfigurationState.refreshAnyChangesState()
        }
    }
    LaunchedEffect(targetDurationState) {
        snapshotFlow { targetDurationState.value }.collect { targetDuration ->
            if(!targetDurationState.isValid || targetDuration == null) return@collect
            spectrogramConfigurationState.spectrogramParameters.windowDurationInSeconds = targetDuration / 1000.0
            spectrogramConfigurationState.refreshAnyChangesState()
        }
    }
    LaunchedEffect(fixedSamplesCountState) {
        snapshotFlow { fixedSamplesCountState.value }.collect { samplesCount ->
            if(!fixedSamplesCountState.isValid || samplesCount == null) return@collect
            if(!samplesCount.isPowerOf2()) return@collect
            spectrogramConfigurationState.spectrogramParameters.windowSize = samplesCount
            spectrogramConfigurationState.refreshAnyChangesState()
        }
    }
    // export whether this input is valid
    LaunchedEffect(Unit) {
        snapshotFlow { (fromDurationSelected && targetDurationState.isValid) || (!fromDurationSelected && fixedSamplesCountValid) }.collect {
            spectrogramConfigurationState.windowSizeInputValid = it
        }
    }
}

@Composable
fun WindowFunctionConfigurationRows(spectrogramConfigurationState: SpectrogramConfigurationState) {
    // Header
    Row {
        Text("Window Function:")
    }
    // Hamming Window Function Selected State
    var hammingSelected by remember { mutableStateOf(spectrogramConfigurationState.spectrogramParameters.useHammingWindow) }

    // Hamming Window Option
    RadioButtonRow(
        modifier = Modifier.padding(start = 20.dp),
        text = "Hamming",
        selected = hammingSelected,
        onClick = { hammingSelected = true }
    )
    // Rectangle Option
    RadioButtonRow(
        modifier = Modifier.padding(start = 20.dp),
        text = "Rectangle",
        selected = !hammingSelected,
        onClick = { hammingSelected = false }
    )

    // Export State back to spectrogramParameters
    LaunchedEffect(Unit) {
        snapshotFlow { hammingSelected }.collect {
            spectrogramConfigurationState.spectrogramParameters.useHammingWindow = it
            spectrogramConfigurationState.refreshAnyChangesState()
        }
    }
}

@Composable
fun OverlapFactorConfigurationRows(spectrogramConfigurationState: SpectrogramConfigurationState) {
    // Overlap Factor State
    val overlapFactorState = remember {
        IntNumberFieldState(
            initialValue = spectrogramConfigurationState.spectrogramParameters.overlapFactor,
            validRange = 1..256
        )
    }
    // Overlap Factor Input with Label
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(Modifier.fillMaxHeight().padding(end = 6.dp), Arrangement.Center) {
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
            spectrogramConfigurationState.spectrogramParameters.overlapFactor = overlapFactor
            spectrogramConfigurationState.refreshAnyChangesState()
        }
    }
    //export whether this input is valid
    LaunchedEffect(Unit) {
        snapshotFlow { overlapFactorState.isValid }.collect {
            spectrogramConfigurationState.overlapFactorValid = it
        }
    }
}

// Maybe remove this option in the future?
@Composable
fun MaxAmpRangeConfigurationRows(spectrogramConfigurationState: SpectrogramConfigurationState) {
    // Max Amp Range State
    val maxAmpRangeState = remember {
        IntNumberFieldState(
            initialValue = spectrogramConfigurationState.spectrogramParameters.maxRange,
            validRange = 1..99999
        )
    }
    // Max Amp Range Input with Label
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(Modifier.fillMaxHeight().padding(end = 6.dp), Arrangement.Center) {
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
            spectrogramConfigurationState.spectrogramParameters.maxRange = maxAmpRange
            spectrogramConfigurationState.refreshAnyChangesState()
        }
    }
    //export whether this input is valid
    LaunchedEffect(Unit) {
        snapshotFlow { maxAmpRangeState.isValid }.collect {
            spectrogramConfigurationState.maxAmpRangeValid = it
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YAxisConfigurationRows(spectrogramConfigurationState: SpectrogramConfigurationState) {
    // Header
    Row {
        Text("Y-Axis:")
    }
    // Logarithmic Axis Selected State
    var log10YAxisSelected by remember { mutableStateOf(spectrogramConfigurationState.spectrogramParameters.log10YAxis) }
    // Log10 Y-Axis Scale State
    val log10YAxisScaleState = remember {
        DoubleNumberFieldState(
            initialValue = spectrogramConfigurationState.spectrogramParameters.log10YAxisLengthFactor,
            validRange = 0.1..10.0
        )
    }
    // Linear and Logarithmic Axis Options
    RadioButtonRow(
        modifier = Modifier.padding(start = 20.dp),
        text = "Linear",
        selected = !log10YAxisSelected,
        onClick = { log10YAxisSelected = false }
    )
    FlowRow(modifier = Modifier.padding(start = 20.dp)) {
        Column(Modifier.fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
            RadioButtonRow(
                text = "Logarithmic",
                selected = log10YAxisSelected,
                onClick = { log10YAxisSelected = true }
            )
        }
        Column(Modifier.fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
            // Y-Axis Scale Factor Input with Label
            Row(Modifier.padding(start = 24.dp).height(IntrinsicSize.Min)) {
                Column(Modifier.fillMaxHeight().padding(end = 6.dp), Arrangement.Center) {
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
        }
    }
    // Export State back to spectrogramParameters
    LaunchedEffect(Unit) {
        snapshotFlow { log10YAxisSelected }.collect {
            spectrogramConfigurationState.spectrogramParameters.log10YAxis = it
            spectrogramConfigurationState.refreshAnyChangesState()
        }
    }
    LaunchedEffect(log10YAxisScaleState) {
        snapshotFlow { log10YAxisScaleState.value }.collect { scale ->
            if(!log10YAxisScaleState.isValid || scale == null) return@collect
            spectrogramConfigurationState.spectrogramParameters.log10YAxisLengthFactor = scale
            spectrogramConfigurationState.refreshAnyChangesState()
        }
    }
    // export whether this input is valid
    LaunchedEffect(Unit) {
        snapshotFlow { !log10YAxisSelected || log10YAxisScaleState.isValid }.collect {
            spectrogramConfigurationState.yAxisValid = it
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