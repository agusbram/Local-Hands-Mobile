package com.undef.localhandsbrambillafunes.ui.viewmodel.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel para la pantalla de gestión de categorías.
 *
 * **Responsabilidad principal**: Exponer la lista de categorías de productos disponibles
 * y las listas de productos filtrados por categoría a la UI.
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    /**
     * Flujo de estado que expone la lista de nombres de categorías únicos desde la base de datos.
     *
     * La UI observará este `StateFlow` para mostrar las categorías. El flujo es reactivo,
     * por lo que cualquier cambio en las categorías de los productos se reflejará aquí.
     */
    val categories: StateFlow<List<String>> = productRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Obtiene una lista reactiva de productos filtrados por una categoría específica.
     *
     * @param category El nombre de la categoría por la cual filtrar.
     * @return Un [StateFlow] que emite la lista de productos para esa categoría.
     */
    fun getProductsByCategory(category: String): StateFlow<List<Product>> {
        return productRepository.getProductsByCategory(category)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}