package com.maximaaax.androidutils.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximaaax.androidutils.presentation.components.ArrowRightIcon
import com.maximaaax.androidutils.presentation.components.GlassButton
import com.maximaaax.androidutils.presentation.components.GlassButtonVariant
import com.maximaaax.androidutils.presentation.components.NoticeBanner
import com.maximaaax.androidutils.presentation.components.PlayIcon
import com.maximaaax.androidutils.presentation.components.SparkleIcon
import com.maximaaax.androidutils.presentation.components.StatusPill
import com.maximaaax.androidutils.presentation.components.olaniTextFieldColors
import com.maximaaax.androidutils.presentation.model.AndroidUtilsUiState
import com.maximaaax.androidutils.presentation.model.AppSection
import com.maximaaax.androidutils.presentation.navigation.AppShell
import com.maximaaax.androidutils.presentation.navigation.GlassSection
import com.maximaaax.androidutils.presentation.navigation.PageHeader
import com.maximaaax.androidutils.presentation.navigation.appDisplayName
import com.maximaaax.androidutils.presentation.theme.AndroidUtilsPalette
import com.maximaaax.androidutils.presentation.theme.AndroidUtilsTheme
import com.maximaaax.androidutils.presentation.theme.GlassSurface
import com.maximaaax.androidutils.presentation.theme.InteractiveGlassSurface
import com.maximaaax.androidutils.presentation.theme.olaniTokens

