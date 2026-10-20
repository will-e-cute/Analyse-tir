package com.example.analyse_tir.store

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analyse_tir.db.ArcheryDatabaseHelper
import com.example.analyse_tir.types.DiagnosticResult
import com.example.analyse_tir.types.ImpactCible
import com.example.analyse_tir.types.JournalReglage
import com.example.analyse_tir.types.LateraliteArcher
import com.example.analyse_tir.types.ProfilMateriel
import com.example.analyse_tir.types.SetFleches
import com.example.analyse_tir.types.SymptomeVol
import com.example.analyse_tir.types.TestVol
import com.example.analyse_tir.types.TypeFlecheImpact
import com.example.analyse_tir.types.TypeTest
import com.example.analyse_tir.utils.AppLanguage
import com.example.analyse_tir.utils.AppStrings
import com.example.analyse_tir.utils.AppTranslations
import com.example.analyse_tir.utils.DiagnosticEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

/**
 * État global immuable combinant :
 * - Langue active de l'interface (FR, EN, NL, ES, DE, IT)
 * - Equipment Store (Profils d'arc, Sets de flèches, Journaux de réglage)
 * - Session Store (Impacts sur cible en cours, type de flèche sélectionnée, symptômes, diagnostic instantané)
 */
data class ArcheryUiState(
    val langue: AppLanguage = AppLanguage.FR,
    val profils: List<ProfilMateriel> = emptyList(),
    val setsFleches: List<SetFleches> = emptyList(),
    val journauxReglage: List<JournalReglage> = emptyList(),
    val historiqueTests: List<TestVol> = emptyList(),

    val profilActifId: Long? = null,
    val setFlechesActifId: Long? = null,

    // Réglage courant sur le pas de tir (modifiable d'une seule main par clics)
    val bandMm: Double = 224.0,
    val detalonnageMm: Double = 4.0,
    val dureteBergerClics: Int = 14,
    val sortieBergerMm: Double = 1.5,
    val tillerDiffMm: Double = 4.0,
    val distanceTirMetres: Int = 18,

    // Session de tir en cours
    val typeTestActif: TypeTest = TypeTest.FUT_NU,
    val modeSaisieImpact: TypeFlecheImpact = TypeFlecheImpact.FUT_NU,
    val impactsCible: List<ImpactCible> = listOf(
        ImpactCible(id = "seed-emp-1", x = 0.02f, y = -0.01f, typeFleche = TypeFlecheImpact.EMPENNEE),
        ImpactCible(id = "seed-emp-2", x = -0.03f, y = 0.02f, typeFleche = TypeFlecheImpact.EMPENNEE),
        ImpactCible(id = "seed-emp-3", x = 0.01f, y = -0.01f, typeFleche = TypeFlecheImpact.EMPENNEE),
        ImpactCible(id = "seed-nu-1", x = -0.26f, y = -0.18f, typeFleche = TypeFlecheImpact.FUT_NU)
    ),
    val symptomesSelectionnes: Set<SymptomeVol> = emptySet(),
    val commentaireSession: String = "",
    val dernierMessageConfirmation: String? = null
) {
    val strings: AppStrings
        get() = AppTranslations.forLanguage(langue)

    val profilActif: ProfilMateriel?
        get() = profils.firstOrNull { it.id == profilActifId } ?: profils.firstOrNull()

    val setFlechesActif: SetFleches?
        get() = setsFleches.firstOrNull { it.id == setFlechesActifId } ?: setsFleches.firstOrNull()

    val reglageEnCours: JournalReglage
        get() = JournalReglage(
            profilMaterielId = profilActif?.id ?: 1L,
            setFlechesId = setFlechesActif?.id ?: 1L,
            bandMm = bandMm,
            detalonnageMm = detalonnageMm,
            dureteBergerClics = dureteBergerClics,
            sortieBergerMm = sortieBergerMm,
            tillerDiffMm = tillerDiffMm,
            distanceTirMetres = distanceTirMetres,
            commentaire = commentaireSession
        )

    val diagnosticTempsReel: DiagnosticResult
        get() {
            val (ecartX, ecartY) = DiagnosticEngine.calculerEcartDepuisImpacts(impactsCible)
            return DiagnosticEngine.analyserTir(
                impactHorizontal = ecartX,
                impactVertical = ecartY,
                lateralite = profilActif?.lateralite ?: LateraliteArcher.DROITIER,
                typeTest = typeTestActif,
                symptomes = symptomesSelectionnes.toList(),
                reglageActuel = reglageEnCours,
                profil = profilActif,
                fleches = setFlechesActif,
                langue = langue
            )
        }
}

class ArcheryViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = ArcheryDatabaseHelper(application.applicationContext)
    private val prefs = application.getSharedPreferences("analyse_tir_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        ArcheryUiState(
            langue = runCatching {
                val savedCode = prefs.getString("app_language", AppLanguage.FR.name) ?: AppLanguage.FR.name
                AppLanguage.valueOf(savedCode)
            }.getOrDefault(AppLanguage.FR)
        )
    )
    val uiState: StateFlow<ArcheryUiState> = _uiState.asStateFlow()

    init {
        chargerDonneesDepuisSqlite()
    }

    fun changerLangue(nouvelleLangue: AppLanguage) {
        prefs.edit().putString("app_language", nouvelleLangue.name).apply()
        _uiState.update { it.copy(langue = nouvelleLangue) }
    }

    fun chargerDonneesDepuisSqlite() {
        viewModelScope.launch(Dispatchers.IO) {
            val profils = dbHelper.getAllProfils()
            val fleches = dbHelper.getAllSetsFleches()
            val reglages = dbHelper.getAllJournauxReglage()
            val tests = dbHelper.getAllTestsVol()

            val dernierReglage = reglages.firstOrNull()

            _uiState.update { current ->
                current.copy(
                    profils = profils,
                    setsFleches = fleches,
                    journauxReglage = reglages,
                    historiqueTests = tests,
                    profilActifId = current.profilActifId ?: dernierReglage?.profilMaterielId ?: profils.firstOrNull()?.id,
                    setFlechesActifId = current.setFlechesActifId ?: dernierReglage?.setFlechesId ?: fleches.firstOrNull()?.id,
                    bandMm = dernierReglage?.bandMm ?: current.bandMm,
                    detalonnageMm = dernierReglage?.detalonnageMm ?: current.detalonnageMm,
                    dureteBergerClics = dernierReglage?.dureteBergerClics ?: current.dureteBergerClics,
                    sortieBergerMm = dernierReglage?.sortieBergerMm ?: current.sortieBergerMm,
                    tillerDiffMm = dernierReglage?.tillerDiffMm ?: current.tillerDiffMm,
                    distanceTirMetres = dernierReglage?.distanceTirMetres ?: current.distanceTirMetres
                )
            }
        }
    }

    // =========================================================================
    // Actions Session de Tir (Cible Interactive & Symptômes)
    // =========================================================================

    fun changerModeSaisieImpact(mode: TypeFlecheImpact) {
        _uiState.update { it.copy(modeSaisieImpact = mode) }
    }

    fun changerTypeTest(typeTest: TypeTest) {
        _uiState.update { it.copy(typeTestActif = typeTest) }
    }

    fun ajouterImpactCible(xNormalise: Float, yNormalise: Float) {
        val nouvelImpact = ImpactCible(
            id = UUID.randomUUID().toString(),
            x = xNormalise.coerceIn(-1f, 1f),
            y = yNormalise.coerceIn(-1f, 1f),
            typeFleche = _uiState.value.modeSaisieImpact
        )
        _uiState.update { state ->
            state.copy(impactsCible = state.impactsCible + nouvelImpact)
        }
    }

    fun annulerDernierImpact() {
        _uiState.update { state ->
            if (state.impactsCible.isEmpty()) state
            else state.copy(impactsCible = state.impactsCible.dropLast(1))
        }
    }

    fun viderImpactsCible() {
        _uiState.update { state ->
            state.copy(
                impactsCible = listOf(
                    ImpactCible(
                        id = UUID.randomUUID().toString(),
                        x = 0f,
                        y = 0f,
                        typeFleche = TypeFlecheImpact.EMPENNEE
                    )
                )
            )
        }
    }

    fun toggleSymptome(symptome: SymptomeVol) {
        _uiState.update { state ->
            val next = state.symptomesSelectionnes.toMutableSet()
            if (!next.add(symptome)) {
                next.remove(symptome)
            }
            state.copy(symptomesSelectionnes = next)
        }
    }

    // =========================================================================
    // Actions Réglages d'Arc à une main (Clics Berger, Détalonnage, Band, Latéralité)
    // =========================================================================

    fun ajusterDureteBerger(deltaClics: Int) {
        _uiState.update { state ->
            state.copy(dureteBergerClics = (state.dureteBergerClics + deltaClics).coerceIn(0, 40))
        }
    }

    fun ajusterSortieBerger(deltaMm: Double) {
        _uiState.update { state ->
            val next = ((state.sortieBergerMm + deltaMm) * 10.0).roundToInt() / 10.0
            state.copy(sortieBergerMm = next.coerceIn(-5.0, 10.0))
        }
    }

    fun ajusterDetalonnage(deltaMm: Double) {
        _uiState.update { state ->
            val next = ((state.detalonnageMm + deltaMm) * 10.0).roundToInt() / 10.0
            state.copy(detalonnageMm = next.coerceIn(-5.0, 18.0))
        }
    }

    fun ajusterBand(deltaMm: Double) {
        _uiState.update { state ->
            val next = ((state.bandMm + deltaMm) * 10.0).roundToInt() / 10.0
            state.copy(bandMm = next.coerceIn(170.0, 260.0))
        }
    }

    fun changerDistanceTir(distanceMetres: Int) {
        _uiState.update { it.copy(distanceTirMetres = distanceMetres) }
    }

    fun basculerLateraliteProfilActif() {
        val profil = _uiState.value.profilActif ?: return
        val nouvelleLat = if (profil.lateralite == LateraliteArcher.DROITIER) {
            LateraliteArcher.GAUCHER
        } else {
            LateraliteArcher.DROITIER
        }
        val profilMisAJour = profil.copy(lateralite = nouvelleLat)
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.updateProfil(profilMisAJour)
            chargerDonneesDepuisSqlite()
        }
    }

    fun appliquerReglageSuggereParDiagnostic() {
        val suggere = _uiState.value.diagnosticTempsReel.reglageSuggere ?: return
        _uiState.update { state ->
            state.copy(
                detalonnageMm = suggere.detalonnageMm,
                dureteBergerClics = suggere.dureteBergerClics,
                sortieBergerMm = suggere.sortieBergerMm,
                dernierMessageConfirmation = state.strings.msgAppliedSuggested
            )
        }
    }

    fun enregistrerVolEtReglageDansSqlite() {
        val state = _uiState.value
        val diagnostic = state.diagnosticTempsReel
        val (ecartX, ecartY) = DiagnosticEngine.calculerEcartDepuisImpacts(state.impactsCible)

        viewModelScope.launch(Dispatchers.IO) {
            val reglageId = dbHelper.insertJournalReglage(
                state.reglageEnCours.copy(
                    commentaire = state.commentaireSession.ifBlank {
                        "${diagnostic.etatDetalonnage} | ${diagnostic.etatSpine}"
                    },
                    timestamp = System.currentTimeMillis()
                )
            )
            dbHelper.insertTestVol(
                TestVol(
                    journalReglageId = reglageId,
                    typeTest = state.typeTestActif,
                    impactHorizontal = ecartX,
                    impactVertical = ecartY,
                    ecartHorizontalCm = diagnostic.ecartHorizontalCm,
                    ecartVerticalCm = diagnostic.ecartVerticalCm,
                    symptomes = state.symptomesSelectionnes.toList(),
                    diagnosticResume = "${diagnostic.etatDetalonnage} • ${diagnostic.etatSpine}",
                    timestamp = System.currentTimeMillis()
                )
            )
            chargerDonneesDepuisSqlite()
            _uiState.update {
                it.copy(
                    dernierMessageConfirmation = it.strings.msgSavedToSqlite
                )
            }
        }
    }

    fun effacerMessageConfirmation() {
        _uiState.update { it.copy(dernierMessageConfirmation = null) }
    }

    // =========================================================================
    // Gestion du Matériel (Profils d'arc & Lots de flèches)
    // =========================================================================

    fun selectionnerProfilActif(id: Long) {
        _uiState.update { it.copy(profilActifId = id) }
    }

    fun selectionnerSetFlechesActif(id: Long) {
        _uiState.update { it.copy(setFlechesActifId = id) }
    }

    fun creerProfilMateriel(
        nomProfil: String,
        modeleArc: String,
        puissanceLivres: Double,
        allongePouces: Double,
        tailleArcPouces: Int,
        lateralite: LateraliteArcher
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = dbHelper.insertProfil(
                ProfilMateriel(
                    nomProfil = nomProfil,
                    modeleArc = modeleArc,
                    puissanceLivres = puissanceLivres,
                    allongePouces = allongePouces,
                    tailleArcPouces = tailleArcPouces,
                    lateralite = lateralite
                )
            )
            _uiState.update { it.copy(profilActifId = newId) }
            chargerDonneesDepuisSqlite()
        }
    }

    fun creerSetFleches(
        nomLot: String,
        modeleTube: String,
        spine: Int,
        longueurPouces: Double,
        poidsPointeGrains: Int,
        poidsTotalGrains: Int,
        nombrePlumes: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = dbHelper.insertSetFleches(
                SetFleches(
                    nomLot = nomLot,
                    modeleTube = modeleTube,
                    spine = spine,
                    longueurPouces = longueurPouces,
                    poidsPointeGrains = poidsPointeGrains,
                    poidsTotalGrains = poidsTotalGrains,
                    nombrePlumes = nombrePlumes
                )
            )
            _uiState.update { it.copy(setFlechesActifId = newId) }
            chargerDonneesDepuisSqlite()
        }
    }

    fun supprimerProfilMateriel(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteProfil(id)
            _uiState.update { state ->
                val nextId = if (state.profilActifId == id) null else state.profilActifId
                state.copy(
                    profilActifId = nextId,
                    dernierMessageConfirmation = state.strings.msgBowDeleted
                )
            }
            chargerDonneesDepuisSqlite()
        }
    }

    fun supprimerSetFleches(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteSetFleches(id)
            _uiState.update { state ->
                val nextId = if (state.setFlechesActifId == id) null else state.setFlechesActifId
                state.copy(
                    setFlechesActifId = nextId,
                    dernierMessageConfirmation = state.strings.msgArrowsDeleted
                )
            }
            chargerDonneesDepuisSqlite()
        }
    }

    fun supprimerTestVol(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteTestVol(id)
            chargerDonneesDepuisSqlite()
        }
    }
}
