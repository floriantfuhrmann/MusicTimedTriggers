package eu.florian_fuhrmann.musictimedtriggers.gui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys

class BasicAlert(
    private val type: BasicAlertType = BasicAlertType.Info,
    private val title: String,
    private val buttons: @Composable BasicAlertScope.() -> Unit,
    private val content: @Composable BasicAlertScope.() -> Unit
) : BasicAlertScope {

    @Composable
    fun Content() {
        Row(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, JewelTheme.globalColors.borders.normal, RoundedCornerShape(8.dp))
                .background(JewelTheme.globalColors.panelBackground)
                .padding(20.dp)
                .width(370.dp)
        ) {
            Icon(type.iconKey, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                content.invoke(this@BasicAlert)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Spacer(Modifier.weight(1f))
                    buttons.invoke(this@BasicAlert)
                }
            }
        }
    }

    fun show() {
        AlertsManager.showAlert(this)
    }

    @Composable
    override fun OKButton(onClick: () -> Unit, label: String) {
        DefaultButton(onClick) {
            Text(label)
        }
    }

    @Composable
    override fun CancelButton(onClick: () -> Unit, label: String) {
        OutlinedButton(onClick) {
            Text(label)
        }
    }

    override fun close() {
        AlertsManager.closeAlert(this)
    }
}

enum class BasicAlertType(val iconKey: IconKey) {
    Info(AllIconsKeys.General.InformationDialog),
    Error(AllIconsKeys.General.ErrorDialog),
    Warning(AllIconsKeys.General.WarningDialog),
    Question(AllIconsKeys.General.QuestionDialog)
}

interface BasicAlertScope {
//    @Composable
//    fun OKButton(onClick: () -> Unit = { closeAlert() }, label: String = "OK")
//    @Composable
//    fun CancelButton(onClick: () -> Unit = { closeAlert() }, label: String = "Cancel")
    @Composable
    fun OKButton(onClick: () -> Unit, label: String)
    @Composable
    fun CancelButton(onClick: () -> Unit, label: String)
    fun close()
}
