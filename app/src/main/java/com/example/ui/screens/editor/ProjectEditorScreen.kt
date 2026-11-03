package com.example.ui.screens.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioFormat
import com.example.data.model.CameraMovement
import com.example.data.model.CharacterProfile
import com.example.data.model.DialogueLine
import com.example.data.model.ScenePrompt
import com.example.data.model.StudioProject
import com.example.data.model.VideoModel
import com.example.ui.components.CharacterAvatar
import com.example.ui.components.GlassCard
import com.example.ui.components.ModelBadge
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
import com.example.util.ExportHelper

@Composable
fun ProjectEditorScreen(
    projectId: String,
    viewModel: ProjectEditorViewModel,
    onNavigateBack: () -> Unit,
    onExportProject: (StudioProject) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var editingScene by remember { mutableStateOf<ScenePrompt?>(null) }
    var editingCharacter by remember { mutableStateOf<CharacterProfile?>(null) }
    var showNewCharacterDialog by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val project = (uiState as? EditorUiState.Ready)?.project
            StudioHeader(
                title = project?.title ?: "Studio Director",
                subtitle = project?.let { "${it.targetModel.displayName} • ${it.aspectRatio.ratioString}" },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (project != null) {
                        IconButton(
                            onClick = {
                                val promptsText = ExportHelper.generateAllVisualPrompts(project)
                                ExportHelper.copyToClipboard(context, promptsText, "All Prompts")
                            },
                            modifier = Modifier.testTag("copy_all_prompts_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy All Prompts",
                                tint = CyanBlue
                            )
                        }

                        IconButton(
                            onClick = { onExportProject(project) },
                            modifier = Modifier.testTag("export_project_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export Project",
                                tint = ElectricPurple
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is EditorUiState.Ready && selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { viewModel.addScene() },
                    containerColor = ElectricPurple,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_scene_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Scene"
                    )
                }
            } else if (uiState is EditorUiState.Ready && selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = { showNewCharacterDialog = true },
                    containerColor = CyanBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_character_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Character"
                    )
                }
            }
        },
        containerColor = NavyBackground
    ) { paddingValues ->
        when (val state = uiState) {
            is EditorUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ElectricPurple)
                }
            }

            is EditorUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is EditorUiState.Ready -> {
                val project = state.project
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Model & Format Quick Switcher Toolbar
                    ModelQuickSwitcher(
                        currentModel = project.targetModel,
                        currentRatio = project.aspectRatio,
                        onModelSelected = { viewModel.updateProjectModel(it) },
                        onRatioSelected = { viewModel.updateAspectRatio(it) }
                    )

                    // Navigation Tabs: Scenes, Characters, Screenplay
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = NavySurface,
                        contentColor = TextPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = ElectricPurple
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Scenes (${project.scenes.size})", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Characters (${project.characters.size})", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = { Text("Screenplay & Urdu", fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    // Tab Content
                    when (selectedTabIndex) {
                        0 -> ScenesListTab(
                            project = project,
                            onEditScene = { editingScene = it },
                            onDeleteScene = { viewModel.deleteScene(it.id) },
                            onCopyPrompt = { prompt ->
                                ExportHelper.copyToClipboard(context, prompt, "Scene Prompt")
                            }
                        )
                        1 -> CharactersListTab(
                            characters = project.characters,
                            onEditCharacter = { editingCharacter = it },
                            onDeleteCharacter = { viewModel.deleteCharacter(it.id) },
                            onAddCharacter = { showNewCharacterDialog = true }
                        )
                        2 -> ScreenplayTab(project = project)
                    }
                }

                // Edit Scene Modal Dialog
                if (editingScene != null) {
                    EditSceneDialog(
                        scene = editingScene!!,
                        characters = project.characters,
                        onDismiss = { editingScene = null },
                        onSave = { updated ->
                            viewModel.updateScene(updated)
                            editingScene = null
                        }
                    )
                }

                // Edit Character Modal Dialog
                if (editingCharacter != null) {
                    EditCharacterDialog(
                        character = editingCharacter!!,
                        onDismiss = { editingCharacter = null },
                        onSave = { updated ->
                            viewModel.saveCharacter(updated)
                            editingCharacter = null
                        }
                    )
                }

                // New Character Modal Dialog
                if (showNewCharacterDialog) {
                    EditCharacterDialog(
                        character = CharacterProfile(name = ""),
                        onDismiss = { showNewCharacterDialog = false },
                        onSave = { newChar ->
                            viewModel.saveCharacter(newChar)
                            showNewCharacterDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelQuickSwitcher(
    currentModel: VideoModel,
    currentRatio: AspectRatioFormat,
    onModelSelected: (VideoModel) -> Unit,
    onRatioSelected: (AspectRatioFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavySurfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Model:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                VideoModel.values().forEach { model ->
                    val isSelected = model == currentModel
                    val modelColor = Color(model.badgeColorHex)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) modelColor.copy(alpha = 0.25f) else Color.Transparent)
                            .border(1.dp, if (isSelected) modelColor else NavyBorder, RoundedCornerShape(6.dp))
                            .clickable { onModelSelected(model) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = model.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ratio:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                AspectRatioFormat.values().forEach { ratio ->
                    val isSelected = ratio == currentRatio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyanBlue.copy(alpha = 0.25f) else Color.Transparent)
                            .border(1.dp, if (isSelected) CyanBlue else NavyBorder, RoundedCornerShape(6.dp))
                            .clickable { onRatioSelected(ratio) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ratio.ratioString,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) CyanBlue else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScenesListTab(
    project: StudioProject,
    onEditScene: (ScenePrompt) -> Unit,
    onDeleteScene: (ScenePrompt) -> Unit,
    onCopyPrompt: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(project.scenes, key = { it.id }) { scene ->
            SceneCardItem(
                scene = scene,
                targetModel = project.targetModel,
                characters = project.characters,
                onEdit = { onEditScene(scene) },
                onDelete = { onDeleteScene(scene) },
                onCopyPrompt = { onCopyPrompt(scene.visualPrompt) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun SceneCardItem(
    scene: ScenePrompt,
    targetModel: VideoModel,
    characters: List<CharacterProfile>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopyPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "scene_card_${scene.sceneNumber}"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Scene #, Title, Duration (6-10s) & Edit/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ElectricPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${scene.sceneNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = scene.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CyanBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${scene.durationSeconds}s",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${scene.cameraMovement.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Scene",
                            tint = CyanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Scene",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Setting & Lighting
            Text(
                text = "📍 Setting: ${scene.settingDescription}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            // Visual Prompt Box with dedicated Copy Button
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(targetModel.badgeColorHex).copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROMPT FOR ${targetModel.displayName.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(targetModel.badgeColorHex)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavySurfaceVariant)
                                .clickable { onCopyPrompt() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("copy_prompt_button_${scene.sceneNumber}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = CyanBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = scene.visualPrompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Voice-Over script
            if (scene.voiceOverEnglish.isNotBlank() || scene.voiceOverRomanUrdu.isNotBlank() || scene.voiceOverUrdu.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurface)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Voice-Over Narration",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricPurple
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (scene.voiceOverEnglish.isNotBlank()) {
                        Text(
                            text = "EN: ${scene.voiceOverEnglish}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                    if (scene.voiceOverRomanUrdu.isNotBlank()) {
                        Text(
                            text = "Roman: ${scene.voiceOverRomanUrdu}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanBlue
                        )
                    }
                    if (scene.voiceOverUrdu.isNotBlank()) {
                        Text(
                            text = "اردو: ${scene.voiceOverUrdu}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Dialogues
            if (scene.dialogues.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Dialogue (${scene.dialogues.size}):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted
                    )
                    scene.dialogues.forEach { d ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavySurface.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "${d.characterName} (${d.emotion}):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanBlue
                            )
                            if (d.englishText.isNotBlank()) {
                                Text(
                                    text = "\"${d.englishText}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                            if (d.romanUrduText.isNotBlank()) {
                                Text(
                                    text = "Roman: \"${d.romanUrduText}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (d.urduText.isNotBlank()) {
                                Text(
                                    text = "اردو: \"${d.urduText}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CharactersListTab(
    characters: List<CharacterProfile>,
    onEditCharacter: (CharacterProfile) -> Unit,
    onDeleteCharacter: (CharacterProfile) -> Unit,
    onAddCharacter: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Character Consistency Explanation Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricPurple)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Character Consistency Lock",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Character appearance tags are automatically embedded across every scene prompt so AI video models maintain identical faces, hair and clothing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        items(characters, key = { it.id }) { char ->
            CharacterCardItem(
                character = char,
                onEdit = { onEditCharacter(char) },
                onDelete = { onDeleteCharacter(char) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun CharacterCardItem(
    character: CharacterProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "character_card_${character.id}"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CharacterAvatar(character = character, size = 40)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = character.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanBlue
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Character",
                            tint = CyanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Character",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Visual Appearance Prompt Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurface)
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "VISUAL PROMPT TAG:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = character.appearancePromptTag,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            // Personality & Voice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Personality: ${character.personality}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "Voice: ${character.voiceTone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Catchphrases in English / Roman Urdu / Urdu
            if (character.catchphraseEnglish.isNotBlank() || character.catchphraseRomanUrdu.isNotBlank() || character.catchphraseUrdu.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurfaceVariant)
                        .padding(8.dp)
                ) {
                    if (character.catchphraseEnglish.isNotBlank()) {
                        Text(
                            text = "Catchphrase (EN): \"${character.catchphraseEnglish}\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                    if (character.catchphraseRomanUrdu.isNotBlank()) {
                        Text(
                            text = "Catchphrase (Roman): \"${character.catchphraseRomanUrdu}\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanBlue
                        )
                    }
                    if (character.catchphraseUrdu.isNotBlank()) {
                        Text(
                            text = "Catchphrase (اردو): \"${character.catchphraseUrdu}\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenplayTab(project: StudioProject) {
    val context = LocalContext.current
    val scriptText = remember(project) { ExportHelper.generateProductionScriptTxt(project) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Screenplay & Dialogue View",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Button(
                onClick = { ExportHelper.copyToClipboard(context, scriptText, "Screenplay Script") },
                colors = ButtonDefaults.buttonColors(containerColor = CyanBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Full Script")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(NavySurface)
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = scriptText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        ),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun EditSceneDialog(
    scene: ScenePrompt,
    characters: List<CharacterProfile>,
    onDismiss: () -> Unit,
    onSave: (ScenePrompt) -> Unit
) {
    var title by remember { mutableStateOf(scene.title) }
    var duration by remember { mutableIntStateOf(scene.durationSeconds.coerceIn(6, 10)) }
    var setting by remember { mutableStateOf(scene.settingDescription) }
    var visualPrompt by remember { mutableStateOf(scene.visualPrompt) }
    var camera by remember { mutableStateOf(scene.cameraMovement) }
    var voEnglish by remember { mutableStateOf(scene.voiceOverEnglish) }
    var voRomanUrdu by remember { mutableStateOf(scene.voiceOverRomanUrdu) }
    var voUrdu by remember { mutableStateOf(scene.voiceOverUrdu) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Text(
                text = "Edit Scene ${scene.sceneNumber}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Scene Title", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_scene_title_input")
                )

                // Duration Slider (6 to 10 seconds)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duration:", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text("${duration}s (6s to 10s)", color = CyanBlue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    Slider(
                        value = duration.toFloat(),
                        onValueChange = { duration = it.toInt() },
                        valueRange = 6f..10f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = CyanBlue, activeTrackColor = CyanBlue),
                        modifier = Modifier.testTag("edit_duration_slider")
                    )
                }

                OutlinedTextField(
                    value = setting,
                    onValueChange = { setting = it },
                    label = { Text("Setting & Environment", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = visualPrompt,
                    onValueChange = { visualPrompt = it },
                    label = { Text("Visual Prompt", color = TextSecondary) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_visual_prompt_input")
                )

                OutlinedTextField(
                    value = voEnglish,
                    onValueChange = { voEnglish = it },
                    label = { Text("Voice-Over (English)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = voRomanUrdu,
                    onValueChange = { voRomanUrdu = it },
                    label = { Text("Voice-Over (Roman Urdu)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = voUrdu,
                    onValueChange = { voUrdu = it },
                    label = { Text("Voice-Over (اردو)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        scene.copy(
                            title = title,
                            durationSeconds = duration.coerceIn(6, 10),
                            settingDescription = setting,
                            visualPrompt = visualPrompt,
                            cameraMovement = camera,
                            voiceOverEnglish = voEnglish,
                            voiceOverRomanUrdu = voRomanUrdu,
                            voiceOverUrdu = voUrdu
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                modifier = Modifier.testTag("save_scene_button")
            ) {
                Text("Save Scene")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun EditCharacterDialog(
    character: CharacterProfile,
    onDismiss: () -> Unit,
    onSave: (CharacterProfile) -> Unit
) {
    var name by remember { mutableStateOf(character.name) }
    var role by remember { mutableStateOf(character.role) }
    var appearance by remember { mutableStateOf(character.appearancePromptTag) }
    var personality by remember { mutableStateOf(character.personality) }
    var voice by remember { mutableStateOf(character.voiceTone) }
    var catchEng by remember { mutableStateOf(character.catchphraseEnglish) }
    var catchRoman by remember { mutableStateOf(character.catchphraseRomanUrdu) }
    var catchUrdu by remember { mutableStateOf(character.catchphraseUrdu) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Text(
                text = if (character.name.isBlank()) "New Character" else "Edit ${character.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Character Name", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("character_name_input")
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role (Protagonist, Sidekick, Villain...)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = appearance,
                    onValueChange = { appearance = it },
                    label = { Text("Appearance Prompt Tag (Hair, clothes, face)", color = TextSecondary) },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("character_appearance_input")
                )

                OutlinedTextField(
                    value = personality,
                    onValueChange = { personality = it },
                    label = { Text("Personality", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = voice,
                    onValueChange = { voice = it },
                    label = { Text("Voice Tone", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = catchRoman,
                    onValueChange = { catchRoman = it },
                    label = { Text("Roman Urdu Catchphrase", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = catchUrdu,
                    onValueChange = { catchUrdu = it },
                    label = { Text("Urdu Script Catchphrase (اردو)", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            character.copy(
                                name = name,
                                role = role.ifBlank { "Protagonist" },
                                appearancePromptTag = appearance,
                                personality = personality,
                                voiceTone = voice,
                                catchphraseEnglish = catchEng,
                                catchphraseRomanUrdu = catchRoman,
                                catchphraseUrdu = catchUrdu
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanBlue),
                modifier = Modifier.testTag("save_character_button")
            ) {
                Text("Save Character")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
