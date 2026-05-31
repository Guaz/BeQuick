package com.kitsuneo.bquick.ui.screen

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitsuneo.bquick.feature.home.HomeUiState
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundTarget
import com.kitsuneo.bquick.ui.component.ScreenFrame

@Composable
fun SettingsScreen(
    state: HomeUiState,
    onBack: () -> Unit,
    onSelectBuiltInSound: (SoundTarget, BuiltInSound) -> Unit,
    onSelectCustomSound: (SoundTarget, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pendingSoundTarget = remember { mutableStateOf<SoundTarget?>(null) }
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val target = pendingSoundTarget.value ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val label = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "Selected media"
            onSelectCustomSound(target, uri.toString(), label)
        }
    }

    ScreenFrame(
        title = "Settings",
        subtitle = "Choose built-in sounds or pick audio from your device for timer events.",
        modifier = modifier,
        onBack = onBack
    ) {
        SoundSettingsCard(
            modeSwitchSoundLabel = state.modeSwitchSoundLabel,
            reactionSoundLabel = state.reactionSoundLabel,
            onSelectBuiltInSound = onSelectBuiltInSound,
            onPickCustomSound = { target ->
                pendingSoundTarget.value = target
                pickerLauncher.launch(arrayOf("audio/*"))
            }
        )
    }
}

@Composable
private fun SoundSettingsCard(
    modeSwitchSoundLabel: String,
    reactionSoundLabel: String,
    onSelectBuiltInSound: (SoundTarget, BuiltInSound) -> Unit,
    onPickCustomSound: (SoundTarget) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SoundTargetSection(
                title = "Mode switch sound",
                currentLabel = modeSwitchSoundLabel,
                target = SoundTarget.ModeSwitch,
                onSelectBuiltInSound = onSelectBuiltInSound,
                onPickCustomSound = onPickCustomSound
            )
            SoundTargetSection(
                title = "Reaction cue sound",
                currentLabel = reactionSoundLabel,
                target = SoundTarget.Reaction,
                onSelectBuiltInSound = onSelectBuiltInSound,
                onPickCustomSound = onPickCustomSound
            )
        }
    }
}

@Composable
private fun SoundTargetSection(
    title: String,
    currentLabel: String,
    target: SoundTarget,
    onSelectBuiltInSound: (SoundTarget, BuiltInSound) -> Unit,
    onPickCustomSound: (SoundTarget) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Current: $currentLabel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BuiltInSound.entries.forEach { sound ->
                Button(
                    onClick = { onSelectBuiltInSound(target, sound) }
                ) {
                    Text(text = sound.label)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { onPickCustomSound(target) }) {
                Text(text = "Choose media")
            }
        }
    }
}
