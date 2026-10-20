package com.example.analyse_tir.utils

import com.example.analyse_tir.types.ActionCorrective
import com.example.analyse_tir.types.DiagnosticResult
import com.example.analyse_tir.types.ImpactCible
import com.example.analyse_tir.types.JournalReglage
import com.example.analyse_tir.types.LateraliteArcher
import com.example.analyse_tir.types.ProfilMateriel
import com.example.analyse_tir.types.SetFleches
import com.example.analyse_tir.types.SeveriteDiagnostic
import com.example.analyse_tir.types.SymptomeVol
import com.example.analyse_tir.types.TypeFlecheImpact
import com.example.analyse_tir.types.TypeTest
import com.example.analyse_tir.types.ZoneHorizontale
import com.example.analyse_tir.types.ZoneVerticale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Moteur de diagnostic balistique pur (sans effet de bord) avec support multilingue (FR, EN, NL, ES, DE, IT).
 */
object DiagnosticEngine {

    private const val SEUIL_CENTRE = 0.12
    private const val SEUIL_FORT = 0.45

    fun calculerEcartDepuisImpacts(
        impacts: List<ImpactCible>,
        rayonBlasonCm: Double = 40.0
    ): Pair<Double, Double> {
        val empennees = impacts.filter { it.typeFleche == TypeFlecheImpact.EMPENNEE }
        val futsNus = impacts.filter { it.typeFleche == TypeFlecheImpact.FUT_NU }

        if (futsNus.isEmpty()) return Pair(0.0, 0.0)

        val centreEmpenneesX = if (empennees.isNotEmpty()) {
            empennees.map { it.x.toDouble() }.average()
        } else {
            0.0
        }
        val centreEmpenneesY = if (empennees.isNotEmpty()) {
            empennees.map { it.y.toDouble() }.average()
        } else {
            0.0
        }

        val centreNuX = futsNus.map { it.x.toDouble() }.average()
        val centreNuY = futsNus.map { it.y.toDouble() }.average()

        val deltaX = (centreNuX - centreEmpenneesX).coerceIn(-1.0, 1.0)
        val deltaY = (centreNuY - centreEmpenneesY).coerceIn(-1.0, 1.0)
        return Pair(deltaX, deltaY)
    }

    fun qualifierZoneHorizontale(impactHorizontal: Double): ZoneHorizontale = when {
        impactHorizontal <= -SEUIL_FORT -> ZoneHorizontale.FORTEMENT_GAUCHE
        impactHorizontal < -SEUIL_CENTRE -> ZoneHorizontale.GAUCHE
        impactHorizontal <= SEUIL_CENTRE -> ZoneHorizontale.CENTRE
        impactHorizontal < SEUIL_FORT -> ZoneHorizontale.DROITE
        else -> ZoneHorizontale.FORTEMENT_DROITE
    }

    fun qualifierZoneVerticale(impactVertical: Double): ZoneVerticale = when {
        impactVertical <= -SEUIL_FORT -> ZoneVerticale.FORTEMENT_BAS
        impactVertical < -SEUIL_CENTRE -> ZoneVerticale.BAS
        impactVertical <= SEUIL_CENTRE -> ZoneVerticale.CENTRE
        impactVertical < SEUIL_FORT -> ZoneVerticale.HAUT
        else -> ZoneVerticale.FORTEMENT_HAUT
    }

