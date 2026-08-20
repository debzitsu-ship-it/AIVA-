package com.aiva.ui.pip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.aiva.ui.theme.AivaTheme

class PipActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AivaTheme { Text("AIVA") } }
    }
}
