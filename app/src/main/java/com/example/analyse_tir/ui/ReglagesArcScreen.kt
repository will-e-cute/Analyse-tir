package com.example.analyse_tir.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.analyse_tir.components.OneHandClickStepper
import com.example.analyse_tir.store.ArcheryUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReglagesArcScreen(
    uiState: ArcheryUiState,
    onAjusterDureteBerger: (Int) -> Unit,
    onAjusterSortieBerger: (Double) -> Unit,
    onAjusterDetalonnage: (Double) -> Unit,
    onAjusterBand: (Double) -> Unit,
    onChangerDistance: (Int) -> Unit,
    onEnregistrerReglageSeul: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = uiState.strings
    val cSing = strings.clickUnitSingular
    val cPlur = strings.clickUnitPlural

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = strings.bowSettingsTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = strings.bowSettingsSubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Sélection rapide de la distance de tir (taille adaptée au texte)
        Text(
            text = strings.currentDistanceLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(15, 18, 30, 50, 70).forEach { dist ->
                FilterChip(
                    selected = uiState.distanceTirMetres == dist,
                    onClick = { onChangerDistance(dist) },
                    label = { Text("${dist}m", fontWeight = FontWeight.Bold, maxLines = 1) }
                )
            }
        }

        // 1. Dureté Berger Button (en clics) + Slider précis
        OneHandClickStepper(
            titre = strings.bergerSpringTitle,
            sousTitre = strings.bergerSpringSubtitle,
            valeurFormatee = "${uiState.dureteBergerClics} $cPlur",
            labelMoinsFort = "-5 $cPlur",
            labelMoinsFin = "-1 $cSing",
            labelPlusFin = "+1 $cSing",
            labelPlusFort = "+5 $cPlur",
            onMoinsFort = { onAjusterDureteBerger(-5) },
            onMoinsFin = { onAjusterDureteBerger(-1) },
            onPlusFin = { onAjusterDureteBerger(1) },
            onPlusFort = { onAjusterDureteBerger(5) }
        )

        Slider(
            value = uiState.dureteBergerClics.toFloat(),
            onValueChange = { newVal ->
                val delta = newVal.roundToInt() - uiState.dureteBergerClics
                if (delta != 0) onAjusterDureteBerger(delta)
            },
            valueRange = 0f..40f,
            steps = 39,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // 2. Détalonnage (Hauteur du point d'encochage en mm)
        OneHandClickStepper(
            titre = strings.nockingPointTitle,
            sousTitre = strings.nockingPointSubtitle,
            valeurFormatee = "${uiState.detalonnageMm} mm",
            labelMoinsFort = "-1.5 mm",
            labelMoinsFin = "-0.5 mm",
            labelPlusFin = "+0.5 mm",
            labelPlusFort = "+1.5 mm",
            onMoinsFort = { onAjusterDetalonnage(-1.5) },
            onMoinsFin = { onAjusterDetalonnage(-0.5) },
            onPlusFin = { onAjusterDetalonnage(0.5) },
            onPlusFort = { onAjusterDetalonnage(1.5) }
        )

        // 3. Sortie Berger Button (Centershot en mm)
        OneHandClickStepper(
            titre = strings.centershotTitle,
            sousTitre = strings.centershotSubtitle,
            valeurFormatee = "${uiState.sortieBergerMm} mm",
            labelMoinsFort = "-0.5 mm",
            labelMoinsFin = "-0.1 mm",
            labelPlusFin = "+0.1 mm",
            labelPlusFort = "+0.5 mm",
            onMoinsFort = { onAjusterSortieBerger(-0.5) },
            onMoinsFin = { onAjusterSortieBerger(-0.1) },
            onPlusFin = { onAjusterSortieBerger(0.1) },
            onPlusFort = { onAjusterSortieBerger(0.5) }
        )

        // 4. Band de l'arc (Brace Height en mm)
        OneHandClickStepper(
            titre = strings.braceHeightTitle,
            sousTitre = strings.braceHeightSubtitle,
            valeurFormatee = "${uiState.bandMm} mm",
            labelMoinsFort = "-2.0 mm",
            labelMoinsFin = "-0.5 mm",
            labelPlusFin = "+0.5 mm",
            labelPlusFort = "+2.0 mm",
            onMoinsFort = { onAjusterBand(-2.0) },
            onMoinsFin = { onAjusterBand(-0.5) },
            onPlusFin = { onAjusterBand(0.5) },
            onPlusFort = { onAjusterBand(2.0) }
        )

        Button(
            onClick = onEnregistrerReglageSeul,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = strings.btnSaveTuningState,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
