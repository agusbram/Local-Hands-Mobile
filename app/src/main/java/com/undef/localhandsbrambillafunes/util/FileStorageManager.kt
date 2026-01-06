package com.undef.localhandsbrambillafunes.util

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor centralizado para operaciones de almacenamiento de archivos
 * en el almacenamiento interno de la aplicación.
 *
 * Esta clase se encarga de persistir imágenes seleccionadas por el usuario
 * (por ejemplo, fotos de perfil) dentro del espacio privado de la app, garantizando:
 *
 * - Persistencia entre ejecuciones
 * - Acceso exclusivo de la aplicación
 * - Nombres de archivo únicos
 * - Limpieza controlada de archivos obsoletos
 *
 * Está anotada como [Singleton] para asegurar una única instancia
 * durante el ciclo de vida de la aplicación.
 *
 * @property context Contexto de aplicación inyectado por Hilt.
 */
@Singleton
class FileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Guarda una imagen proveniente de una URI de contenido
     * en el almacenamiento interno de la aplicación.
     *
     * El nombre del archivo se genera de forma única mediante UUID
     * para evitar colisiones o sobrescrituras accidentales.
     *
     * @param contentUri URI de contenido (por ejemplo, obtenida desde
     *                   un selector de imágenes del sistema).
     *
     * @return
     * Ruta absoluta del archivo guardado si la operación fue exitosa,
     * o `null` si ocurrió un error durante el proceso.
     *
     * @throws Exception
     * La excepción es capturada internamente; en caso de error,
     * se devuelve `null` y se registra el stacktrace.
     */
    fun saveImageToInternalStorage(contentUri: Uri): String? {
        return try {
            // Obtiene un flujo de entrada desde la URI de contenido
            val inputStream = context.contentResolver.openInputStream(contentUri) ?: return null

            // Crea un directorio para las imágenes de perfil si no existe
            val directory = File(context.filesDir, "profile_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Crea un nombre de archivo único para evitar colisiones
            val fileName = "profile_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            // Copia los datos del inputStream al nuevo archivo
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Devuelve la ruta absoluta del archivo guardado
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Elimina imágenes de perfil antiguas almacenadas en el
     * almacenamiento interno de la aplicación.
     *
     * Este método se utiliza para liberar espacio cuando el usuario
     * cambia su foto de perfil, manteniendo únicamente la imagen
     * actualmente en uso.
     *
     * La operación se ejecuta en el dispatcher [Dispatchers.IO].
     *
     * @param currentPhotoPath
     * Ruta absoluta de la imagen de perfil actualmente activa.
     * Esta imagen no será eliminada.
     */
    suspend fun cleanupOldProfileImages(currentPhotoPath: String?) {
        return withContext(Dispatchers.IO) {
            try {
                val directory = File(context.filesDir, "profile_images")
                if (!directory.exists()) return@withContext

                val files = directory.listFiles() ?: emptyArray()
                files.forEach { file ->
                    // Solo eliminar si no es la foto actual Y tiene más de 1 archivo
                    if (currentPhotoPath != file.absolutePath && files.size > 1) {
                        try {
                            file.delete()
                            Log.d("FileStorageManager", "Eliminada foto antigua: ${file.name}")
                        } catch (e: Exception) {
                            Log.e("FileStorageManager", "Error al eliminar ${file.name}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FileStorageManager", "Error en cleanupOldProfileImages", e)
            }
        }
    }
}
