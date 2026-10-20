package com.example.analyse_tir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.analyse_tir.store.ArcheryViewModel
import com.example.analyse_tir.ui.HistoriqueScreen
import com.example.analyse_tir.ui.MaterielScreen
import com.example.analyse_tir.ui.ReglagesArcScreen
import com.example.analyse_tir.ui.SessionTirScreen
import com.example.analyse_tir.utils.AppLanguage
import com.example.analyse_tir.utils.AppStrings

enum class OngletNavigation(val icon: ImageVector) {
    CIBLE(Icons.Default.GpsFixed),
    REGLAGES(Icons.Default.Tune),
    MATERIEL(Icons.Default.Build),
    HISTORIQUE(Icons.Default.History);

    fun titre(strings: AppStrings): String = when (this) {
        CIBLE -> strings.tabShootDiag
        REGLAGES -> strings.tabClicksBow
        MATERIEL -> strings.tabEquipment
        HISTORIQUE -> strings.tabHistory
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnalyseTirTheme {
                AnalyseTirApp()
            }
        }
    }
}

@Composable
fun AnalyseTirTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF4FC3F7),
            secondary = Color(0xFFFFB74D),
            tertiary = Color(0xFFCE93D8)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF005B9F),
            secondary = Color(0xFF00796B),
            tertiary = Color(0xFF7B1FA2)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyseTirApp(
    archeryViewModel: ArcheryViewModel = viewModel()
) {
    val uiState by archeryViewModel.uiState.collectAsState()
    val strings = uiState.strings
    var ongletActif by remember { mutableStateOf(OngletNavigation.CIBLE) }
    var menuLangueOuvert by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.dernierMessageConfirmation) {
        val msg = uiState.dernierMessageConfirmation
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            archeryViewModel.effacerMessageConfirmation()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.appTitle,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        FilledTonalButton(
                            onClick = { menuLangueOuvert = true },
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 36.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = strings.languageDialogTitle,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "${uiState.langue.flag} ${uiState.langue.code}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        DropdownMenu(
                            expanded = menuLangueOuvert,
                            onDismissRequest = { menuLangueOuvert = false }
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                val isSelected = lang == uiState.langue
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(lang.flag)
                                            Text(
                                                text = "${lang.nativeName} (${lang.code})",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        archeryViewModel.changerLangue(lang)
                                        menuLangueOuvert = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                OngletNavigation.entries.forEach { onglet ->
                    val label = onglet.titre(strings)
                    NavigationBarItem(
                        selected = ongletActif == onglet,
                        onClick = { ongletActif = onglet },
                        icon = { Icon(onglet.icon, contentDescription = label) },
                        label = {
                            Text(
                                text = label,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (ongletActif) {
            OngletNavigation.CIBLE -> SessionTirScreen(
                uiState = uiState,
                onTargetTap = archeryViewModel::ajouterImpactCible,
                onChangerModeSaisie = archeryViewModel::changerModeSaisieImpact,
                onChangerTypeTest = archeryViewModel::changerTypeTest,
                onUndoImpact = archeryViewModel::annulerDernierImpact,
                onClearImpacts = archeryViewModel::viderImpactsCible,
                onToggleSymptome = archeryViewModel::toggleSymptome,
                onBasculerLateralite = archeryViewModel::basculerLateraliteProfilActif,
                onAppliquerReglageSuggere = archeryViewModel::appliquerReglageSuggereParDiagnostic,
                onEnregistrerVol = archeryViewModel::enregistrerVolEtReglageDansSqlite,
                modifier = Modifier.padding(innerPadding)
            )

            OngletNavigation.REGLAGES -> ReglagesArcScreen(
                uiState = uiState,
                onAjusterDureteBerger = archeryViewModel::ajusterDureteBerger,
                onAjusterSortieBerger = archeryViewModel::ajusterSortieBerger,
                onAjusterDetalonnage = archeryViewModel::ajusterDetalonnage,
                onAjusterBand = archeryViewModel::ajusterBand,
                onChangerDistance = archeryViewModel::changerDistanceTir,
                onEnregistrerReglageSeul = archeryViewModel::enregistrerVolEtReglageDansSqlite,
                modifier = Modifier.padding(innerPadding)
            )

            OngletNavigation.MATERIEL -> MaterielScreen(
                uiState = uiState,
                onSelectProfil = archeryViewModel::selectionnerProfilActif,
                onSelectSetFleches = archeryViewModel::selectionnerSetFlechesActif,
                onCreerProfil = archeryViewModel::creerProfilMateriel,
                onCreerSetFleches = archeryViewModel::creerSetFleches,
                onSupprimerProfil = archeryViewModel::supprimerProfilMateriel,
                onSupprimerSetFleches = archeryViewModel::supprimerSetFleches,
                modifier = Modifier.padding(innerPadding)
            )

            OngletNavigation.HISTORIQUE -> HistoriqueScreen(
                uiState = uiState,
                onDeleteTestVol = archeryViewModel::supprimerTestVol,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
