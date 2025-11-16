package com.maximaaax.android.utils

import com.maximaaax.android.utils.infrastructure.ResourceExtractor
import java.io.File

object ScrcpyLauncher {
    private fun scrcpyExecutable(): File = ResourceExtractor.ensureBinaryAvailable("scrcpy")

    fun launch(serial: String, extraArgs: List<String> = emptyList()) {
        try {
            val scrcpyPath = scrcpyExecutable().absolutePath
            val scrcpyDir = File(scrcpyPath).parent
            
            // Construire la commande complète
            val cmd = mutableListOf(scrcpyPath, "-s", serial)
            cmd.addAll(extraArgs)
            
            // Sur macOS, lancer directement en arrière-plan sans terminal
            val processBuilder = ProcessBuilder(cmd)
                .directory(File(scrcpyDir))
            
            // Rediriger la sortie et les erreurs vers /dev/null pour ne pas voir le terminal
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
            
            // Lancer en arrière-plan
            val process = processBuilder.start()
            
            // Ne pas attendre la fin du processus - laisser scrcpy tourner indépendamment
            // Le processus continuera même si l'application se ferme
            
        } catch (e: Exception) {
            throw RuntimeException("Erreur lors du lancement de scrcpy: ${e.message}. Vérifiez que scrcpy est bien extrait dans ~/.androidutils/bin/", e)
        }
    }
}


