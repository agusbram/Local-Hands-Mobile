package com.undef.localhandsbrambillafunes.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
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
    private val productRepository: ProductRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // --- Ubicación del Usuario ---

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
            initialValue = ""
        )

    /**
     * Latitud de la ubicación seleccionada por el usuario.
     * Se utiliza para cálculos de proximidad de productos.
     * Se expone como StateFlow desde el repositorio para reactividad.
     */
    val userLatitude: StateFlow<Double> = userPreferencesRepository.userLatitudeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    /**
     * Longitud de la ubicación seleccionada por el usuario.
     * Se utiliza para cálculos de proximidad de productos.
     * Se expone como StateFlow desde el repositorio para reactividad.
     */
    val userLongitude: StateFlow<Double> = userPreferencesRepository.userLongitudeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    /**
     * Actualiza la ubicación del usuario, incluyendo las coordenadas geográficas.
     *
     * Lanza una corrutina en el [viewModelScope] para ejecutar
     * la operación de guardado de forma asíncrona y segura.
     *
     * @param newLocation Nueva ubicación ingresada por el usuario (ej: "Buenos Aires, Argentina").
     * @param latitude Latitud de la ubicación (coordenada geográfica).
     * @param longitude Longitud de la ubicación (coordenada geográfica).
     */
    fun updateUserLocation(newLocation: String, latitude: Double = 0.0, longitude: Double = 0.0) {
        viewModelScope.launch {
            userPreferencesRepository.saveUserLocation(newLocation)
            userPreferencesRepository.saveUserCoordinates(latitude, longitude)
        }
    }

    // --- Categorías Favoritas ---

    /**
     * Flujo que expone la lista de todas las categorías disponibles en la app.
     */
    val allCategories: StateFlow<List<String>> = productRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Flujo que expone el conjunto de categorías que el usuario ha marcado como favoritas.
     */
    val favoriteCategories: StateFlow<Set<String>> = userPreferencesRepository.favoriteCategoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    /**
     * Actualiza las categorías favoritas del usuario.
     * Añade o elimina una categoría del conjunto guardado en DataStore.
     * @param categoryName El nombre de la categoría a modificar.
     * @param isSelected Si la categoría debe ser añadida o eliminada.
     */
    fun updateFavoriteCategory(categoryName: String, isSelected: Boolean) {
        viewModelScope.launch {
            val currentFavorites = favoriteCategories.value.toMutableSet()
            if (isSelected) {
                currentFavorites.add(categoryName)
            } else {
                currentFavorites.remove(categoryName)
            }
            userPreferencesRepository.saveFavoriteCategories(currentFavorites)
        }
    }
}