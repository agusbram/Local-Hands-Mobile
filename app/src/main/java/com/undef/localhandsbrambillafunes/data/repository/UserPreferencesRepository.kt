package com.undef.localhandsbrambillafunes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

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

        /**
         * Clave utilizada para almacenar el emprendimiento del vendedor actualmente logueado.
         */
        val USER_ENTREPRENEURSHIP = stringPreferencesKey("user_entrepreneurship")

        /**
         * Clave para almacenar el conjunto de categorías favoritas del usuario.
         */
        val FAVORITE_CATEGORIES = stringSetPreferencesKey("favorite_categories")
    }

    /**
     * Flujo reactivo que expone las categorías favoritas del usuario.
     * Emite un conjunto de strings. Si no hay nada guardado, emite un conjunto vacío.
     */
    val favoriteCategoriesFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FAVORITE_CATEGORIES] ?: emptySet()
        }

    /**
     * Guarda el conjunto de categorías favoritas del usuario.
     * @param categories Un conjunto de strings con los nombres de las categorías.
     */
    suspend fun saveFavoriteCategories(categories: Set<String>) {
        context.dataStore.edit {
            it[PreferencesKeys.FAVORITE_CATEGORIES] = categories
        }
    }

    /**
     * Flujo reactivo que expone el nombre del emprendimiento del usuario almacenado en DataStore.
     *
     * Este Flow emite un nuevo valor cada vez que cambia la preferencia
     * [PreferencesKeys.USER_ENTREPRENEURSHIP]. Si no existe un valor almacenado,
     * se devuelve una cadena vacía.
     *
     * Resulta útil para observar cambios en tiempo real desde ViewModels o UI
     * (por ejemplo, con collectAsState en Jetpack Compose).
     */
    val userEntrepreneurshipFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ENTREPRENEURSHIP] ?: ""
        }

    /**
     * Guarda o actualiza el nombre del emprendimiento del usuario en DataStore.
     *
     * @param entrepreneurship Nombre del emprendimiento a persistir.
     */
    suspend fun saveUserEntrepreneurship(entrepreneurship: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ENTREPRENEURSHIP] = entrepreneurship
        }
    }

    /**
     * Obtiene el nombre del emprendimiento del usuario de forma síncrona
     * (sin exponer un Flow).
     *
     * Este método es útil en casos puntuales donde no se desea observar cambios
     * reactivos, sino obtener el valor una sola vez.
     *
     * @return El nombre del emprendimiento almacenado o una cadena vacía
     * si no existe.
     */
    suspend fun getUserEntrepreneurship(): String {
        return context.dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.USER_ENTREPRENEURSHIP] ?: ""
            }
            .firstOrNull() ?: ""
    }

    /**
     * Elimina el nombre del emprendimiento del usuario almacenado en DataStore.
     *
     * Normalmente se utiliza en procesos de cierre de sesión o limpieza
     * de datos del usuario.
     */
    suspend fun clearUserEntrepreneurship() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_ENTREPRENEURSHIP)
        }
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