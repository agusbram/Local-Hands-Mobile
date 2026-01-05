package com.undef.localhandsbrambillafunes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.clear

/**
 * Extensión de [Context] que define una única instancia de [DataStore].
 *
 * El archivo donde se almacenan las preferencias se llamará "user_preferences".
 * Esta instancia se utiliza para guardar y leer datos persistentes del usuario.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repositorio encargado de gestionar las preferencias del usuario relacionadas
 * con la ubicación.
 *
 * Utiliza DataStore para almacenar datos de forma persistente y reactiva.
 *
 * @property context Contexto de la aplicación inyectado por Hilt.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * Objeto que contiene las claves utilizadas en DataStore.
     *
     * Cada clave representa una preferencia almacenada como par clave-valor.
     */
    private object PreferencesKeys {
        /**
         * Clave utilizada para almacenar la ubicación del usuario.
         */
        val USER_LOCATION = stringPreferencesKey("user_location")

        /**
         * Clave utilizada para almacenar el ID del usuario actualmente logueado.
         */
        val USER_ID = intPreferencesKey("user_id")
    }

    /**
     * Flujo reactivo que emite la ubicación actual del usuario.
     *
     * Cada vez que el valor almacenado en DataStore cambia,
     * este [Flow] emitirá automáticamente el nuevo valor.
     *
     * Si no existe una ubicación guardada, se devuelve el valor por defecto
     * "Rosario, Santa Fe".
     */
    val userLocationFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Leemos el valor. Si no existe, devolvemos un valor por defecto.
            preferences[PreferencesKeys.USER_LOCATION] ?: "Rosario, Santa Fe"
        }

    /**
     * Guarda la ubicación del usuario en DataStore (en el archivo de preferencias)
     *
     * Esta operación es segura y transaccional. Si ocurre un error,
     * DataStore garantiza la consistencia de los datos.
     *
     * @param location Ubicación del usuario a guardar.
     */
    suspend fun saveUserLocation(location: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_LOCATION] = location
        }
    }

    /**
     * Flujo reactivo que emite el ID del usuario logueado.
     *
     * Este Flow es fundamental para que otras partes de la app (como ProfileViewModel)
     * sepan qué usuario está activo.
     *
     * Si no hay ningún usuario logueado, emite -1 como valor por defecto.
     */
    val userIdFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID] ?: -1
        }

    /**
     * Guarda el ID del usuario que acaba de iniciar sesión.
     *
     * Se debe llamar a esta función después de un login exitoso.
     *
     * @param userId El ID del usuario a guardar.
     */
    suspend fun saveUserId(userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    /**
     * Limpia el ID del usuario guardado.
     *
     * Se debe llamar a esta función cuando el usuario cierra sesión (logout)
     * para asegurar que los datos del perfil no se muestren a la persona equivocada.
     */
    suspend fun clearUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_ID)
        }
    }

    /**
     * Limpia COMPLETAMENTE la sesión del usuario.
     * Elimina todas las preferencias guardadas (ID y email), lo que efectivamente
     * cierra la sesión y borra cualquier rastro del usuario logueado.
     */
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            // El método .clear() elimina TODAS las claves guardadas en este DataStore.
            // Es la forma más rápida y segura de garantizar un cierre de sesión completo.
            preferences.clear()
        }
    }

}