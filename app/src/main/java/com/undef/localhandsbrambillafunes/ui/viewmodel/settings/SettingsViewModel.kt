package com.undef.localhandsbrambillafunes.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica de la pantalla de configuración.
 *
 * Actúa como intermediario entre la UI y el repositorio de preferencias,
 * exponiendo datos reactivos y manejando operaciones asíncronas.
 *
 * @property userPreferencesRepository Repositorio que maneja las preferencias
 * del usuario relacionadas con la configuración.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    /**
     * Estado observable que representa la ubicación actual del usuario.
     *
     * Este [StateFlow] se alimenta directamente del [Flow] provisto por el repositorio.
     * Se utiliza `stateIn` para convertir un Flow frío en un flujo caliente,
     * adecuado para ser consumido de forma segura por la UI.
     *
     * - Permanece activo mientras existan suscriptores.
     * - Se detiene automáticamente tras 5 segundos sin observadores.
     * - Expone un valor inicial mientras se obtiene el dato persistido.
     */
    val userLocation: StateFlow<String> = userPreferencesRepository.userLocationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Rosario, Santa Fe" // Valor inicial mientras se carga el real
        )

    /**
     * Actualiza la ubicación del usuario.
     *
     * Lanza una corrutina en el [viewModelScope] para ejecutar
     * la operación de guardado de forma asíncrona y segura.
     *
     * @param newLocation Nueva ubicación ingresada por el usuario.
     */
    fun updateUserLocation(newLocation: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveUserLocation(newLocation)
        }
    }
}