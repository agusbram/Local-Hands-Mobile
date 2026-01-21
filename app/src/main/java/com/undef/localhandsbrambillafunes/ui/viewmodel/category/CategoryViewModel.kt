package com.undef.localhandsbrambillafunes.ui.viewmodel.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * desde la capa de datos (a través del `ProductRepository`) a la UI.
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
}
