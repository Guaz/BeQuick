package com.kitsuneo.bquick

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kitsuneo.bquick.alarm.AlarmRepository
import com.kitsuneo.bquick.settings.SoundSettingsRepository
import com.kitsuneo.bquick.ui.theme.BQuickTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        SoundSettingsRepository.applyStoredAppLanguage(this)
        super.onCreate(savedInstanceState)
        AlarmRepository.initialize(applicationContext)
        SoundSettingsRepository.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            BQuickTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BQuickApp()
                }
            }
        }
    }
}
