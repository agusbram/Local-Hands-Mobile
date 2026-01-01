package com.undef.localhandsbrambillafunes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
}