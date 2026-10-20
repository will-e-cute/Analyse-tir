package com.example.analyse_tir.types

/**
 * Latéralité de l'archer.
 * Détermine le sens d'interprétation de l'écart horizontal (fût nu / papier).
 */
enum class LateraliteArcher(val label: String) {
    DROITIER("Droitier (RH)"),
    GAUCHER("Gaucher (LH)")
}

/**
 * Type de test effectué lors d'une séance de réglage.
 */
enum class TypeTest(val label: String) {
    FUT_NU("Fût Nu (Bare Shaft)"),
    TEST_PAPIER("Test Papier (Paper Tune)")
}

/**
 * Type de flèche impactant la cible interactive.
 */
enum class TypeFlecheImpact(val label: String) {
    EMPENNEE("Flèche Empennée"),
    FUT_NU("Fût Nu")
}

/**
 * Zones discrètes d'impact horizontal (-1.0 = extrême gauche, +1.0 = extrême droite).
 */
enum class ZoneHorizontale(val label: String) {
    FORTEMENT_GAUCHE("Fortement à gauche"),
    GAUCHE("À gauche"),
    CENTRE("Centré"),
    DROITE("À droite"),
    FORTEMENT_DROITE("Fortement à droite")
}

/**
 * Zones discrètes d'impact vertical (-1.0 = extrême bas, +1.0 = extrême haut).
 */
enum class ZoneVerticale(val label: String) {
    FORTEMENT_BAS("Fortement en bas"),
    BAS("En bas"),
    CENTRE("Centré"),
    HAUT("En haut"),
    FORTEMENT_HAUT("Fortement en haut")
}

/**
 * Symptômes visuels observés en vol ou en cible.
 */
enum class SymptomeVol(val label: String) {
    VOL_PROPRE("Vol net et groupé"),
    MARSouinage("Marsouinage (Porpoising vertical)"),
    FISHTAILING("Godille horizontale (Fishtailing)"),
    CONTACT_FENETRE("Contact fenêtre / repose-flèche"),
    DECHIRURE_GAUCHE("Déchirure papier pointe à gauche"),
    DECHIRURE_DROITE("Déchirure papier pointe à droite"),
    DECHIRURE_HAUT("Déchirure papier pointe en haut"),
    DECHIRURE_BAS("Déchirure papier pointe en bas")
}

/**
 * 1. Entité `profil_materiel` : Configurations d'arc (Puissance, Allonge, Modèle, Latéralité).
 */
data class ProfilMateriel(
    val id: Long = 0L,
    val nomProfil: String,
    val modeleArc: String,
    val puissanceLivres: Double, // ex: 38.5 lbs
    val allongePouces: Double,   // ex: 28.5"
    val tailleArcPouces: Int,    // ex: 68" ou 70"
    val lateralite: LateraliteArcher = LateraliteArcher.DROITIER,
    val dateCreation: Long = System.currentTimeMillis()
)

/**
 * 2. Entité `set_fleches` : Lots de flèches (Spine, Longueur, Poids de pointe).
 */
data class SetFleches(
    val id: Long = 0L,
    val nomLot: String,
    val modeleTube: String,      // ex: Easton X10, Skylon Paragon
    val spine: Int,              // ex: 550, 600, 650
    val longueurPouces: Double,  // ex: 29.0"
    val poidsPointeGrains: Int,  // ex: 110 gr, 120 gr
    val poidsTotalGrains: Int,   // ex: 365 gr
    val nombrePlumes: Int = 3,
    val dateCreation: Long = System.currentTimeMillis()
)

/**
 * 3. Entité `journal_reglage` : État de l'arc à l'instant T (Band, Détalonnage, Dureté/Sortie Berger).
 */
data class JournalReglage(
    val id: Long = 0L,
    val profilMaterielId: Long,
    val setFlechesId: Long,
    val bandMm: Double,              // ex: 222.0 mm (8.75")
    val detalonnageMm: Double,       // ex: +3.5 mm au-dessus de l'équerre
    val dureteBergerClics: Int,      // ex: 12 clics depuis le plus souple (ou échelle 1..30)
    val sortieBergerMm: Double,      // ex: +1.5 mm (centershot)
    val tillerDiffMm: Double = 4.0,  // Différentiel de tiller haut-bas en mm
    val distanceTirMetres: Int = 18, // ex: 18m, 30m, 50m, 70m
    val commentaire: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Point d'impact unitaire sur la cible interactive (coordonnées normalisées -1.0 .. +1.0).
 * x : -1.0 (Gauche) à +1.0 (Droite)
 * y : -1.0 (Bas) à +1.0 (Haut)
 */
data class ImpactCible(
    val id: String,
    val x: Float,
    val y: Float,
    val typeFleche: TypeFlecheImpact
)

/**
 * 4. Entité `test_vol` : Résultats de tir (Impact Horizontal/Vertical, Symptômes).
 * `impactHorizontal` : écart relatif du fût nu par rapport au groupe empenné (-1.0 = Gauche, +1.0 = Droite)
 * `impactVertical` : écart relatif du fût nu par rapport au groupe empenné (-1.0 = Bas, +1.0 = Haut)
 */
data class TestVol(
    val id: Long = 0L,
    val journalReglageId: Long,
    val typeTest: TypeTest = TypeTest.FUT_NU,
    val impactHorizontal: Double, // -1.0 (Gauche) .. +1.0 (Droite)
    val impactVertical: Double,   // -1.0 (Bas) .. +1.0 (Haut)
    val ecartHorizontalCm: Double,
    val ecartVerticalCm: Double,
    val symptomes: List<SymptomeVol> = emptyList(),
    val diagnosticResume: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Niveau de priorité ou de sévérité d'un axe de réglage.
 */
enum class SeveriteDiagnostic {
    OPTIMAL,
    LEGER_AJUSTEMENT,
    CORRECTION_IMPORTANTE
}

/**
 * Action concrète recommandée sur l'arc ou la flèche.
 */
data class ActionCorrective(
    val parametre: String,       // ex: "Détalonnage (Nocking Point)", "Ressort Berger Button", "Puissance d'arc"
    val action: String,          // ex: "Descendre le repère de détalonnage"
    val valeurSuggeree: String,  // ex: "-1.0 mm à -1.5 mm" ou "-2 à -3 clics"
    val priorite: Int            // 1 = Priorité haute (faire en premier)
)

/**
 * Résultat complet retourné par le moteur de diagnostic pur (`DiagnosticEngine`).
 */
data class DiagnosticResult(
    val severiteGlobale: SeveriteDiagnostic,
    val etatSpine: String,
    val etatDetalonnage: String,
    val explicationPhysique: String,
    val zoneHorizontale: ZoneHorizontale,
    val zoneVerticale: ZoneVerticale,
    val ecartHorizontalCm: Double,
    val ecartVerticalCm: Double,
    val actionsRecommandees: List<ActionCorrective>,
    val reglageSuggere: JournalReglage? = null
)
