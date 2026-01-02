package com.undef.localhandsbrambillafunes.ui.viewmodel.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.exception.NotAuthenticatedException
import com.undef.localhandsbrambillafunes.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsable de gestionar la lógica relacionada con los productos favoritos.
 *
 * Esta clase se encarga de interactuar con el repositorio `FavoriteRepository` para
 * realizar operaciones como agregar un producto a la lista de favoritos de un usuario.
 * Utiliza `viewModelScope` para ejecutar tareas asincrónicas de forma segura dentro del ciclo de vida del ViewModel.
 *
 * @param favoriteRepository Instancia del repositorio que proporciona acceso a los datos de favoritos.
 */
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val _favorites = MutableStateFlow<List<Product>>(emptyList())
    val favorites: StateFlow<List<Product>> = _favorites


    /**
     * Agrega un producto a la lista de favoritos de un usuario específico.
     *
     * Esta función lanza una corrutina en el `viewModelScope` para realizar la operación
     * de forma asincrónica, evitando bloquear el hilo principal.
     *
     * @param productId ID del producto que se desea marcar como favorito.
     */
    fun addFavorite(productId: Int) {
        viewModelScope.launch {
            favoriteRepository.addFavoriteForCurrentUser(productId)
        }
    }


    /**
     * Elimina un elemento de favoritos según el ID del producto proporcionado.
     *
     * Esta función inicia una coroutine en el [viewModelScope], lo que garantiza que la operación
     * se realice de manera asíncrona y segura dentro del ciclo de vida del ViewModel.
     *
     * Internamente, delega la operación de eliminación al [favoriteRepository], que contiene
     * la lógica de acceso a datos.
     *
     * @param productId El ID del producto cuyo favorito debe eliminarse.
     */
    fun removeFavoriteByProductId(productId: Int) = viewModelScope.launch {
        favoriteRepository.removeFavorite(productId)
    }


    /**
     * Carga la lista de productos marcados como favoritos por el usuario autenticado.
     *
     * Esta función inicia una coroutine dentro del [viewModelScope] para recopilar de manera
     * asíncrona los datos de favoritos desde el [favoriteRepository]. Los resultados obtenidos
     * se asignan al `LiveData` o `StateFlow` interno [_favorites] para ser observados por la UI.
     *
     * En caso de que el usuario no esté autenticado, se captura una excepción [NotAuthenticatedException],
     * la cual puede ser utilizada para notificar al usuario o redirigir al flujo de autenticación.
     */
    fun loadFavorites() {
        viewModelScope.launch {
            try {
                favoriteRepository.getFavoritesForUser()
                    .collect { list ->
                        _favorites.value = list
                    }
            } catch (e: NotAuthenticatedException) {
                print(e.message)
            }
        }
    }
}