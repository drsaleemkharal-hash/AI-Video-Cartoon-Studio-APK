package com.example.ui.screens.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioFormat
import com.example.data.model.ScriptLanguage
import com.example.data.model.StoryGenre
import com.example.data.model.VideoModel
import com.example.ui.components.GradientButton
import com.example.ui.components.StudioHeader
import com.example.ui.theme.CyanBlue
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreateStoryScreen(
    viewModel: CreateStoryViewModel,
    onNavigateBack: () -> Unit,
    onStoryCreated: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val premise by viewModel.premise.collectAsState()
    val title by viewModel.title.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val selectedRatio by viewModel.selectedAspectRatio.collectAsState()
    val durationSeconds by viewModel.sceneDurationSeconds.collectAsState()
    val sceneCount by viewModel.sceneCount.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(uiState) {
        if (uiState is CreateUiState.Success) {
            val project = (uiState as CreateUiState.Success).createdProject
            viewModel.resetState()
            onStoryCreated(project.id)
        }
    }

    Scaffold(
        topBar = {
            StudioHeader(
                title = "Create Story & Prompts",
                subtitle = "AI Video & Cartoon Studio Wizard",
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                }
            )
        },
        containerColor = NavyBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Error Banner if needed
            if (uiState is CreateUiState.Error) {
                val error = uiState as CreateUiState.Error
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("error_banner")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notice",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        if (error.canFallbackToLocal) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.generateWithLocalEngine() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("fallback_local_button")
                            ) {
                                Text("Generate with Local Engine (Offline)")
                            }
                        }
                    }
                }
            }

            // 1. Story Topic / Premise Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Story Topic or Premise *",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                OutlinedTextField(
                    value = premise,
                    onValueChange = { viewModel.setPremise(it) },
                    placeholder = {
                        Text(
                            "e.g. Two animated forest friends discover a glowing portal that leads to a futuristic floating city...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface,
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("premise_input")
                )

                // Quick Inspiration Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InspirationChip("🦊 Cartoon Animals") {
                        viewModel.setPremise("A cheeky raccoon and an eccentric owl invent a flying bicycle in an enchanted autumn forest.")
                        viewModel.setGenre(StoryGenre.CARTOON_3D)
                    }
                    InspirationChip("⚡ Cyberpunk Neon") {
                        viewModel.setPremise("A rogue hacker infiltrates a mega-corporation spire in neon-lit cyberpunk city at midnight.")
                        viewModel.setGenre(StoryGenre.CYBERPUNK)
                    }
                    InspirationChip("🚀 Space Odyssey") {
                        viewModel.setPremise("An astronaut crew navigates crystalline rings around Saturn and finds an ancient cosmic beacon.")
                        viewModel.setGenre(StoryGenre.CINEMATIC)
                    }
                    InspirationChip("🏰 Urdu Folk Tale") {
                        viewModel.setPremise("Shahzada aur Jadui Chiragh - Ek bahadur ladka aur uski samajhdar behan purane qile mein dastan khojte hain.")
                        viewModel.setGenre(StoryGenre.DARK_FANTASY)
                        viewModel.setLanguage(ScriptLanguage.BILINGUAL_MULTILINGUAL)
                    }
                }
            }

            // 2. Story Title (Optional)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Project Title (Optional)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.title.value = it },
                    placeholder = { Text("Auto-generated from premise if blank", color = TextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface,
                        focusedBorderColor = CyanBlue,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("title_input")
                )
            }

            // 3. Target AI Video Generator Model
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Target Video AI Model",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Prompts are dynamically formatted to match the chosen model's optimal syntax.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                VideoModel.values().forEach { model ->
                    val isSelected = selectedModel == model
                    val modelColor = Color(model.badgeColorHex)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) modelColor.copy(alpha = 0.15f) else NavySurface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) modelColor else NavyBorder
                            )
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setModel(model) }
                            .testTag("model_card_${model.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(modelColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = modelColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${model.vendor}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Story Genre / Visual Style
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Visual Genre & Art Style",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StoryGenre.values().forEach { genre ->
                        val isSelected = selectedGenre == genre
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricPurple else NavySurface)
                                .border(1.dp, if (isSelected) CyanBlue else NavyBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.setGenre(genre) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("genre_chip_${genre.name}")
                        ) {
                            Text(
                                text = genre.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }

            // 5. Aspect Ratio Format
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Video Format & Aspect Ratio",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioFormat.values().forEach { format ->
                        val isSelected = selectedRatio == format
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CyanBlue.copy(alpha = 0.15f) else NavySurface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSelected) CyanBlue else NavyBorder
                                )
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setAspectRatio(format) }
                                .testTag("ratio_card_${format.name}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = format.ratioString,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) CyanBlue else TextPrimary
                                )
                                Text(
                                    text = format.platformTarget,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 6. Configurable Scene Duration (6 to 10 seconds)
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyBorder)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CyanBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scene Duration",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${durationSeconds}s / scene",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanBlue
                        )
                    }
                    Text(
                        text = "Configurable between 6 and 10 seconds per scene.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Slider(
                        value = durationSeconds.toFloat(),
                        onValueChange = { viewModel.setDuration(it.toInt()) },
                        valueRange = 6f..10f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanBlue,
                            activeTrackColor = CyanBlue,
                            inactiveTrackColor = NavySurfaceVariant
                        ),
                        modifier = Modifier.testTag("duration_slider")
                    )
                }
            }

            // 7. Scene Count (2 to 8)
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyBorder)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scene Count",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "$sceneCount Scenes (~${sceneCount * durationSeconds}s total)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElectricPurple
                        )
                    }
                    Slider(
                        value = sceneCount.toFloat(),
                        onValueChange = { viewModel.setScenes(it.toInt()) },
                        valueRange = 2f..8f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricPurple,
                            activeTrackColor = ElectricPurple,
                            inactiveTrackColor = NavySurfaceVariant
                        ),
                        modifier = Modifier.testTag("scene_count_slider")
                    )
                }
            }

            // 8. Script Language
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dialogue & Script Language",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                ScriptLanguage.values().forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NavySurfaceVariant else NavySurface)
                            .border(1.dp, if (isSelected) ElectricPurple else NavyBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setLanguage(lang) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = lang.flag, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = lang.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CyanBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton(
                    text = "Generate with Gemini AI",
                    onClick = { viewModel.generateWithGemini() },
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "generate_gemini_button"
                )

                OutlinedButton(
                    onClick = { viewModel.generateWithLocalEngine() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = NavySurface,
                        contentColor = TextPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NavyBorder)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("generate_local_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = CyanBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate with Local Engine (Instant / Offline)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Generating Overlay Dialog
        if (uiState is CreateUiState.Generating) {
            val message = (uiState as CreateUiState.Generating).message
            AlertDialog(
                onDismissRequest = { /* Cannot dismiss while generating */ },
                containerColor = NavySurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = ElectricPurple,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Crafting Video Story...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                },
                text = {
                    Column {
                        Text(text = message, color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Synthesizing model prompts for ${selectedModel.displayName}, character consistency tags, and dialogues...",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun InspirationChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NavySurfaceVariant)
            .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = CyanBlue
        )
    }
}
