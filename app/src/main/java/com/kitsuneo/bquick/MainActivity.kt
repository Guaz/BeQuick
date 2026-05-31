package com.kitsuneo.bquick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import com.kitsuneo.bquick.ui.theme.BQuickTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundSettingsRepository.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            BQuickTheme {
                BQuickApp()
            }
        }
    }
}
