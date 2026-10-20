package com.example.analyse_tir.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.analyse_tir.components.InteractiveTargetCanvas
import com.example.analyse_tir.store.ArcheryUiState
import com.example.analyse_tir.types.LateraliteArcher
import com.example.analyse_tir.types.SeveriteDiagnostic
import com.example.analyse_tir.types.SymptomeVol
import com.example.analyse_tir.types.TypeFlecheImpact
import com.example.analyse_tir.types.TypeTest
import com.example.analyse_tir.utils.AppTranslations

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionTirScreen(
    uiState: ArcheryUiState,
    onTargetTap: (Float, Float) -> Unit,
    onChangerModeSaisie: (TypeFlecheImpact) -> Unit,
    onChangerTypeTest: (TypeTest) -> Unit,
    onUndoImpact: () -> Unit,
    onClearImpacts: () -> Unit,
    onToggleSymptome: (SymptomeVol) -> Unit,
    onBasculerLateralite: () -> Unit,
    onAppliquerReglageSuggere: () -> Unit,
    onEnregistrerVol: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diagnostic = uiState.diagnosticTempsReel
    val profil = uiState.profilActif
    val fleches = uiState.setFlechesActif
    val strings = uiState.strings
    val compactBtnPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bandeau résumé matériel actif + bascule rapide Droitier / Gaucher
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = profil?.nomProfil ?: strings.bowNotConfigured,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${profil?.puissanceLivres ?: 40.0} lbs • ${fleches?.modeleTube ?: "Tube"} (Spine ${fleches?.spine ?: 550}, ${fleches?.poidsPointeGrains ?: 110} gr)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    AssistChip(
                        onClick = onBasculerLateralite,
                        label = {
                            Text(
                                text = AppTranslations.lateraliteLabel(
                                    profil?.lateralite ?: LateraliteArcher.DROITIER,
                                    uiState.langue
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                Text(
                    text = "${strings.activeSetupPrefix} : ${strings.nockingPointShort} ${uiState.detalonnageMm} mm • Berger ${uiState.dureteBergerClics} ${strings.clicksShort} (${uiState.sortieBergerMm} mm) • ${strings.braceHeightShort} ${uiState.bandMm} mm • ${uiState.distanceTirMetres}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Sélecteur Type de Test (Fût Nu vs Test Papier) adapté au texte
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TypeTest.entries.forEach { test ->
                FilterChip(
                    selected = uiState.typeTestActif == test,
                    onClick = { onChangerTypeTest(test) },
                    label = {
                        Text(
                            text = AppTranslations.typeTestLabel(test, uiState.langue),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        // Boutons de sélection de flèche à saisir (Taille adaptée au texte)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isEmpennee = uiState.modeSaisieImpact == TypeFlecheImpact.EMPENNEE
            Button(
                onClick = { onChangerModeSaisie(TypeFlecheImpact.EMPENNEE) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 1.dp, minHeight = 44.dp),
                contentPadding = compactBtnPadding,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmpennee) Color(0xFF00A844) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isEmpennee) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = strings.btnFletchedImpact,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            val isFutNu = uiState.modeSaisieImpact == TypeFlecheImpact.FUT_NU
            Button(
                onClick = { onChangerModeSaisie(TypeFlecheImpact.FUT_NU) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 1.dp, minHeight = 44.dp),
                contentPadding = compactBtnPadding,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFutNu) Color(0xFF8E24AA) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isFutNu) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = strings.btnBareShaftImpact,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Cible Interactive
        InteractiveTargetCanvas(
            impacts = uiState.impactsCible,
            typeTest = uiState.typeTestActif,
            strings = strings,
            onTargetTap = onTargetTap
        )

        // Boutons rapides Annuler dernier impact / Réinitialiser cible (Taille adaptée au texte)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onUndoImpact,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 1.dp, minHeight = 42.dp),
                contentPadding = compactBtnPadding,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = strings.btnUndoImpact,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = onClearImpacts,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 1.dp, minHeight = 42.dp),
                contentPadding = compactBtnPadding,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = strings.btnClearTarget,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Carte de Diagnostic Algorithmique Instantané
        val bannerColor = when (diagnostic.severiteGlobale) {
            SeveriteDiagnostic.OPTIMAL -> Color(0xFF1B5E20)
            SeveriteDiagnostic.LEGER_AJUSTEMENT -> Color(0xFFE65100)
            SeveriteDiagnostic.CORRECTION_IMPORTANTE -> Color(0xFFB71C1C)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, bannerColor.copy(alpha = 0.7f)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (diagnostic.severiteGlobale == SeveriteDiagnostic.OPTIMAL)
                            Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = bannerColor
                    )
                    Text(
                        text = strings.diagCardTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = bannerColor
                    )
                }

                // Deux badges synthétiques : Vertical (Détalonnage) & Horizontal (Spine/Berger)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${strings.verticalAxisLabel} (ΔY = ${diagnostic.ecartVerticalCm} cm)",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = diagnostic.etatDetalonnage,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${strings.horizontalAxisLabel} (ΔX = ${diagnostic.ecartHorizontalCm} cm)",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = diagnostic.etatSpine,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = diagnostic.explicationPhysique,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = strings.actionPlanTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                diagnostic.actionsRecommandees.forEach { action ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "${action.priorite}. ${action.parametre}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = action.action,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = action.valeurSuggeree,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Symptômes complémentaires observés en vol
                Text(
                    text = strings.symptomsTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        SymptomeVol.VOL_PROPRE,
                        SymptomeVol.MARSouinage,
                        SymptomeVol.FISHTAILING,
                        SymptomeVol.CONTACT_FENETRE
                    ).forEach { symp ->
                        val selected = uiState.symptomesSelectionnes.contains(symp)
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleSymptome(symp) },
                            label = { Text(AppTranslations.symptomeLabel(symp, uiState.langue)) },
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }
                }

                // Boutons d'action directe (Appliquer les clics + Sauvegarder dans SQLite) adaptés au texte
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAppliquerReglageSuggere,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minWidth = 1.dp, minHeight = 44.dp),
                        contentPadding = compactBtnPadding,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = strings.btnApplyClicks,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onEnregistrerVol,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minWidth = 1.dp, minHeight = 44.dp),
                        contentPadding = compactBtnPadding,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = strings.btnSaveEnd,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