@Composable
fun AndroidUtilsApp(
    state: AndroidUtilsUiState,
    onSetSection: (AppSection) -> Unit,
    onSetDeviceMenuExpanded: (Boolean) -> Unit,
    onSelectDevice: (String) -> Unit,
    onRefreshDevices: () -> Unit,
    onRefreshPackages: () -> Unit,
    onPackageFilter: (String) -> Unit,
    onExtract: (String) -> Unit,
    onStartScrcpy: () -> Unit,
    snackbarHost: SnackbarHostState,
) {
    AndroidUtilsTheme {
        AppShell(
            currentSection = state.currentSection,
            onSelect = onSetSection,
            snackbarHostState = snackbarHost,
        ) {
            AnimatedContent(
                targetState = state.currentSection,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { section ->
                when (section) {
                    AppSection.Home -> HomeContent(onGoWorkshop = { onSetSection(AppSection.Workshop) })
                    AppSection.Workshop -> WorkshopContent(
                        state = state,
                        onSetDeviceMenuExpanded = onSetDeviceMenuExpanded,
                        onSelectDevice = onSelectDevice,
                        onRefreshDevices = onRefreshDevices,
                        onRefreshPackages = onRefreshPackages,
                        onPackageFilter = onPackageFilter,
                        onExtract = onExtract,
                        onStartScrcpy = onStartScrcpy,
                    )
                    AppSection.Credits -> CreditsContent(adbAvailable = state.adbToolsAvailable)
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    onGoWorkshop: () -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 8.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        GlassSurface(
            borderCorner = 20.dp,
            padding = 32.dp,
        ) {
            val pill = Brush.linearGradient(
                listOf(
                    AndroidUtilsPalette.GlowViolet.copy(alpha = 0.25f),
                    AndroidUtilsPalette.GlowCyan.copy(alpha = 0.2f),
                ),
            )
            val tokens = olaniTokens()
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Bienvenue",
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(brush = pill, shape = RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = "Bienvenue dans\n$appDisplayName",
                    style = MaterialTheme.typography.displayMedium,
                    color = AndroidUtilsPalette.OnBackground,
                )
                Text(
                    text = "Outils ADB & scrcpy (macOS) : appareils, miroir d’écran, extraction d’APK. Sans prérequis, pour les équipes peu techniques.",
                    color = tokens.textMuted,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        FeatureCard(
            title = "Atelier ADB & APK",
            pill = "ÉCRAN & FICHIERS",
            onClick = onGoWorkshop,
        )
    }
}

@Composable
private fun FeatureCard(
    title: String,
    pill: String,
    onClick: () -> Unit,
) {
    val tokens = olaniTokens()
    InteractiveGlassSurface(
        onClick = onClick,
        borderCorner = 20.dp,
        padding = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val iconBox = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(AndroidUtilsPalette.GlowViolet, AndroidUtilsPalette.GlowIndigo, AndroidUtilsPalette.GlowPink),
                    ),
                )
            Box(
                iconBox,
                contentAlignment = Alignment.Center,
            ) {
                PlayIcon(
                    fillBrush = Brush.linearGradient(
                        listOf(Color.White, Color(0xFFE0E0FF)),
                    ),
                    size = 20.dp,
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    text = pill,
                    color = tokens.textDim,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                )
            }
            GlassButton(
                text = "Ouvrir l'atelier",
                onClick = onClick,
                variant = GlassButtonVariant.Primary,
                leading = { SparkleIcon(color = Color.White, size = 14.dp) },
                trailing = { ArrowRightIcon(color = Color.White, size = 14.dp) },
            )
        }
    }
}

@Composable
private fun WorkshopContent(
    state: AndroidUtilsUiState,
    onSetDeviceMenuExpanded: (Boolean) -> Unit,
    onSelectDevice: (String) -> Unit,
    onRefreshDevices: () -> Unit,
    onRefreshPackages: () -> Unit,
    onPackageFilter: (String) -> Unit,
    onExtract: (String) -> Unit,
    onStartScrcpy: () -> Unit,
) {
    var highlightPkg by remember { mutableStateOf<String?>(null) }
    val filtered = remember(
        state.packages,
        state.packageFilter,
    ) {
        if (state.packageFilter.isBlank()) {
            state.packages
        } else {
            state.packages.filter { it.contains(state.packageFilter.trim(), ignoreCase = true) }
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            PageHeader(
                eyebrow = "Atelier",
                title = "ADB, miroir & applications",
                description = "Sélectionnez un appareil, lancez scrcpy, listez et extrayez des APK sur votre ordinateur.",
            )
        }
        if (!state.adbToolsAvailable) {
            item {
                NoticeBanner(
                    message = "Fonctionnalité complète disponible sur l’appli de bureau (macOS). Ici, interface seulement.",
                    isError = true,
                )
            }
        }
        item {
            GlassSection(
                title = "Source",
                description = "Sélection de l'appareil ADB (USB + débogage).",
            ) {
                if (state.loadingDevices) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = AndroidUtilsPalette.GlowViolet,
                            trackColor = Color(0x14FFFFFF),
                        )
                    }
                }
                DeviceDropdown(
                    state = state,
                    onSetExpanded = onSetDeviceMenuExpanded,
                    onSelect = onSelectDevice,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassButton("Actualiser les appareils", onClick = onRefreshDevices, enabled = !state.loadingDevices)
                    if (state.loadingDevices) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = AndroidUtilsPalette.Accent,
                        )
                    }
                }
            }
        }
        item {
            GlassSection(
                title = "Miroir",
                description = "Lancez scrcpy sur l'appareil actif (adb « device »).",
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val serial = state.selectedSerial
                    GlassButton(
                        text = "Lancer scrcpy",
                        onClick = onStartScrcpy,
                        variant = GlassButtonVariant.Primary,
                        leading = { SparkleIcon(color = Color.White, size = 16.dp) },
                    )
                    if (serial != null) {
                        val d = state.devices.find { it.serial == serial }
                        if (d != null) {
                            if (d.isUsable) {
                                StatusPill("Prêt", color = olaniTokens().success)
                            } else {
                                StatusPill("État : " + d.displayLabel, color = AndroidUtilsPalette.Warning)
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassSection(
                title = "Options",
                description = "Filtre de la liste des packages. Extraction = dossier côté desktop (dialogue de fichiers).",
            ) {
                OutlinedTextField(
                    value = state.packageFilter,
                    onValueChange = onPackageFilter,
                    label = { Text("Filtrer le nom de package", color = olaniTokens().textDim) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = olaniTextFieldColors(),
                )
            }
        }
        item {
            GlassSection(
                title = "Applications installées",
                description = "Liste lue sur l'appareil. Extraire copie l'APK (libellé + version).",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassButton("Lister les applications", onClick = onRefreshPackages, enabled = !state.loadingPackages)
                    if (state.loadingPackages) {
                        Box(Modifier.width(80.dp), contentAlignment = Alignment.CenterStart) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = AndroidUtilsPalette.GlowViolet,
                                trackColor = Color(0x14FFFFFF),
                            )
                        }
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = AndroidUtilsPalette.Accent)
                    }
                }
                GlassSurface(
                    borderCorner = 16.dp,
                    padding = 0.dp,
                    highlight = true,
                    modifier = Modifier.padding(top = 8.dp).height(320.dp),
                ) {
                    if (state.loadingPackages && state.packages.isEmpty()) {
                        Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
                            Text("Chargement…", color = olaniTokens().textDim)
                        }
                    } else {
                        val scrollInner = rememberScrollState()
                        Column(Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollInner)) {
                            for (pkg in filtered) {
                                val isSel = highlightPkg == pkg
                                val interaction = remember(pkg) { MutableInteractionSource() }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = interaction,
                                            indication = null,
                                        ) { highlightPkg = if (isSel) null else pkg }
                                        .background(
                                            if (isSel) Color(0x14FFFFFF) else Color.Transparent,
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = pkg,
                                        modifier = Modifier.weight(1f),
                                        color = AndroidUtilsPalette.OnBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    GlassButton(
                                        text = if (state.pullingPackage == pkg) "Patientez…" else "Extraire",
                                        onClick = { onExtract(pkg) },
                                        enabled = state.pullingPackage == null,
                                        variant = GlassButtonVariant.Secondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceDropdown(
    state: AndroidUtilsUiState,
    onSetExpanded: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        val label: String = when {
            state.devices.isEmpty() -> "Aucun appareil"
            state.selectedSerial != null -> {
                val d = state.devices.find { it.serial == state.selectedSerial }
                d?.displayLabel ?: "Choisir un appareil"
            }
            else -> "Choisir un appareil"
        }
        val click = remember { MutableInteractionSource() }
        GlassSurface(
            borderCorner = 12.dp,
            padding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = click,
                    indication = null,
                ) { onSetExpanded(!state.deviceMenuExpanded) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (state.deviceMenuExpanded) "▲" else "▼",
                    color = olaniTokens().textMuted,
                )
            }
        }
        DropdownMenu(
            expanded = state.deviceMenuExpanded,
            onDismissRequest = { onSetExpanded(false) },
            containerColor = AndroidUtilsPalette.SurfaceElevated,
        ) {
            for (d in state.devices) {
                DropdownMenuItem(
                    text = { Text(d.displayLabel) },
                    onClick = {
                        onSelect(d.serial)
                        onSetExpanded(false)
                    },
                )
            }
        }
    }
}

@Composable
private fun CreditsContent(adbAvailable: Boolean) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        PageHeader(
            eyebrow = "Mentions",
            title = "Crédits & outils",
            description = "Cette appli intègre adb (Google platform-tools) et scrcpy (Genymobile), sous licences open source. Vérifiez les conditions de votre distribution pour les binaires embarqués.",
        )
        GlassSection(
            title = "Outils",
            description = "Chemin des ressources : propriété compose.application.resources.dir au runtime.",
        ) {
            Text(
                "• Android Debug Bridge (adb) — platform-tools",
                color = olaniTokens().textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "• scrcpy — miroir et contrôle",
                color = olaniTokens().textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (!adbAvailable) {
            NoticeBanner(
                message = "Cette cible n’inclut pas l’orchestration ADB complète. Utilisez l’app de bureau sur macOS pour l’extraction d’APK et scrcpy.",
                isError = true,
            )
        }
    }
}