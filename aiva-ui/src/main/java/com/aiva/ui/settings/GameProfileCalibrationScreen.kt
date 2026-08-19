package com.aiva.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Rect
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun GameProfileCalibrationScreen(profileId: String, onBack: () -> Unit) {
    // Calibration state
    val controls = remember { mutableStateOf<GameControlCalibration>(GameControlCalibration()) }
    var currentStep by remember { mutableStateOf(0) }
    var showPreview by remember { mutableStateOf(false) }
    
    val steps = listOf(
        CalibrationStep("movementJoystick", "Movement Joystick", "Drag to set the movement joystick area (usually bottom-left)", Icons.Default.GpsFixed),
        CalibrationStep("aimArea", "Aim Area", "Drag to set the aim/look area (usually right side)", Icons.Default.GpsFixed),
        CalibrationStep("fireButton", "Fire Button", "Tap to set the fire/shoot button position", Icons.Default.RadioButtonChecked),
        CalibrationStep("reloadButton", "Reload Button", "Tap to set the reload button position", Icons.Default.RadioButtonChecked),
        CalibrationStep("jumpButton", "Jump Button", "Tap to set the jump button position", Icons.Default.RadioButtonChecked),
        CalibrationStep("crouchButton", "Crouch/Prone", "Tap to set crouch/prone button", Icons.Default.RadioButtonChecked),
        CalibrationStep("weaponSwitch", "Weapon Switch", "Tap to set weapon switch button", Icons.Default.RadioButtonChecked),
        CalibrationStep("mapButton", "Map", "Tap to set map button", Icons.Default.RadioButtonChecked),
    )
    
    val currentStepData = steps.getOrNull(currentStep)
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Calibrate Controls", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            navigationIcon = { IconButton(onClick = { if (currentStep > 0) currentStep-- else onBack() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
            actions = {
                if (currentStep == steps.size - 1) {
                    Button(onClick = { /* Save calibration */ onBack() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            }
        )
        
        // Progress indicator
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Step ${currentStep + 1} of ${steps.size}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    steps.forEachIndexed { index, step ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(
                                    color = if (index <= currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
        
        // Current step instruction
        currentStepData?.let { step ->
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(step.icon, contentDescription = step.title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text(step.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(step.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
            
            // Calibration area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                CalibrationCanvas(
                    controls = controls,
                    currentStep = currentStep,
                    stepId = step.id,
                    onPositionSet = { x, y, width, height ->
                        setControlPosition(controls, step.id, x, y, width, height)
                        if (currentStep < steps.size - 1) currentStep++
                    }
                )
            }
        }
        
        // Existing controls preview
        if (showPreview && controls.value.hasAnySet()) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Configured Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        controls.value.movementJoystick?.let { 
                            ControlPreviewChip(name = "Movement", bounds = it) 
                        }
                        controls.value.aimArea?.let { 
                            ControlPreviewChip(name = "Aim", bounds = it) 
                        }
                        controls.value.fireButton?.let { 
                            ControlPreviewChip(name = "Fire", bounds = it) 
                        }
                        controls.value.reloadButton?.let { 
                            ControlPreviewChip(name = "Reload", bounds = it) 
                        }
                        controls.value.jumpButton?.let { 
                            ControlPreviewChip(name = "Jump", bounds = it) 
                        }
                        controls.value.crouchButton?.let { 
                            ControlPreviewChip(name = "Crouch", bounds = it) 
                        }
                        controls.value.weaponSwitch?.let { 
                            ControlPreviewChip(name = "Weapon", bounds = it) 
                        }
                        controls.value.mapButton?.let { 
                            ControlPreviewChip(name = "Map", bounds = it) 
                        }
                    }
                }
            }
        }
        
        // Preview toggle
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Show Preview", fontSize = 16.sp)
                androidx.compose.material3.Switch(
                    checked = showPreview,
                    onCheckedChange = { showPreview = it },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

data class CalibrationStep(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class GameControlCalibration(
    val movementJoystick: NormalizedRect? = null,
    val aimArea: NormalizedRect? = null,
    val fireButton: NormalizedRect? = null,
    val reloadButton: NormalizedRect? = null,
    val jumpButton: NormalizedRect? = null,
    val crouchButton: NormalizedRect? = null,
    val proneButton: NormalizedRect? = null,
    val weaponSwitch: NormalizedRect? = null,
    val mapButton: NormalizedRect? = null,
    val inventoryButton: NormalizedRect? = null,
    val interactButton: NormalizedRect? = null,
) {
    fun hasAnySet(): Boolean = movementJoystick != null || aimArea != null || fireButton != null ||
        reloadButton != null || jumpButton != null || crouchButton != null ||
        weaponSwitch != null || mapButton != null || inventoryButton != null || interactButton != null
}

data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val centerX: Float = (left + right) / 2
    val centerY: Float = (top + bottom) / 2
    val width: Float = right - left
    val height: Float = bottom - top
}

@Composable
fun CalibrationCanvas(
    controls: androidx.compose.runtime.MutableState<GameControlCalibration>,
    currentStep: Int,
    stepId: String,
    onPositionSet: (Float, Float, Float, Float) -> Unit
) {
    val canvasSize = remember { mutableStateOf<androidx.compose.ui.geometry.Size>(androidx.compose.ui.geometry.Size.Zero) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .background(Color.Black.copy(alpha = 0.1f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .onSizeChanged { canvasSize.value = it.toSize() }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitFirstDown()
                        val pos = event.changes.first().position
                        val size = canvasSize.value
                        if (size.width > 0 && size.height > 0) {
                            val normalizedX = (pos.x / size.width).coerceIn(0f, 1f)
                            val normalizedY = (pos.y / size.height).coerceIn(0f, 1f)
                            
                            // Default control size
                            val controlWidth = 0.15f
                            val controlHeight = 0.15f
                            
                            onPositionSet(
                                (normalizedX - controlWidth / 2).coerceIn(0f, 1f - controlWidth),
                                (normalizedY - controlHeight / 2).coerceIn(0f, 1f - controlHeight),
                                controlWidth,
                                controlHeight
                            )
                        }
                    }
                }
            }
        ) {
            // Draw existing controls as semi-transparent overlays
            controls.value.movementJoystick?.let { drawControlOverlay(it, Color.Blue) }
            controls.value.aimArea?.let { drawControlOverlay(it, Color.Green) }
            controls.value.fireButton?.let { drawControlOverlay(it, Color.Red) }
            controls.value.reloadButton?.let { drawControlOverlay(it, Color.Orange) }
            controls.value.jumpButton?.let { drawControlOverlay(it, Color.Yellow) }
            controls.value.crouchButton?.let { drawControlOverlay(it, Color.Magenta) }
            controls.value.weaponSwitch?.let { drawControlOverlay(it, Color.Cyan) }
            controls.value.mapButton?.let { drawControlOverlay(it, Color.White) }
            
            // Current step indicator
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun drawControlOverlay(rect: NormalizedRect, color: Color) {
    androidx.compose.ui.graphics.Canvas(modifier = Modifier.fillMaxSize()) {
        val size = this.size
        val left = rect.left * size.width
        val top = rect.top * size.height
        val right = rect.right * size.width
        val bottom = rect.bottom * size.height
        
        drawRect(
            color = color.copy(alpha = 0.3f),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top)
        )
        drawRect(
            color = color.copy(alpha = 0.8f),
            style = androidx.compose.ui.graphics.Stroke(width = 2.dp.toPx()),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top)
        )
    }
}

@Composable
fun ControlPreviewChip(name: String, bounds: NormalizedRect) {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(8.dp, 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("(${String.format("%.0f", bounds.centerX * 100)}%, ${String.format("%.0f", bounds.centerY * 100)}%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

fun setControlPosition(
    controls: androidx.compose.runtime.MutableState<GameControlCalibration>,
    stepId: String,
    x: Float, y: Float, width: Float, height: Float
) {
    val rect = NormalizedRect(x, y, x + width, y + height)
    controls.value = when (stepId) {
        "movementJoystick" -> controls.value.copy(movementJoystick = rect)
        "aimArea" -> controls.value.copy(aimArea = rect)
        "fireButton" -> controls.value.copy(fireButton = rect)
        "reloadButton" -> controls.value.copy(reloadButton = rect)
        "jumpButton" -> controls.value.copy(jumpButton = rect)
        "crouchButton" -> controls.value.copy(crouchButton = rect)
        "proneButton" -> controls.value.copy(proneButton = rect)
        "weaponSwitch" -> controls.value.copy(weaponSwitch = rect)
        "mapButton" -> controls.value.copy(mapButton = rect)
        "inventoryButton" -> controls.value.copy(inventoryButton = rect)
        "interactButton" -> controls.value.copy(interactButton = rect)
        else -> controls.value
    }
}