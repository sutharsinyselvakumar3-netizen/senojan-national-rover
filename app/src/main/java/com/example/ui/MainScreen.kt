package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotMode
import com.example.ui.components.ModeSwitchDialog
import com.example.ui.components.TopStatusBar
import com.example.ui.screens.AiVisionScreen
import com.example.ui.screens.ArmScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RobotScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AiCompanionTheme
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.TextMuted
import com.example.viewmodel.RobotViewModel
import com.example.viewmodel.UiEvent

enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("HOME", Icons.Default.Home),
    ROBOT("ROBOT", Icons.Default.SmartToy),
    AI_VISION("AI VISION", Icons.Default.RemoveRedEye),
    ARM("ARM", Icons.Default.PrecisionManufacturing),
    SETTINGS("SETTINGS", Icons.Default.Settings)
}

@Composable
fun MainScreen(viewModel: RobotViewModel) {
    val robotMode by viewModel.robotMode.collectAsState()
    val esp32State by viewModel.esp32ConnectionState.collectAsState()
    val cameraState by viewModel.cameraConnectionState.collectAsState()
    val aiReady by viewModel.aiReady.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showModeDialog by remember { mutableStateOf(false) }
    var modeDialogError by remember { mutableStateOf<String?>(null) }
    var isModeSwitching by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // Back handling: if not on Home screen, back goes to Home
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    // Dynamic Mode Theming: Accents change to Dark Green in MANUAL and Dark Blue in AUTO
    // Royal White background stays unchanged throughout the app!
    AiCompanionTheme(robotMode = robotMode) {
        val modeColors = LocalRobotModeColors.current

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(RoyalWhite)
                .statusBarsPadding()
                .navigationBarsPadding(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopStatusBar(
                    robotMode = robotMode,
                    esp32State = esp32State,
                    cameraState = cameraState,
                    aiReady = aiReady,
                    onModeClick = {
                        modeDialogError = null
                        showModeDialog = true
                    },
                    onEmergencyStop = {
                        viewModel.triggerEmergencyStop()
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CardSurfaceWhite,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .border(1.dp, CardBorderLight)
                        .testTag("bottom_navigation_bar")
                ) {
                    AppTab.entries.forEachIndexed { index, tab ->
                        val isSelected = selectedTabIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PureWhite,
                                selectedTextColor = modeColors.primaryAccent,
                                indicatorColor = modeColors.primaryAccent,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(RoyalWhite)
            ) {
                when (selectedTabIndex) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToCameraSettings = { selectedTabIndex = 4 }
                    )
                    1 -> RobotScreen(
                        viewModel = viewModel,
                        onNavigateToCameraSettings = { selectedTabIndex = 4 }
                    )
                    2 -> AiVisionScreen(
                        viewModel = viewModel,
                        onNavigateToCameraSettings = { selectedTabIndex = 4 }
                    )
                    3 -> ArmScreen(
                        viewModel = viewModel
                    )
                    4 -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }

            // Global Mode Confirmation Dialog
            if (showModeDialog) {
                ModeSwitchDialog(
                    currentMode = robotMode,
                    isProcessing = isModeSwitching,
                    errorMessage = modeDialogError,
                    onDismiss = {
                        showModeDialog = false
                        modeDialogError = null
                    },
                    onConfirmSwitch = {
                        isModeSwitching = true
                        if (robotMode == RobotMode.MANUAL) {
                            viewModel.requestSwitchToAuto { success, message ->
                                isModeSwitching = false
                                if (success) {
                                    showModeDialog = false
                                    modeDialogError = null
                                } else {
                                    modeDialogError = message
                                }
                            }
                        } else {
                            viewModel.requestSwitchToManual { success, message ->
                                isModeSwitching = false
                                if (success) {
                                    showModeDialog = false
                                    modeDialogError = null
                                } else {
                                    modeDialogError = message
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
