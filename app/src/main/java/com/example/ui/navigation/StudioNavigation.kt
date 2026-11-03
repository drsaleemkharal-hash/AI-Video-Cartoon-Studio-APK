package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.StudioProject
import com.example.ui.screens.create.CreateStoryScreen
import com.example.ui.screens.create.CreateStoryViewModel
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.editor.ProjectEditorScreen
import com.example.ui.screens.editor.ProjectEditorViewModel
import com.example.ui.screens.export.ExportBottomSheet
import com.example.ui.screens.settings.SettingsScreen

sealed interface StudioScreen {
    data object Dashboard : StudioScreen
    data object CreateStory : StudioScreen
    data class ProjectEditor(val projectId: String) : StudioScreen
    data object Settings : StudioScreen
}

@Composable
fun StudioNavigation() {
    var currentScreen by remember { mutableStateOf<StudioScreen>(StudioScreen.Dashboard) }
    var activeExportProject by remember { mutableStateOf<StudioProject?>(null) }

    when (val screen = currentScreen) {
        is StudioScreen.Dashboard -> {
            val dashboardViewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToCreate = { currentScreen = StudioScreen.CreateStory },
                onNavigateToEditor = { id -> currentScreen = StudioScreen.ProjectEditor(id) },
                onNavigateToSettings = { currentScreen = StudioScreen.Settings },
                onExportProject = { project -> activeExportProject = project }
            )
        }

        is StudioScreen.CreateStory -> {
            val createViewModel: CreateStoryViewModel = viewModel()
            CreateStoryScreen(
                viewModel = createViewModel,
                onNavigateBack = { currentScreen = StudioScreen.Dashboard },
                onStoryCreated = { projectId ->
                    currentScreen = StudioScreen.ProjectEditor(projectId)
                }
            )
        }

        is StudioScreen.ProjectEditor -> {
            val editorViewModel: ProjectEditorViewModel = viewModel()
            ProjectEditorScreen(
                projectId = screen.projectId,
                viewModel = editorViewModel,
                onNavigateBack = { currentScreen = StudioScreen.Dashboard },
                onExportProject = { project -> activeExportProject = project }
            )
        }

        is StudioScreen.Settings -> {
            SettingsScreen(
                onNavigateBack = { currentScreen = StudioScreen.Dashboard }
            )
        }
    }

    // Modal Export Bottom Sheet when active
    activeExportProject?.let { project ->
        ExportBottomSheet(
            project = project,
            onDismiss = { activeExportProject = null }
        )
    }
}
