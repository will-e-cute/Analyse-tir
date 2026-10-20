package com.example.analyse_tir.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.analyse_tir.types.JournalReglage
import com.example.analyse_tir.types.LateraliteArcher
import com.example.analyse_tir.types.ProfilMateriel
import com.example.analyse_tir.types.SetFleches
import com.example.analyse_tir.types.SymptomeVol
import com.example.analyse_tir.types.TestVol
import com.example.analyse_tir.types.TypeTest

/**
 * Base de données SQLite locale (Offline-First) gérant les 4 entités relationnelles :
 * 1. `profil_materiel`
 * 2. `set_fleches`
 * 3. `journal_reglage`
 * 4. `test_vol`
 */
class ArcheryDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "analyse_tir_offline.db"
        const val DATABASE_VERSION = 1

        const val TABLE_PROFIL_MATERIEL = "profil_materiel"
        const val TABLE_SET_FLECHES = "set_fleches"
        const val TABLE_JOURNAL_REGLAGE = "journal_reglage"
        const val TABLE_TEST_VOL = "test_vol"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_PROFIL_MATERIEL (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom_profil TEXT NOT NULL,
                modele_arc TEXT NOT NULL,
                puissance_livres REAL NOT NULL,
                allonge_pouces REAL NOT NULL,
                taille_arc_pouces INTEGER NOT NULL,
                lateralite TEXT NOT NULL,
                date_creation INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_SET_FLECHES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom_lot TEXT NOT NULL,
                modele_tube TEXT NOT NULL,
                spine INTEGER NOT NULL,
                longueur_pouces REAL NOT NULL,
                poids_pointe_grains INTEGER NOT NULL,
                poids_total_grains INTEGER NOT NULL,
                nombre_plumes INTEGER NOT NULL,
                date_creation INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_JOURNAL_REGLAGE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                profil_materiel_id INTEGER NOT NULL,
                set_fleches_id INTEGER NOT NULL,
                band_mm REAL NOT NULL,
                detalonnage_mm REAL NOT NULL,
                durete_berger_clics INTEGER NOT NULL,
                sortie_berger_mm REAL NOT NULL,
                tiller_diff_mm REAL NOT NULL,
                distance_tir_metres INTEGER NOT NULL,
                commentaire TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY (profil_materiel_id) REFERENCES $TABLE_PROFIL_MATERIEL(id) ON DELETE CASCADE,
                FOREIGN KEY (set_fleches_id) REFERENCES $TABLE_SET_FLECHES(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_TEST_VOL (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                journal_reglage_id INTEGER NOT NULL,
                type_test TEXT NOT NULL,
                impact_horizontal REAL NOT NULL,
                impact_vertical REAL NOT NULL,
                ecart_horizontal_cm REAL NOT NULL,
                ecart_vertical_cm REAL NOT NULL,
                symptomes TEXT NOT NULL,
                diagnostic_resume TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY (journal_reglage_id) REFERENCES $TABLE_JOURNAL_REGLAGE(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TEST_VOL")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_JOURNAL_REGLAGE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SET_FLECHES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFIL_MATERIEL")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()

        val profilId = db.insert(
            TABLE_PROFIL_MATERIEL,
            null,
            ContentValues().apply {
                put("nom_profil", "Arc Classique FITA 70m")
                put("modele_arc", "Hoyt Formula XD / Uukha SX+")
                put("puissance_livres", 40.0)
                put("allonge_pouces", 29.0)
                put("taille_arc_pouces", 68)
                put("lateralite", LateraliteArcher.DROITIER.name)
                put("date_creation", now)
            }
        )

        val flecheId = db.insert(
            TABLE_SET_FLECHES,
            null,
            ContentValues().apply {
                put("nom_lot", "Lot Compétition Extérieur #1")
                put("modele_tube", "Easton X10")
                put("spine", 550)
                put("longueur_pouces", 29.25)
                put("poids_pointe_grains", 110)
                put("poids_total_grains", 372)
                put("nombre_plumes", 3)
                put("date_creation", now)
            }
        )

        val reglageId = db.insert(
            TABLE_JOURNAL_REGLAGE,
            null,
            ContentValues().apply {
                put("profil_materiel_id", profilId)
                put("set_fleches_id", flecheId)
                put("band_mm", 224.0)
                put("detalonnage_mm", 4.0)
                put("durete_berger_clics", 14)
                put("sortie_berger_mm", 1.5)
                put("tiller_diff_mm", 4.0)
                put("distance_tir_metres", 18)
                put("commentaire", "Réglage initial de référence avant test fût nu à 18m")
                put("timestamp", now)
            }
        )

        db.insert(
            TABLE_TEST_VOL,
            null,
            ContentValues().apply {
                put("journal_reglage_id", reglageId)
                put("type_test", TypeTest.FUT_NU.name)
                put("impact_horizontal", -0.25)
                put("impact_vertical", -0.18)
                put("ecart_horizontal_cm", -10.0)
                put("ecart_vertical_cm", -7.2)
                put("symptomes", SymptomeVol.VOL_PROPRE.name)
                put("diagnostic_resume", "Détalonnage trop HAUT • Flèche trop RIGIDE")
                put("timestamp", now)
            }
        )
    }

    // =========================================================================
    // Requêtes typées : PROFIL_MATERIEL
    // =========================================================================

    fun getAllProfils(): List<ProfilMateriel> {
        val list = mutableListOf<ProfilMateriel>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_PROFIL_MATERIEL ORDER BY date_creation DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursor.toProfilMateriel())
            }
        }
        return list
    }

    fun insertProfil(profil: ProfilMateriel): Long {
        val values = ContentValues().apply {
            put("nom_profil", profil.nomProfil)
            put("modele_arc", profil.modeleArc)
            put("puissance_livres", profil.puissanceLivres)
            put("allonge_pouces", profil.allongePouces)
            put("taille_arc_pouces", profil.tailleArcPouces)
            put("lateralite", profil.lateralite.name)
            put("date_creation", profil.dateCreation)
        }
        return writableDatabase.insert(TABLE_PROFIL_MATERIEL, null, values)
    }

    fun updateProfil(profil: ProfilMateriel) {
        val values = ContentValues().apply {
            put("nom_profil", profil.nomProfil)
            put("modele_arc", profil.modeleArc)
            put("puissance_livres", profil.puissanceLivres)
            put("allonge_pouces", profil.allongePouces)
            put("taille_arc_pouces", profil.tailleArcPouces)
            put("lateralite", profil.lateralite.name)
        }
        writableDatabase.update(
            TABLE_PROFIL_MATERIEL,
            values,
            "id = ?",
            arrayOf(profil.id.toString())
        )
    }

    fun deleteProfil(id: Long) {
        writableDatabase.delete(TABLE_PROFIL_MATERIEL, "id = ?", arrayOf(id.toString()))
    }

    // =========================================================================
    // Requêtes typées : SET_FLECHES
    // =========================================================================

    fun getAllSetsFleches(): List<SetFleches> {
        val list = mutableListOf<SetFleches>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_SET_FLECHES ORDER BY date_creation DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursor.toSetFleches())
            }
        }
        return list
    }

    fun insertSetFleches(setFleches: SetFleches): Long {
        val values = ContentValues().apply {
            put("nom_lot", setFleches.nomLot)
            put("modele_tube", setFleches.modeleTube)
            put("spine", setFleches.spine)
            put("longueur_pouces", setFleches.longueurPouces)
            put("poids_pointe_grains", setFleches.poidsPointeGrains)
            put("poids_total_grains", setFleches.poidsTotalGrains)
            put("nombre_plumes", setFleches.nombrePlumes)
            put("date_creation", setFleches.dateCreation)
        }
        return writableDatabase.insert(TABLE_SET_FLECHES, null, values)
    }

    fun deleteSetFleches(id: Long) {
        writableDatabase.delete(TABLE_SET_FLECHES, "id = ?", arrayOf(id.toString()))
    }

    // =========================================================================
    // Requêtes typées : JOURNAL_REGLAGE
    // =========================================================================

    fun getAllJournauxReglage(): List<JournalReglage> {
        val list = mutableListOf<JournalReglage>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_JOURNAL_REGLAGE ORDER BY timestamp DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursor.toJournalReglage())
            }
        }
        return list
    }

    fun insertJournalReglage(reglage: JournalReglage): Long {
        val values = ContentValues().apply {
            put("profil_materiel_id", reglage.profilMaterielId)
            put("set_fleches_id", reglage.setFlechesId)
            put("band_mm", reglage.bandMm)
            put("detalonnage_mm", reglage.detalonnageMm)
            put("durete_berger_clics", reglage.dureteBergerClics)
            put("sortie_berger_mm", reglage.sortieBergerMm)
            put("tiller_diff_mm", reglage.tillerDiffMm)
            put("distance_tir_metres", reglage.distanceTirMetres)
            put("commentaire", reglage.commentaire)
            put("timestamp", reglage.timestamp)
        }
        return writableDatabase.insert(TABLE_JOURNAL_REGLAGE, null, values)
    }

    // =========================================================================
    // Requêtes typées : TEST_VOL
    // =========================================================================

    fun getAllTestsVol(): List<TestVol> {
        val list = mutableListOf<TestVol>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_TEST_VOL ORDER BY timestamp DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursor.toTestVol())
            }
        }
        return list
    }

    fun insertTestVol(testVol: TestVol): Long {
        val values = ContentValues().apply {
            put("journal_reglage_id", testVol.journalReglageId)
            put("type_test", testVol.typeTest.name)
            put("impact_horizontal", testVol.impactHorizontal)
            put("impact_vertical", testVol.impactVertical)
            put("ecart_horizontal_cm", testVol.ecartHorizontalCm)
            put("ecart_vertical_cm", testVol.ecartVerticalCm)
            put("symptomes", testVol.symptomes.joinToString(",") { it.name })
            put("diagnostic_resume", testVol.diagnosticResume)
            put("timestamp", testVol.timestamp)
        }
        return writableDatabase.insert(TABLE_TEST_VOL, null, values)
    }

    fun deleteTestVol(id: Long) {
        writableDatabase.delete(TABLE_TEST_VOL, "id = ?", arrayOf(id.toString()))
    }

    // =========================================================================
    // Mappers privés strictement typés
    // =========================================================================

    private fun Cursor.toProfilMateriel(): ProfilMateriel {
        val latStr = getString(getColumnIndexOrThrow("lateralite"))
        val lateralite = runCatching { LateraliteArcher.valueOf(latStr) }
            .getOrDefault(LateraliteArcher.DROITIER)
        return ProfilMateriel(
            id = getLong(getColumnIndexOrThrow("id")),
            nomProfil = getString(getColumnIndexOrThrow("nom_profil")),
            modeleArc = getString(getColumnIndexOrThrow("modele_arc")),
            puissanceLivres = getDouble(getColumnIndexOrThrow("puissance_livres")),
            allongePouces = getDouble(getColumnIndexOrThrow("allonge_pouces")),
            tailleArcPouces = getInt(getColumnIndexOrThrow("taille_arc_pouces")),
            lateralite = lateralite,
            dateCreation = getLong(getColumnIndexOrThrow("date_creation"))
        )
    }

    private fun Cursor.toSetFleches(): SetFleches = SetFleches(
        id = getLong(getColumnIndexOrThrow("id")),
        nomLot = getString(getColumnIndexOrThrow("nom_lot")),
        modeleTube = getString(getColumnIndexOrThrow("modele_tube")),
        spine = getInt(getColumnIndexOrThrow("spine")),
        longueurPouces = getDouble(getColumnIndexOrThrow("longueur_pouces")),
        poidsPointeGrains = getInt(getColumnIndexOrThrow("poids_pointe_grains")),
        poidsTotalGrains = getInt(getColumnIndexOrThrow("poids_total_grains")),
        nombrePlumes = getInt(getColumnIndexOrThrow("nombre_plumes")),
        dateCreation = getLong(getColumnIndexOrThrow("date_creation"))
    )

    private fun Cursor.toJournalReglage(): JournalReglage = JournalReglage(
        id = getLong(getColumnIndexOrThrow("id")),
        profilMaterielId = getLong(getColumnIndexOrThrow("profil_materiel_id")),
        setFlechesId = getLong(getColumnIndexOrThrow("set_fleches_id")),
        bandMm = getDouble(getColumnIndexOrThrow("band_mm")),
        detalonnageMm = getDouble(getColumnIndexOrThrow("detalonnage_mm")),
        dureteBergerClics = getInt(getColumnIndexOrThrow("durete_berger_clics")),
        sortieBergerMm = getDouble(getColumnIndexOrThrow("sortie_berger_mm")),
        tillerDiffMm = getDouble(getColumnIndexOrThrow("tiller_diff_mm")),
        distanceTirMetres = getInt(getColumnIndexOrThrow("distance_tir_metres")),
        commentaire = getString(getColumnIndexOrThrow("commentaire")),
        timestamp = getLong(getColumnIndexOrThrow("timestamp"))
    )

    private fun Cursor.toTestVol(): TestVol {
        val typeStr = getString(getColumnIndexOrThrow("type_test"))
        val typeTest = runCatching { TypeTest.valueOf(typeStr) }.getOrDefault(TypeTest.FUT_NU)
        val symptomesStr = getString(getColumnIndexOrThrow("symptomes"))
        val symptomes = if (symptomesStr.isBlank()) {
            emptyList()
        } else {
            symptomesStr.split(",").mapNotNull { token ->
                runCatching { SymptomeVol.valueOf(token.trim()) }.getOrNull()
            }
        }
        return TestVol(
            id = getLong(getColumnIndexOrThrow("id")),
            journalReglageId = getLong(getColumnIndexOrThrow("journal_reglage_id")),
            typeTest = typeTest,
            impactHorizontal = getDouble(getColumnIndexOrThrow("impact_horizontal")),
            impactVertical = getDouble(getColumnIndexOrThrow("impact_vertical")),
            ecartHorizontalCm = getDouble(getColumnIndexOrThrow("ecart_horizontal_cm")),
            ecartVerticalCm = getDouble(getColumnIndexOrThrow("ecart_vertical_cm")),
            symptomes = symptomes,
            diagnosticResume = getString(getColumnIndexOrThrow("diagnostic_resume")),
            timestamp = getLong(getColumnIndexOrThrow("timestamp"))
        )
    }
}
