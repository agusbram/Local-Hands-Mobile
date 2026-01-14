package com.undef.localhandsbrambillafunes.ui.viewmodel.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la gestión de productos.
 *
 * **Responsabilidad principal**: Mantiene y proporciona los datos de productos a la UI,
 * actuando como intermediario entre la capa de UI y el `ProductRepository`.
 * Sobrevive a cambios de configuración como rotaciones de pantalla.
 *
 * **Arquitectura**: Sigue el patrón MVVM y la estrategia de "Única Fuente de Verdad",
 * donde la UI observa un flujo de datos (`StateFlow`) que proviene directamente de la
 * base de datos local (Room).
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {

    /**
     * Flujo de estado que expone la lista completa de productos desde la base de datos local.
     * La UI observa este `StateFlow` para reaccionar a cualquier cambio en los datos.
     */
    val products: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Al crear el ViewModel, se inicia una sincronización con la API.
        fetchAndCacheProducts()
    }

    /**
     * Orquesta la obtención de productos desde la API y su guardado en la caché local (Room).
     */
    fun fetchAndCacheProducts() {
        viewModelScope.launch {
            repository.fetchAndCacheProducts()
        }
    }

    /**
     * Orquesta la creación de un nuevo producto, sincronizándolo con la API y guardándolo localmente.
     */
    fun addProductSyncApi(product: Product) {
        viewModelScope.launch {
            repository.addProductApi(product)
        }
    }

    /**
     * Orquesta la actualización de un producto existente, sincronizándolo con la API y actualizándolo localmente.
     */
    fun updateProductSyncApi(product: Product) {
        viewModelScope.launch {
            repository.updateProductApi(product)
        }
    }

    /**
     * Orquesta la eliminación de un producto, sincronizándolo con la API y eliminándolo localmente.
     */
    fun deleteProductSyncApi(product: Product) {
        viewModelScope.launch {
            repository.deleteProductApi(product)
        }
    }

    /**
     * Obtiene los productos que un usuario específico está vendiendo.
     */
    fun getMyProducts(ownerId: Int): StateFlow<List<Product>> =
        repository.getProductsByOwner(ownerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Obtiene la lista de productos marcados como favoritos por el usuario.
     */
    fun getFavorites(userId: Int): StateFlow<List<Product>> =
        repository.getFavoritesForUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Agrega un producto a la lista de favoritos del usuario.
     */
    fun addFavorite(productId: Int, userId: Int) = viewModelScope.launch {
        repository.addFavorite(userId, productId)
    }

    /**
     * Elimina un producto de la lista de favoritos del usuario.
     */
    fun removeFavorite(productId: Int, userId: Int) = viewModelScope.launch {
        repository.removeFavorite(userId, productId)
    }
}
