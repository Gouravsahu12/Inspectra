package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.EvidenceViewerScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NewInspectionScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ViolationDetailSheet
import com.example.ui.theme.InspectraTheme
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.viewmodel.InspectraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InspectraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InspectraTheme {
                InspectraApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun InspectraApp(viewModel: InspectraViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val navIndex by viewModel.currentNavIndex.collectAsState()
    val isWizardOpen by viewModel.isInspectionWizardOpen.collectAsState()
    val showEvidenceViewer by viewModel.showEvidenceViewer.collectAsState()
    val showReportScreen by viewModel.showReportScreen.collectAsState()
    val selectedViolation by viewModel.selectedViolation.collectAsState()

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
    } else if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else if (showReportScreen) {
        ReportScreen(
            viewModel = viewModel,
            onBack = { viewModel.toggleReportScreen(false) }
        )
    } else if (showEvidenceViewer) {
        EvidenceViewerScreen(
            viewModel = viewModel,
            onBack = { viewModel.toggleEvidenceViewer(false) }
        )
    } else if (isWizardOpen) {
        NewInspectionScreen(
            viewModel = viewModel,
            onClose = { viewModel.closeWizard() },
            onViewEvidence = { viewModel.toggleEvidenceViewer(true) },
            onViewReport = { viewModel.toggleReportScreen(true) },
            onViolationClick = { violation -> viewModel.selectViolationForDetail(violation) }
        )

        selectedViolation?.let { violation ->
            ViolationDetailSheet(
                violation = violation,
                onDismiss = { viewModel.selectViolationForDetail(null) },
                onViewEvidence = {
                    viewModel.selectViolationForDetail(null)
                    viewModel.toggleEvidenceViewer(true)
                }
            )
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        Triple(0, "Home", Icons.Default.Home),
                        Triple(1, "History", Icons.Default.History),
                        Triple(2, "Analytics", Icons.Default.Analytics),
                        Triple(3, "Profile", Icons.Default.Person)
                    )

                    navItems.forEach { (index, label, icon) ->
                        val isSelected = navIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setNavIndex(index) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Navy900,
                                selectedTextColor = Navy900,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate600,
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (navIndex) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onStartNewInspection = { viewModel.startNewInspection() },
                        onSelectInspection = { record ->
                            viewModel.startNewInspection()
                            // Set sample package if matches
                            if (record.productName.contains("Oil", ignoreCase = true)) {
                                viewModel.selectSamplePackage("oil")
                            } else if (record.productName.contains("Cookie", ignoreCase = true) || record.productName.contains("Biscuit", ignoreCase = true)) {
                                viewModel.selectSamplePackage("biscuits")
                            } else {
                                viewModel.selectSamplePackage("detergent")
                            }
                            viewModel.runAiAnalysis()
                        },
                        onNavigateToTab = { index -> viewModel.setNavIndex(index) }
                    )
                    1 -> HistoryScreen(
                        viewModel = viewModel,
                        onSelectInspection = { record ->
                            viewModel.startNewInspection()
                            if (record.productName.contains("Oil", ignoreCase = true)) {
                                viewModel.selectSamplePackage("oil")
                            } else if (record.productName.contains("Cookie", ignoreCase = true) || record.productName.contains("Biscuit", ignoreCase = true)) {
                                viewModel.selectSamplePackage("biscuits")
                            } else {
                                viewModel.selectSamplePackage("detergent")
                            }
                            viewModel.runAiAnalysis()
                        }
                    )
                    2 -> AnalyticsScreen(viewModel = viewModel)
                    3 -> ProfileScreen(
                        viewModel = viewModel,
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        }
    }
}
