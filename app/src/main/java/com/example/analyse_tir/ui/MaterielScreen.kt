package com.example.analyse_tir.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.analyse_tir.store.ArcheryUiState
import com.example.analyse_tir.types.LateraliteArcher
import com.example.analyse_tir.utils.AppLanguage
import com.example.analyse_tir.utils.AppStrings
import com.example.analyse_tir.utils.AppTranslations

@Composable
fun MaterielScreen(
    uiState: ArcheryUiState,
    onSelectProfil: (Long) -> Unit,
    onSelectSetFleches: (Long) -> Unit,
    onCreerProfil: (String, String, Double, Double, Int, LateraliteArcher) -> Unit,
    onCreerSetFleches: (String, String, Int, Double, Int, Int, Int) -> Unit,
    onSupprimerProfil: (Long) -> Unit,
    onSupprimerSetFleches: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddProfilDialog by remember { mutableStateOf(false) }
    var showAddFlechesDialog by remember { mutableStateOf(false) }
    val strings = uiState.strings

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1 : PROFIL_MATERIEL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.bowProfilesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = strings.bowProfilesSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalIconButton(
                onClick = { showAddProfilDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.dialogNewBowTitle,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (uiState.profils.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = strings.emptyBowsMessage,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.profils.forEach { profil ->
            val isSelected = profil.id == uiState.profilActif?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectProfil(profil.id) },
                shape = RoundedCornerShape(14.dp),
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profil.nomProfil,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profil.modeleArc,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${profil.puissanceLivres} lbs • ${profil.allongePouces}\" • ${profil.tailleArcPouces}\" • ${AppTranslations.lateraliteLabel(profil.lateralite, uiState.langue)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onSupprimerProfil(profil.id) }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Section 2 : SET_FLECHES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.arrowSetsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = strings.arrowSetsSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalIconButton(
                onClick = { showAddFlechesDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.dialogNewArrowsTitle,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (uiState.setsFleches.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = strings.emptyArrowsMessage,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.setsFleches.forEach { lot ->
            val isSelected = lot.id == uiState.setFlechesActif?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSetFleches(lot.id) },
                shape = RoundedCornerShape(14.dp),
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lot.nomLot,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lot.modeleTube} • Spine ${lot.spine}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${lot.longueurPouces}\" • ${strings.pointShort} ${lot.poidsPointeGrains} gr • ${strings.totalShort} ${lot.poidsTotalGrains} gr (${lot.nombrePlumes} ${strings.fletchingsUnit})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onSupprimerSetFleches(lot.id) }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showAddProfilDialog) {
        AddProfilDialog(
            strings = strings,
            langue = uiState.langue,
            onDismiss = { showAddProfilDialog = false },
            onConfirm = { nom, modele, puiss, allonge, taille, lat ->
                onCreerProfil(nom, modele, puiss, allonge, taille, lat)
                showAddProfilDialog = false
            }
        )
    }

    if (showAddFlechesDialog) {
        AddFlechesDialog(
            strings = strings,
            onDismiss = { showAddFlechesDialog = false },
            onConfirm = { nom, tube, spine, long, pointe, total, plumes ->
                onCreerSetFleches(nom, tube, spine, long, pointe, total, plumes)
                showAddFlechesDialog = false
            }
        )
    }
}

@Composable
private fun AddProfilDialog(
    strings: AppStrings,
    langue: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, Int, LateraliteArcher) -> Unit
) {
    var nom by remember { mutableStateOf("Arc 18m") }
    var modele by remember { mutableStateOf("Wiawis ATF-X") }
    var puissance by remember { mutableStateOf("38.0") }
    var allonge by remember { mutableStateOf("29.0") }
    var taille by remember { mutableStateOf("68") }
    var lateralite by remember { mutableStateOf(LateraliteArcher.DROITIER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.dialogNewBowTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text(strings.fieldProfileName) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = modele,
                    onValueChange = { modele = it },
                    label = { Text(strings.fieldBowModel) },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = puissance,
                        onValueChange = { puissance = it },
                        label = { Text(strings.fieldDrawWeight) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = allonge,
                        onValueChange = { allonge = it },
                        label = { Text(strings.fieldDrawLength) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = taille,
                    onValueChange = { taille = it },
                    label = { Text(strings.fieldBowLength) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LateraliteArcher.entries.forEach { lat ->
                        FilterChip(
                            selected = lateralite == lat,
                            onClick = { lateralite = lat },
                            label = { Text(AppTranslations.lateraliteLabel(lat, langue)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        nom.ifBlank { "Arc" },
                        modele.ifBlank { "Recurve" },
                        puissance.toDoubleOrNull() ?: 38.0,
                        allonge.toDoubleOrNull() ?: 28.5,
                        taille.toIntOrNull() ?: 68,
                        lateralite
                    )
                }
            ) {
                Text(strings.btnSave)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.btnCancel) }
        }
    )
}

@Composable
private fun AddFlechesDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Double, Int, Int, Int) -> Unit
) {
    var nomLot by remember { mutableStateOf("Lot #2") }
    var modeleTube by remember { mutableStateOf("Victory VAP V1") }
    var spine by remember { mutableStateOf("600") }
    var longueur by remember { mutableStateOf("29.0") }
    var pointe by remember { mutableStateOf("120") }
    var poidsTotal by remember { mutableStateOf("360") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.dialogNewArrowsTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nomLot,
                    onValueChange = { nomLot = it },
                    label = { Text(strings.fieldSetName) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = modeleTube,
                    onValueChange = { modeleTube = it },
                    label = { Text(strings.fieldShaftModel) },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = spine,
                        onValueChange = { spine = it },
                        label = { Text(strings.fieldSpine) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = longueur,
                        onValueChange = { longueur = it },
                        label = { Text(strings.fieldShaftLength) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pointe,
                        onValueChange = { pointe = it },
                        label = { Text(strings.fieldPointWeight) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = poidsTotal,
                        onValueChange = { poidsTotal = it },
                        label = { Text(strings.fieldTotalWeight) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        nomLot.ifBlank { "Arrows" },
                        modeleTube.ifBlank { "Carbon" },
                        spine.toIntOrNull() ?: 600,
                        longueur.toDoubleOrNull() ?: 29.0,
                        pointe.toIntOrNull() ?: 110,
                        poidsTotal.toIntOrNull() ?: 360,
                        3
                    )
                }
            ) {
                Text(strings.btnSave)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.btnCancel) }
        }
    )
}
