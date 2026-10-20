// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Déplace les dossiers de build hors du dossier synchronisé OneDrive pour éviter
// les erreurs Windows AccessDeniedException (verrouillage de fichiers .dex par OneDrive).
subprojects {
    layout.buildDirectory.set(
        file("${System.getProperty("user.home")}/.gradle/build-cache-local/Analyse-Tir/${project.name}/build")
    )
}
