package com.undef.localhandsbrambillafunes.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream

/**
 * Copia el contenido de una URI (generalmente de la galería) a un archivo temporal
 * en el almacenamiento interno de la caché de la aplicación.
 *
 * @param context El contexto de la aplicación para acceder al contentResolver y al cacheDir.
 * @param uri La URI del archivo a copiar.
 * @return La ruta absoluta (String) del nuevo archivo creado, o null si la operación falla.
 */
fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "${System.currentTimeMillis()}_image.jpg")
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}