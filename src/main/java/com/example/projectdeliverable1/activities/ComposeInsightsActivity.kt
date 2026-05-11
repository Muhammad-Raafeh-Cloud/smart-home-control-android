package com.example.projectdeliverable1.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectdeliverable1.data.AuthRepository
import com.example.projectdeliverable1.data.DeviceRepository

class ComposeInsightsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CloudInsightsScreen(onBack = { finish() })
                }
            }
        }
    }

    @Composable
    private fun CloudInsightsScreen(onBack: () -> Unit) {
        var totalDevices by remember { mutableStateOf(0) }
        var activeDevices by remember { mutableStateOf(0) }
        var status by remember { mutableStateOf("Loading local and cloud session data...") }
        val user = AuthRepository.getCurrentUser()

        LaunchedEffect(Unit) {
            try {
                DeviceRepository.initializeIfNeeded(this@ComposeInsightsActivity)
                val counts = DeviceRepository.getCounts(this@ComposeInsightsActivity)
                totalDevices = counts.first
                activeDevices = counts.second
                status = "Compose screen loaded successfully from Assignment 4."
            } catch (exception: Exception) {
                status = exception.message ?: "Unable to load device summary."
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7FB))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Jetpack Compose Insights",
                color = Color(0xFF1B2A41),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modern UI screen required in Assignment 4.",
                color = Color(0xFF5F6B7A),
                fontSize = 15.sp
            )

            InsightCard(title = "Firebase User", value = user?.email ?: "Signed in", subtitle = "Session persists after restart")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(title = "Devices", value = totalDevices.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Active", value = activeDevices.toString(), modifier = Modifier.weight(1f))
            }

            InsightCard(
                title = "Self-Researched Features",
                value = "DataStore + WorkManager",
                subtitle = "Theme preference persistence and proactive health-check notification"
            )

            Text(text = status, color = Color(0xFF5F6B7A), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back to Dashboard")
            }
        }
    }

    @Composable
    private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = title, color = Color(0xFF5F6B7A), fontSize = 14.sp)
                Text(text = value, color = Color(0xFF1B2A41), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun InsightCard(title: String, value: String, subtitle: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = title, color = Color(0xFF5F6B7A), fontSize = 14.sp)
                Text(text = value, color = Color(0xFF1B2A41), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color(0xFF5F6B7A), fontSize = 14.sp)
            }
        }
    }
}
