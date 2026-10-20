package com.example.analyse_tir

import com.example.analyse_tir.types.ImpactCible
import com.example.analyse_tir.types.JournalReglage
import com.example.analyse_tir.types.LateraliteArcher
import com.example.analyse_tir.types.SeveriteDiagnostic
import com.example.analyse_tir.types.TypeFlecheImpact
import com.example.analyse_tir.utils.DiagnosticEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticEngineTest {

    private val reglageBase = JournalReglage(
        id = 1L,
        profilMaterielId = 1L,
        setFlechesId = 1L,
        bandMm = 224.0,
        detalonnageMm = 4.0,
        dureteBergerClics = 15,
        sortieBergerMm = 1.5
    )

    @Test
    fun `droitier - impact fut nu a GAUCHE detecte fleche trop RIGIDE et assouplit Berger`() {
        val result = DiagnosticEngine.analyserTir(
            impactHorizontal = -0.35,
            impactVertical = 0.0,
            lateralite = LateraliteArcher.DROITIER,
            reglageActuel = reglageBase
        )

        assertTrue(result.etatSpine.contains("RIGIDE"))
        assertTrue(result.actionsRecommandees.any { it.action.contains("Assouplir", ignoreCase = true) })
        assertTrue((result.reglageSuggere?.dureteBergerClics ?: 15) < 15)
    }

    @Test
    fun `droitier - impact fut nu a DROITE detecte fleche trop SOUPLE et durcit Berger`() {
        val result = DiagnosticEngine.analyserTir(
            impactHorizontal = 0.35,
            impactVertical = 0.0,
            lateralite = LateraliteArcher.DROITIER,
            reglageActuel = reglageBase
        )

        assertTrue(result.etatSpine.contains("SOUPLE"))
        assertTrue(result.actionsRecommandees.any { it.action.contains("Durcir", ignoreCase = true) })
        assertTrue((result.reglageSuggere?.dureteBergerClics ?: 15) > 15)
    }

    @Test
    fun `gaucher - inversion horizontale - impact fut nu a GAUCHE detecte fleche trop SOUPLE`() {
        val result = DiagnosticEngine.analyserTir(
            impactHorizontal = -0.35,
            impactVertical = 0.0,
            lateralite = LateraliteArcher.GAUCHER,
            reglageActuel = reglageBase
        )

        assertTrue(result.etatSpine.contains("SOUPLE"))
        assertTrue(result.actionsRecommandees.any { it.action.contains("Durcir", ignoreCase = true) })
    }

    @Test
    fun `gaucher - inversion horizontale - impact fut nu a DROITE detecte fleche trop RIGIDE`() {
        val result = DiagnosticEngine.analyserTir(
            impactHorizontal = 0.35,
            impactVertical = 0.0,
            lateralite = LateraliteArcher.GAUCHER,
            reglageActuel = reglageBase
        )

        assertTrue(result.etatSpine.contains("RIGIDE"))
        assertTrue(result.actionsRecommandees.any { it.action.contains("Assouplir", ignoreCase = true) })
    }

    @Test
    fun `impact fut nu en BAS detecte detalonnage trop HAUT et suggere de descendre le repere`() {
        val result = DiagnosticEngine.analyserTir(
            impactHorizontal = 0.0,
            impactVertical = -0.30,
            lateralite = LateraliteArcher.DROITIER,
            reglageActuel = reglageBase
        )

        assertTrue(result.etatDetalonnage.contains("HAUT"))
        assertTrue(result.actionsRecommandees.first().action.contains("Descendre", ignoreCase = true))
        assertTrue((result.reglageSuggere?.detalonnageMm ?: 4.0) < 4.0)
    }

    @Test
    fun `impact fut nu en HAUT detecte detalonnage trop BAS et suggere de monter le repere`() {
        val result = DiagnosticEngine.analyserTir(
            impactHorizontal = 0.0,
            impactVertical = 0.30,
            lateralite = LateraliteArcher.DROITIER,
            reglageActuel = reglageBase
        )

        assertTrue(result.etatDetalonnage.contains("BAS"))
        assertTrue(result.actionsRecommandees.first().action.contains("Monter", ignoreCase = true))
        assertTrue((result.reglageSuggere?.detalonnageMm ?: 4.0) > 4.0)
    }

    @Test
    fun `impact centre retourne severite OPTIMAL`() {
        val result = DiagnosticEngine.analyserTir(
            impactHorizontal = 0.03,
            impactVertical = -0.04,
            lateralite = LateraliteArcher.DROITIER,
            reglageActuel = reglageBase
        )

        assertEquals(SeveriteDiagnostic.OPTIMAL, result.severiteGlobale)
    }

    @Test
    fun `calculerEcartDepuisImpacts calcule correctement le vecteur entre empennees et futs nus`() {
        val impacts = listOf(
            ImpactCible("1", 0.10f, 0.10f, TypeFlecheImpact.EMPENNEE),
            ImpactCible("2", 0.10f, 0.10f, TypeFlecheImpact.EMPENNEE),
            ImpactCible("3", -0.20f, -0.15f, TypeFlecheImpact.FUT_NU)
        )
        val (dx, dy) = DiagnosticEngine.calculerEcartDepuisImpacts(impacts)
        assertEquals(-0.30, dx, 0.001)
        assertEquals(-0.25, dy, 0.001)
    }
}