    fun analyserTir(
        impactHorizontal: Double,
        impactVertical: Double,
        lateralite: LateraliteArcher,
        typeTest: TypeTest = TypeTest.FUT_NU,
        symptomes: List<SymptomeVol> = emptyList(),
        reglageActuel: JournalReglage? = null,
        profil: ProfilMateriel? = null,
        fleches: SetFleches? = null,
        rayonBlasonCm: Double = 40.0,
        langue: AppLanguage = AppLanguage.FR
    ): DiagnosticResult {
        val normX = impactHorizontal.coerceIn(-1.0, 1.0)
        val normY = impactVertical.coerceIn(-1.0, 1.0)

        val ecartHorizCm = ((normX * rayonBlasonCm) * 10.0).roundToInt() / 10.0
        val ecartVertCm = ((normY * rayonBlasonCm) * 10.0).roundToInt() / 10.0

        val zoneH = qualifierZoneHorizontale(normX)
        val zoneV = qualifierZoneVerticale(normY)

        // Inversion de l'axe horizontal pour un archer gaucher
        val indexRigidite = when (lateralite) {
            LateraliteArcher.DROITIER -> normX
            LateraliteArcher.GAUCHER -> -normX
        }

        val actions = mutableListOf<ActionCorrective>()
        val explications = mutableListOf<String>()

        var nouveauDetalonnage = reglageActuel?.detalonnageMm
        var nouvelleDureteBerger = reglageActuel?.dureteBergerClics
        var nouvelleSortieBerger = reglageActuel?.sortieBergerMm

        // 1. Analyse Verticale (Détalonnage / Nocking point)
        val etatDetalonnage: String = when {
            normY < -SEUIL_FORT -> {
                val deltaMm = -1.5
                nouveauDetalonnage = nouveauDetalonnage?.let { ((it + deltaMm) * 10.0).roundToInt() / 10.0 }
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Détalonnage (Repère)", "Nocking Point", "Nokpunt", "Punto de Enfleche", "Nockpunkt", "Punto di Incocco"),
                        action = tr(langue, "Descendre franchement le repère sur la corde", "Lower the nocking point significantly", "Nokpunt flink verlagen", "Bajar claramente el punto de enfleche", "Nockpunkt deutlich absenken", "Abbassare nettamente il punto di incocco"),
                        valeurSuggeree = "-1.5 .. -2.0 mm",
                        priorite = 1
                    )
                )
                explications.add(
                    tr(
                        langue,
                        "Le fût nu plonge fortement en bas ($ecartVertCm cm) : détalonnage nettement trop haut.",
                        "Bare shaft dives strongly low ($ecartVertCm cm): nocking point is much too high.",
                        "Kale schacht duikt sterk omlaag ($ecartVertCm cm): nokpunt veel te hoog.",
                        "El tubo desnudo cae muy bajo ($ecartVertCm cm): punto de enfleche demasiado alto.",
                        "Rohschaft schlägt deutlich zu tief ein ($ecartVertCm cm): Nockpunkt deutlich zu hoch.",
                        "L'asta nuda impatta molto in basso ($ecartVertCm cm): punto di incocco troppo alto."
                    )
                )
                tr(langue, "Détalonnage nettement trop HAUT", "Nocking point much too HIGH", "Nokpunt veel te HOOG", "Enfleche muy ALTO", "Nockpunkt viel zu HOCH", "Incocco molto ALTO")
            }
            normY < -SEUIL_CENTRE -> {
                val deltaMm = -0.8
                nouveauDetalonnage = nouveauDetalonnage?.let { ((it + deltaMm) * 10.0).roundToInt() / 10.0 }
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Détalonnage (Repère)", "Nocking Point", "Nokpunt", "Punto de Enfleche", "Nockpunkt", "Punto di Incocco"),
                        action = tr(langue, "Descendre légèrement le repère sur la corde", "Lower the nocking point slightly", "Nokpunt iets verlagen", "Bajar ligeramente el punto de enfleche", "Nockpunkt leicht absenken", "Abbassare leggermente il punto di incocco"),
                        valeurSuggeree = "-0.5 .. -1.0 mm",
                        priorite = 1
                    )
                )
                explications.add(
                    tr(
                        langue,
                        "Impact fût nu en bas ($ecartVertCm cm) : détalonnage trop haut.",
                        "Bare shaft impacts low ($ecartVertCm cm): nocking point too high.",
                        "Kale schacht laag ($ecartVertCm cm): nokpunt te hoog.",
                        "Impacto tubo desnudo bajo ($ecartVertCm cm): enfleche demasiado alto.",
                        "Rohschaft tief ($ecartVertCm cm): Nockpunkt zu hoch.",
                        "Asta nuda in basso ($ecartVertCm cm): incocco troppo alto."
                    )
                )
                tr(langue, "Détalonnage trop HAUT", "Nocking point too HIGH", "Nokpunt te HOOG", "Enfleche demasiado ALTO", "Nockpunkt zu HOCH", "Incocco troppo ALTO")
            }
            normY > SEUIL_FORT -> {
                val deltaMm = 1.5
                nouveauDetalonnage = nouveauDetalonnage?.let { ((it + deltaMm) * 10.0).roundToInt() / 10.0 }
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Détalonnage (Repère)", "Nocking Point", "Nokpunt", "Punto de Enfleche", "Nockpunkt", "Punto di Incocco"),
                        action = tr(langue, "Monter franchement le repère sur la corde", "Raise the nocking point significantly", "Nokpunt flink verhogen", "Subir claramente el punto de enfleche", "Nockpunkt deutlich anheben", "Alzare nettamente il punto di incocco"),
                        valeurSuggeree = "+1.5 .. +2.0 mm",
                        priorite = 1
                    )
                )
                explications.add(
                    tr(
                        langue,
                        "Le fût nu impacte fortement en haut (+$ecartVertCm cm) : détalonnage nettement trop bas.",
                        "Bare shaft impacts strongly high (+$ecartVertCm cm): nocking point much too low.",
                        "Kale schacht sterk hoog (+$ecartVertCm cm): nokpunt veel te laag.",
                        "El tubo desnudo impacta muy alto (+$ecartVertCm cm): enfleche demasiado bajo.",
                        "Rohschaft deutlich zu hoch (+$ecartVertCm cm): Nockpunkt viel zu tief.",
                        "L'asta nuda impatta molto in alto (+$ecartVertCm cm): incocco troppo basso."
                    )
                )
                tr(langue, "Détalonnage nettement trop BAS", "Nocking point much too LOW", "Nokpunt veel te LAAG", "Enfleche muy BAJO", "Nockpunkt viel zu TIEF", "Incocco molto BASSO")
            }
            normY > SEUIL_CENTRE -> {
                val deltaMm = 0.8
                nouveauDetalonnage = nouveauDetalonnage?.let { ((it + deltaMm) * 10.0).roundToInt() / 10.0 }
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Détalonnage (Repère)", "Nocking Point", "Nokpunt", "Punto de Enfleche", "Nockpunkt", "Punto di Incocco"),
                        action = tr(langue, "Monter légèrement le repère sur la corde", "Raise the nocking point slightly", "Nokpunt iets verhogen", "Subir ligeramente el punto de enfleche", "Nockpunkt leicht anheben", "Alzare leggermente il punto di incocco"),
                        valeurSuggeree = "+0.5 .. +1.0 mm",
                        priorite = 1
                    )
                )
                explications.add(
                    tr(
                        langue,
                        "Impact fût nu en haut (+$ecartVertCm cm) : détalonnage trop bas.",
                        "Bare shaft impacts high (+$ecartVertCm cm): nocking point too low.",
                        "Kale schacht hoog (+$ecartVertCm cm): nokpunt te laag.",
                        "Impacto tubo desnudo alto (+$ecartVertCm cm): enfleche demasiado bajo.",
                        "Rohschaft hoch (+$ecartVertCm cm): Nockpunkt zu tief.",
                        "Asta nuda in alto (+$ecartVertCm cm): incocco troppo basso."
                    )
                )
                tr(langue, "Détalonnage trop BAS", "Nocking point too LOW", "Nokpunt te LAAG", "Enfleche demasiado BAJO", "Nockpunkt zu TIEF", "Incocco troppo BASSO")
            }
            else -> {
                explications.add(
                    tr(
                        langue,
                        "Hauteur de détalonnage optimale (écart vertical : $ecartVertCm cm).",
                        "Optimal nocking point height (vertical offset: $ecartVertCm cm).",
                        "Optimale nokpunthoogte (verticale afwijking: $ecartVertCm cm).",
                        "Altura de enfleche óptima (desvío vertical: $ecartVertCm cm).",
                        "Optimale Nockpunkthöhe (vertikale Abweichung: $ecartVertCm cm).",
                        "Altezza incocco ottimale (scarto verticale: $ecartVertCm cm)."
                    )
                )
                tr(langue, "Détalonnage optimal", "Optimal nocking point", "Nokpunt optimaal", "Enfleche óptimo", "Nockpunkt optimal", "Incocco ottimale")
            }
        }

        // 2. Analyse Horizontale (Spine dynamique / Berger Button / Puissance / Pointe)
        val etatSpine: String = when {
            indexRigidite < -SEUIL_FORT -> {
                val clicsDelta = -4
                nouvelleDureteBerger = nouvelleDureteBerger?.let { (it + clicsDelta).coerceAtLeast(1) }
                nouvelleSortieBerger = nouvelleSortieBerger?.let { ((it - 0.3) * 10.0).roundToInt() / 10.0 }
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Berger Button (Ressort)", "Plunger Button (Spring)", "Button (Veer)", "Botón Berger (Muelle)", "Button (Feder)", "Bottone Berger (Molla)"),
                        action = tr(langue, "Assouplir le ressort du Berger Button", "Soften plunger spring tension", "Buttonveer zachter zetten", "Aflojar el muelle del Berger", "Buttonfeder weicher stellen", "Ammorbidire la molla del Berger"),
                        valeurSuggeree = tr(langue, "-3 à -5 clics", "-3 to -5 clicks", "-3 tot -5 kliks", "-3 a -5 clics", "-3 bis -5 Klicks", "-3 a -5 click"),
                        priorite = 2
                    )
                )
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Puissance d'arc (Branches)", "Bow Draw Weight", "Trekgewicht (Boog)", "Potencia del Arco", "Zuggewicht (Wurfarme)", "Potenza Arco"),
                        action = tr(langue, "Augmenter la puissance de l'arc", "Increase bow draw weight", "Trekgewicht verhogen", "Aumentar la potencia del arco", "Zuggewicht erhöhen", "Aumentare la potenza dell'arco"),
                        valeurSuggeree = "+1/2 .. +1 tour",
                        priorite = 3
                    )
                )
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Flèche (Pointe)", "Arrow (Point Weight)", "Pijl (Puntgewicht)", "Flecha (Punta)", "Pfeil (Spitze)", "Freccia (Punta)"),
                        action = tr(langue, "Alourdir la pointe pour assouplir le tube", "Increase point weight to weaken dynamic spine", "Zwaardere punt gebruiken", "Aumentar peso de punta", "Schwerere Spitze verwenden", "Aumentare peso punta"),
                        valeurSuggeree = "+10 .. +20 gr",
                        priorite = 4
                    )
                )
                explications.add(
                    tr(
                        langue,
                        "Écart horizontal (${abs(ecartHorizCm)} cm) : la flèche réagit comme nettement TROP RIGIDE.",
                        "Horizontal offset (${abs(ecartHorizCm)} cm): arrow reacts as much TOO STIFF.",
                        "Horizontale afwijking (${abs(ecartHorizCm)} cm): pijl reageert veel TE STIJF.",
                        "Desvío horizontal (${abs(ecartHorizCm)} cm): la flecha reacciona como MUY RÍGIDA.",
                        "Horizontale Abweichung (${abs(ecartHorizCm)} cm): Pfeil reagiert deutlich ZU STEIF.",
                        "Scarto orizzontale (${abs(ecartHorizCm)} cm): la freccia reagisce come molto TROPPO RIGIDA."
                    )
                )
                tr(langue, "Flèche nettement trop RIGIDE", "Arrow much too STIFF", "Pijl veel te STIJF", "Flecha muy RÍGIDA", "Pfeil viel zu STEIF", "Freccia molto RIGIDA")
            }
            indexRigidite < -SEUIL_CENTRE -> {
                val clicsDelta = -2
                nouvelleDureteBerger = nouvelleDureteBerger?.let { (it + clicsDelta).coerceAtLeast(1) }
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Berger Button (Ressort)", "Plunger Button (Spring)", "Button (Veer)", "Botón Berger (Muelle)", "Button (Feder)", "Bottone Berger (Molla)"),
                        action = tr(langue, "Assouplir légèrement le Berger Button", "Soften plunger spring slightly", "Buttonveer iets zachter zetten", "Aflojar ligeramente el Berger", "Buttonfeder leicht weicher stellen", "Ammorbidire leggermente il Berger"),
                        valeurSuggeree = tr(langue, "-1 à -2 clics", "-1 to -2 clicks", "-1 tot -2 kliks", "-1 a -2 clics", "-1 bis -2 Klicks", "-1 a -2 click"),
                        priorite = 2
                    )
                )
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Puissance / Pointe", "Weight / Point", "Trekgewicht / Punt", "Potencia / Punta", "Zuggewicht / Spitze", "Potenza / Punta"),
                        action = tr(langue, "Optionnel : Augmenter puissance ou alourdir pointe", "Optional: Increase draw weight or point weight", "Optioneel: Trekgewicht of puntgewicht verhogen", "Opcional: Subir potencia o peso de punta", "Optional: Zuggewicht oder Spitze erhöhen", "Opzionale: Aumentare potenza o punta"),
                        valeurSuggeree = "+10 gr",
                        priorite = 3
                    )
                )
                explications.add(
                    tr(
                        langue,
                        "Écart horizontal (${abs(ecartHorizCm)} cm) : flèche légèrement trop RIGIDE.",
                        "Horizontal offset (${abs(ecartHorizCm)} cm): arrow slightly TOO STIFF.",
                        "Horizontale afwijking (${abs(ecartHorizCm)} cm): pijl iets TE STIJF.",
                        "Desvío horizontal (${abs(ecartHorizCm)} cm): flecha ligeramente RÍGIDA.",
                        "Horizontale Abweichung (${abs(ecartHorizCm)} cm): Pfeil leicht ZU STEIF.",
                        "Scarto orizzontale (${abs(ecartHorizCm)} cm): freccia leggermente RIGIDA."
                    )
                )
                tr(langue, "Flèche trop RIGIDE", "Arrow too STIFF", "Pijl te STIJF", "Flecha demasiado RÍGIDA", "Pfeil zu STEIF", "Freccia troppo RIGIDA")
            }
            indexRigidite > SEUIL_FORT -> {
                val clicsDelta = 4
                nouvelleDureteBerger = nouvelleDureteBerger?.let { it + clicsDelta }
                nouvelleSortieBerger = nouvelleSortieBerger?.let { ((it + 0.3) * 10.0).roundToInt() / 10.0 }
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Berger Button (Ressort)", "Plunger Button (Spring)", "Button (Veer)", "Botón Berger (Muelle)", "Button (Feder)", "Bottone Berger (Molla)"),
                        action = tr(langue, "Durcir le ressort du Berger Button", "Stiffen plunger spring tension", "Buttonveer stugger zetten", "Endurecer el muelle del Berger", "Buttonfeder härter stellen", "Indurire la molla del Berger"),
                        valeurSuggeree = tr(langue, "+3 à +5 clics", "+3 to +5 clicks", "+3 tot +5 kliks", "+3 a +5 clics", "+3 bis +5 Klicks", "+3 a +5 click"),
                        priorite = 2
                    )
                )
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Puissance d'arc (Branches)", "Bow Draw Weight", "Trekgewicht (Boog)", "Potencia del Arco", "Zuggewicht (Wurfarme)", "Potenza Arco"),
                        action = tr(langue, "Diminuer la puissance de l'arc", "Decrease bow draw weight", "Trekgewicht verlagen", "Disminuir la potencia del arco", "Zuggewicht reduzieren", "Diminuire la potenza dell'arco"),
                        valeurSuggeree = "-1/2 .. -1 tour",
                        priorite = 3
                    )
                )
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Flèche (Pointe / Tube)", "Arrow (Point / Shaft)", "Pijl (Punt / Schacht)", "Flecha (Punta / Tubo)", "Pfeil (Spitze / Schaft)", "Freccia (Punta / Asta)"),
                        action = tr(langue, "Alléger la pointe (ou raccourcir le tube)", "Decrease point weight (or shorten shaft)", "Lichtere punt (of kortere schacht)", "Aligerar la punta (o acortar tubo)", "Leichtere Spitze (oder Schaft kürzen)", "Alleggerire la punta (o accorciare asta)"),
                        valeurSuggeree = "-10 .. -20 gr",
                        priorite = 4
                    )
                )
                explications.add(
                    tr(
                        langue,
                        "Écart horizontal (${abs(ecartHorizCm)} cm) : la flèche réagit comme nettement TROP SOUPLE.",
                        "Horizontal offset (${abs(ecartHorizCm)} cm): arrow reacts as much TOO WEAK.",
                        "Horizontale afwijking (${abs(ecartHorizCm)} cm): pijl reageert veel TE SLAP.",
                        "Desvío horizontal (${abs(ecartHorizCm)} cm): la flecha reacciona como MUY BLANDA.",
                        "Horizontale Abweichung (${abs(ecartHorizCm)} cm): Pfeil reagiert deutlich ZU WEICH.",
                        "Scarto orizzontale (${abs(ecartHorizCm)} cm): la freccia reagisce come molto TROPPO MORBIDA."
                    )
                )
                tr(langue, "Flèche nettement trop SOUPLE", "Arrow much too WEAK", "Pijl veel te SLAP", "Flecha muy BLANDA", "Pfeil viel zu WEICH", "Freccia molto MORBIDA")
            }
            indexRigidite > SEUIL_CENTRE -> {
                val clicsDelta = 2
                nouvelleDureteBerger = nouvelleDureteBerger?.let { it + clicsDelta }
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Berger Button (Ressort)", "Plunger Button (Spring)", "Button (Veer)", "Botón Berger (Muelle)", "Button (Feder)", "Bottone Berger (Molla)"),
                        action = tr(langue, "Durcir légèrement le Berger Button", "Stiffen plunger spring slightly", "Buttonveer iets stugger zetten", "Endurecer ligeramente el Berger", "Buttonfeder leicht härter stellen", "Indurire leggermente il Berger"),
                        valeurSuggeree = tr(langue, "+1 à +2 clics", "+1 to +2 clicks", "+1 tot +2 kliks", "+1 a +2 clics", "+1 bis +2 Klicks", "+1 a +2 click"),
                        priorite = 2
                    )
                )
                actions.add(
                    ActionCorrective(
                        parametre = tr(langue, "Puissance / Pointe", "Weight / Point", "Trekgewicht / Punt", "Potencia / Punta", "Zuggewicht / Spitze", "Potenza / Punta"),
                        action = tr(langue, "Optionnel : Diminuer puissance ou alléger pointe", "Optional: Decrease draw weight or point weight", "Optioneel: Trekgewicht of puntgewicht verlagen", "Opcional: Bajar potencia o peso de punta", "Optional: Zuggewicht oder Spitze verringern", "Opzionale: Diminuire potenza o punta"),
                        valeurSuggeree = "-10 gr",
                        priorite = 3
                    )
                )
                explications.add(
                    tr(
                        langue,
                        "Écart horizontal (${abs(ecartHorizCm)} cm) : flèche légèrement trop SOUPLE.",
                        "Horizontal offset (${abs(ecartHorizCm)} cm): arrow slightly TOO WEAK.",
                        "Horizontale afwijking (${abs(ecartHorizCm)} cm): pijl iets TE SLAP.",
                        "Desvío horizontal (${abs(ecartHorizCm)} cm): flecha ligeramente BLANDA.",
                        "Horizontale Abweichung (${abs(ecartHorizCm)} cm): Pfeil leicht ZU WEICH.",
                        "Scarto orizzontale (${abs(ecartHorizCm)} cm): freccia leggermente MORBIDA."
                    )
                )
                tr(langue, "Flèche trop SOUPLE", "Arrow too WEAK", "Pijl te SLAP", "Flecha demasiado BLANDA", "Pfeil zu WEICH", "Freccia troppo MORBIDA")
            }
            else -> {
                explications.add(
                    tr(
                        langue,
                        "Spine dynamique et Berger Button équilibrés (écart horizontal : $ecartHorizCm cm).",
                        "Dynamic spine and plunger balanced (horizontal offset: $ecartHorizCm cm).",
                        "Dynamische spine en button in balans (horizontale afwijking: $ecartHorizCm cm).",
                        "Spine dinámico y Berger equilibrados (desvío horizontal: $ecartHorizCm cm).",
                        "Dynamischer Spine und Button ausgewogen (horizontale Abweichung: $ecartHorizCm cm).",
                        "Spine dinamico e Berger bilanciati (scarto orizzontale: $ecartHorizCm cm)."
                    )
                )
                tr(langue, "Spine optimal", "Optimal spine", "Spine optimaal", "Spine óptimo", "Spine optimal", "Spine ottimale")
            }
        }

        // 3. Prise en compte des symptômes complémentaires
        if (symptomes.contains(SymptomeVol.CONTACT_FENETRE)) {
            actions.add(
                ActionCorrective(
                    parametre = tr(langue, "Sortie Berger / Band", "Centershot / Brace", "Uitsteek / Spanhoogte", "Centershot / Fistmele", "Centershot / Standhöhe", "Centershot / Brace"),
                    action = tr(
                        langue,
                        "Vérifier l'alignement (centershot) et augmenter le band de 2 mm",
                        "Check centershot alignment and increase brace height by 2 mm",
                        "Controleer centershot en verhoog spanhoogte met 2 mm",
                        "Verificar centershot y aumentar fistmele en 2 mm",
                        "Centershot prüfen und Standhöhe um 2 mm erhöhen",
                        "Verificare centershot e aumentare brace di 2 mm"
                    ),
                    valeurSuggeree = "+0.5 mm / +2 mm",
                    priorite = 2
                )
            )
        }

        if (actions.isEmpty()) {
            actions.add(
                ActionCorrective(
                    parametre = tr(langue, "Configuration validée", "Setup validated", "Afstelling gevalideerd", "Configuración validada", "Setup bestätigt", "Configurazione validata"),
                    action = tr(
                        langue,
                        "Conserver ce réglage et valider à longue distance",
                        "Keep this setup and verify at long distance",
                        "Behoud deze afstelling en bevestig op lange afstand",
                        "Conservar este ajuste y validar a larga distancia",
                        "Einstellung beibehalten und auf weite Distanz prüfen",
                        "Mantenere questo assetto e validare a lunga distanza"
                    ),
                    valeurSuggeree = "OK",
                    priorite = 1
                )
            )
        }

        val severite = when {
            abs(normX) > SEUIL_FORT || abs(normY) > SEUIL_FORT -> SeveriteDiagnostic.CORRECTION_IMPORTANTE
            abs(normX) > SEUIL_CENTRE || abs(normY) > SEUIL_CENTRE || symptomes.contains(SymptomeVol.CONTACT_FENETRE) -> SeveriteDiagnostic.LEGER_AJUSTEMENT
            else -> SeveriteDiagnostic.OPTIMAL
        }

        val reglageSuggere = reglageActuel?.copy(
            detalonnageMm = nouveauDetalonnage ?: reglageActuel.detalonnageMm,
            dureteBergerClics = nouvelleDureteBerger ?: reglageActuel.dureteBergerClics,
            sortieBergerMm = nouvelleSortieBerger ?: reglageActuel.sortieBergerMm,
            timestamp = System.currentTimeMillis()
        )

        return DiagnosticResult(
            severiteGlobale = severite,
            etatSpine = etatSpine,
            etatDetalonnage = etatDetalonnage,
            explicationPhysique = explications.joinToString(" "),
            zoneHorizontale = zoneH,
            zoneVerticale = zoneV,
            ecartHorizontalCm = ecartHorizCm,
            ecartVerticalCm = ecartVertCm,
            actionsRecommandees = actions.sortedBy { it.priorite },
            reglageSuggere = reglageSuggere
        )
    }

    private fun tr(
        lang: AppLanguage,
        fr: String,
        en: String,
        nl: String,
        es: String,
        de: String,
        it: String
    ): String = when (lang) {
        AppLanguage.FR -> fr
        AppLanguage.EN -> en
        AppLanguage.NL -> nl
        AppLanguage.ES -> es
        AppLanguage.DE -> de
        AppLanguage.IT -> it
    }
}
