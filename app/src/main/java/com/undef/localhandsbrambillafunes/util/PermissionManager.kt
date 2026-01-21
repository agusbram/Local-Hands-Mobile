package com.undef.localhandsbrambillafunes.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Objeto utilitario encargado de centralizar la gestión de permisos relacionados
 * con el acceso a imágenes, asegurando compatibilidad entre distintas versiones
 * de Android.
 *
 * Esta abstracción permite:
 * - Verificar si el permiso necesario ya fue concedido.
 * - Obtener dinámicamente el conjunto correcto de permisos a solicitar según
 *   la versión del sistema operativo.
 */
object PermissionManager {

    /**
     * Verifica si la aplicación cuenta con el permiso necesario para acceder
     * a imágenes almacenadas en el dispositivo.
     *
     * El comportamiento varía según la versión de Android:
     * - Android 13 (API 33) y superiores: utiliza
     *   [Manifest.permission.READ_MEDIA_IMAGES].
     * - Android 12 (API 32) y anteriores: utiliza
     *   [Manifest.permission.READ_EXTERNAL_STORAGE].
     *
     * @param context Contexto desde el cual se realiza la verificación del permiso.
     * @return `true` si el permiso requerido ha sido concedido, `false` en caso contrario.
     */
    fun hasImagePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 y anteriores
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Devuelve el conjunto de permisos que deben solicitarse en tiempo de ejecución
     * para acceder a imágenes, según la versión del sistema operativo.
     *
     * Comportamiento por versión:
     * - Android 14 (API 34) y superiores: solicita permisos para acceso a imágenes
     *   y selección visual específica del usuario.
     * - Android 13 (API 33): solicita únicamente
     *   [Manifest.permission.READ_MEDIA_IMAGES].
     * - Android 12 (API 32) y anteriores: solicita
     *   [Manifest.permission.READ_EXTERNAL_STORAGE].
     *
     * @return Un arreglo de [String] con los permisos requeridos para la versión
     *         actual de Android.
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ - Permiso para seleccionar fotos específicas
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            } else {
                // Android 13
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Android 12 y anteriores
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}