package com.example.analyse_tir.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.analyse_tir.types.ImpactCible
import com.example.analyse_tir.types.TypeFlecheImpact
import com.example.analyse_tir.types.TypeTest
import com.example.analyse_tir.utils.AppStrings
import kotlin.math.min

/**
 * Cible interactive World Archery (10 zones) / Cadre Test Papier.
 * Conçue pour une saisie tactile rapide à une main sur le pas de tir.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveTargetCanvas(
    impacts: List<ImpactCible>,
    typeTest: TypeTest,
    strings: AppStrings,
    onTargetTap: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(20.dp)),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            color = Color(0xFF1A2129)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val maxRadius = min(centerX, centerY) * 0.90f
                            if (maxRadius > 0f) {
                                val normX = ((tapOffset.x - centerX) / maxRadius).coerceIn(-1f, 1f)
                                val normY = ((centerY - tapOffset.y) / maxRadius).coerceIn(-1f, 1f)
                                onTargetTap(normX, normY)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val center = Offset(centerX, centerY)
                    val maxRadius = min(centerX, centerY) * 0.90f

                    if (typeTest == TypeTest.FUT_NU) {
                        val rings = listOf(
                            1.00f to Color(0xFFF5F5F5),
                            0.90f to Color(0xFFECECEC),
                            0.80f to Color(0xFF262626),
                            0.70f to Color(0xFF1C1C1C),
                            0.60f to Color(0xFF0088CE),
                            0.50f to Color(0xFF0077B5),
                            0.40f to Color(0xFFE52521),
                            0.30f to Color(0xFFD11F1B),
                            0.20f to Color(0xFFFFD600),
                            0.10f to Color(0xFFFFC400)
                        )

                        for ((ratio, ringColor) in rings) {
                            val r = maxRadius * ratio
                            drawCircle(
                                color = ringColor,
                                radius = r,
                                center = center
                            )
                            drawCircle(
                                color = if (ratio in 0.65f..0.85f) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f),
                                radius = r,
                                center = center,
                                style = Stroke(width = 1.5f)
                            )
                        }
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.4f),
                            radius = maxRadius * 0.05f,
                            center = center,
                            style = Stroke(width = 1.5f)
                        )
                    } else {
                        drawRect(
                            color = Color(0xFFFDFBF7),
                            topLeft = Offset(centerX - maxRadius, centerY - maxRadius),
                            size = androidx.compose.ui.geometry.Size(maxRadius * 2f, maxRadius * 2f)
                        )
                        drawCircle(
                            color = Color(0xFF90A4AE).copy(alpha = 0.35f),
                            radius = maxRadius * 0.55f,
                            center = center,
                            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))
                        )
                        drawCircle(
                            color = Color(0xFFE53935),
                            radius = 8f,
                            center = center
                        )
                    }

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawLine(
                        color = Color.Black.copy(alpha = 0.3f),
                        start = Offset(centerX - maxRadius, centerY),
                        end = Offset(centerX + maxRadius, centerY),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.3f),
                        start = Offset(centerX, centerY - maxRadius),
                        end = Offset(centerX, centerY + maxRadius),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )

                    val empennees = impacts.filter { it.typeFleche == TypeFlecheImpact.EMPENNEE }
                    val futsNus = impacts.filter { it.typeFleche == TypeFlecheImpact.FUT_NU }

                    val barycentreEmp = if (empennees.isNotEmpty()) {
                        val avgX = empennees.map { it.x }.average().toFloat()
                        val avgY = empennees.map { it.y }.average().toFloat()
                        Offset(centerX + avgX * maxRadius, centerY - avgY * maxRadius)
                    } else {
                        center
                    }

                    if (futsNus.isNotEmpty()) {
                        val avgNuX = futsNus.map { it.x }.average().toFloat()
                        val avgNuY = futsNus.map { it.y }.average().toFloat()
                        val barycentreNu = Offset(centerX + avgNuX * maxRadius, centerY - avgNuY * maxRadius)

                        drawLine(
                            color = Color(0xFFFF6D00),
                            start = barycentreEmp,
                            end = barycentreNu,
                            strokeWidth = 5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                        )
                    }

                    impacts.forEach { impact ->
                        val px = centerX + impact.x * maxRadius
                        val py = centerY - impact.y * maxRadius
                        val pos = Offset(px, py)

                        if (impact.typeFleche == TypeFlecheImpact.EMPENNEE) {
                            drawCircle(color = Color.White, radius = 14f, center = pos)
                            drawCircle(color = Color(0xFF00C853), radius = 10.5f, center = pos)
                            drawCircle(color = Color.Black, radius = 3.5f, center = pos)
                        } else {
                            drawCircle(color = Color(0xFFD500F9).copy(alpha = 0.28f), radius = 22f, center = pos)
                            drawCircle(color = Color.White, radius = 15f, center = pos)
                            drawCircle(color = Color(0xFFAA00FF), radius = 11.5f, center = pos)
                            drawCircle(color = Color.White, radius = 4f, center = pos)
                        }
                    }
                }
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LegendBadge(
                color = Color(0xFF00C853),
                label = "${strings.legendFletched} (${impacts.count { it.typeFleche == TypeFlecheImpact.EMPENNEE }})"
            )
            LegendBadge(
                color = Color(0xFFAA00FF),
                label = "${strings.legendBareShaft} (${impacts.count { it.typeFleche == TypeFlecheImpact.FUT_NU }})"
            )
            LegendBadge(
                color = Color(0xFFFF6D00),
                label = strings.legendVector
            )
        }
    }
}

@Composable
private fun LegendBadge(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
