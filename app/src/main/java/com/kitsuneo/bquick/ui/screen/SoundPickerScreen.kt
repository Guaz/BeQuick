package com.kitsuneo.bquick.ui.screen

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.audio.AppSoundPlayer
import com.kitsuneo.bquick.settings.BuiltInSound
import com.kitsuneo.bquick.settings.SoundSelection
import com.kitsuneo.bquick.ui.component.BQuickCard
import com.kitsuneo.bquick.ui.component.BQuickButton
import com.kitsuneo.bquick.ui.component.ScreenFrame
import com.kitsuneo.bquick.ui.theme.BQuickTheme

@Composable
fun SoundPickerScreen(
    title: String,
    currentSelection: SoundSelection,
    importedSounds: List<SoundSelection.Custom>,
    onBack: () -> Unit,
    onSelectSound: (SoundSelection) -> Unit,
    onImportSound: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customSounds = remember(importedSounds, currentSelection) {
        buildList {
            if (currentSelection is SoundSelection.Custom) {
                add(currentSelection)
            }
            importedSounds.forEach { candidate ->
                if (none { it.uri == candidate.uri }) {
                    add(candidate)
                }
            }
        }
    }
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
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
            } ?: context.getString(R.string.selected_media)
            onImportSound(uri.toString(), label)
        }
    }

    DisposableEffect(Unit) {
        onDispose { AppSoundPlayer.release() }
    }

    ScreenFrame(
        title = title,
        subtitle = stringResource(R.string.sound_picker_subtitle),
        modifier = modifier,
        onBack = onBack
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            BQuickButton(
                text = stringResource(R.string.sound_picker_load_from_phone),
                onClick = { pickerLauncher.launch(arrayOf("audio/*")) }
            )
        }

        SoundSectionCard(
            title = stringResource(R.string.sound_picker_bundled)
        ) {
            BuiltInSound.entries.forEach { sound ->
                val selection = SoundSelection.BuiltIn(sound)
                SoundOptionRow(
                    label = stringResource(sound.labelRes),
                    isSelected = currentSelection == selection,
                    onPlay = { AppSoundPlayer.play(context, selection) },
                    onSelect = { onSelectSound(selection) }
                )
            }
        }

        SoundSectionCard(
            title = stringResource(R.string.sound_picker_imported)
        ) {
            if (customSounds.isEmpty()) {
                Text(
                    text = stringResource(R.string.sound_picker_empty_custom),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                customSounds.forEach { sound ->
                    SoundOptionRow(
                        label = sound.label,
                        isSelected = currentSelection == sound,
                        onPlay = { AppSoundPlayer.play(context, sound) },
                        onSelect = { onSelectSound(sound) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    BQuickCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space1 + dimensions.space05)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
private fun SoundOptionRow(
    label: String,
    isSelected: Boolean,
    onPlay: () -> Unit,
    onSelect: () -> Unit
) {
    val dimensions = BQuickTheme.dimensions
    BQuickCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.space1 + dimensions.space05),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BQuickButton(
                text = stringResource(R.string.play),
                onClick = onPlay
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}
