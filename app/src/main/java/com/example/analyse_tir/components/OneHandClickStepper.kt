package com.example.analyse_tir.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout. defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Composant ergonomique utilisable d'une seule main sur le pas de tir :
 * boutons tactiles dont la taille et le padding s'adaptent au texte pour éviter toute coupure.
 */
@Composable
fun OneHandClickStepper(
    titre: String,
    sousTitre: String,
    valeurFormatee: String,
    labelMoinsFort: String,
    labelMoinsFin: String,
    labelPlusFin: String,
    labelPlusFort: String,
    onMoinsFort: () -> Unit,
    onMoinsFin: () -> Unit,
    onPlusFin: () -> Unit,
    onPlusFort: () -> Unit,
    modifier: Modifier = Modifier
) {
    val compactButtonPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        text = titre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sousTitre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = valeurFormatee,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onMoinsFort,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minWidth = 1.dp, minHeight = 42.dp),
                    contentPadding = compactButtonPadding,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = labelMoinsFort,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center
                    )
                }
                FilledTonalButton(
                    onClick = onMoinsFin,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minWidth = 1.dp, minHeight = 42.dp),
                    contentPadding = compactButtonPadding,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = labelMoinsFin,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center
                    )
                }
                FilledTonalButton(
                    onClick = onPlusFin,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minWidth = 1.dp, minHeight = 42.dp),
                    contentPadding = compactButtonPadding,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = labelPlusFin,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center
                    )
                }
                FilledTonalButton(
                    onClick = onPlusFort,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minWidth = 1.dp, minHeight = 42.dp),
                    contentPadding = compactButtonPadding,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = labelPlusFort,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
