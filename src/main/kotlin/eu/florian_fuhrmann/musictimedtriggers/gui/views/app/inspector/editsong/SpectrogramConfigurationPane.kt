package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.IntNumberFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.NumberField
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.calculateWindowSizeFromTargetDuration
import eu.florian_fuhrmann.musictimedtriggers.utils.number.isPowerOf2
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.RadioButtonRow
import org.jetbrains.jewel.ui.component.Text
import kotlin.math.roundToInt

@Composable
fun SpectrogramConfigurationPane(spectrogramParameters: SpectrogramParameters, referenceSampleRate: Float?) {
    Column {
        // Window Size
        WindowSizeConfigurationRows(spectrogramParameters, referenceSampleRate)
    }
}

@Composable
fun WindowSizeConfigurationRows(spectrogramParameters: SpectrogramParameters, referenceSampleRate: Float?) {
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
        }
        snapshotFlow { targetDurationState }.collectLatest { state ->
            if(!state.isValid) return@collectLatest
            state.value?.let {
                spectrogramParameters.windowDurationInSeconds = it / 1000.0
                println("spectrogramParameters.windowDurationInSeconds = ${spectrogramParameters.windowDurationInSeconds}")
            }
        }
        snapshotFlow { fixedSamplesCountState }.collectLatest { state ->
            if(!state.isValid) return@collectLatest
            state.value?.let {
                if(!it.isPowerOf2()) return@let
                spectrogramParameters.windowSize = it
                println("spectrogramParameters.windowSize = ${spectrogramParameters.windowSize}")
            }
        }
    }
}