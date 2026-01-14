package com.undef.localhandsbrambillafunes.ui.viewmodel.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
<<<<<<< HEAD
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
=======
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
>>>>>>> eeb38f6049920a4c5cd753055d6fec65abb2085e
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

<<<<<<< HEAD
=======
    /**
     * Obtiene el estado reactivo en tiempo real de lo que se escribe en la barra de búsqueda
     * de productos.
     * */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    /**
     * Resultados de la búsqueda reactiva de productos.
     *
     * Este [StateFlow] emite en tiempo real la lista de productos que coincide
     * con el texto ingresado en la barra de búsqueda.
     *
     * El flujo aplica un `debounce` de 300 ms para evitar ejecutar búsquedas
     * innecesarias mientras el usuario escribe rápidamente.
     *
     * - Si el texto de búsqueda está vacío o contiene solo espacios en blanco,
     *   se emiten todos los productos almacenados localmente.
     * - Si el texto contiene contenido válido, se ejecuta una búsqueda filtrada
     *   a través del repositorio.
     *
     * El flujo se mantiene activo mientras existan suscriptores y se cancela
     * automáticamente tras 5 segundos sin observadores, según la política
     * [SharingStarted.WhileSubscribed].
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults = _searchQuery
        .debounce(300) // Evita buscar en cada letra si el usuario escribe rápido
        .flatMapLatest { query ->
            if (query.isBlank()) {
                // Muestra todos los productos de Room si está vacía la busqueda
                repository.getAllProducts()
            } else {
                // Llama al DAO con la query múltiple
                repository.searchProducts(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Inicializa el ViewModel ejecutando automáticamente la sincronización
     * de productos con la API al momento de su creación.
     *
     * Esta llamada asegura que la base de datos local y el estado en memoria
     * comiencen alineados con la información remota.
     */
>>>>>>> eeb38f6049920a4c5cd753055d6fec65abb2085e
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
<<<<<<< HEAD
     * Orquesta la creación de un nuevo producto, sincronizándolo con la API y guardándolo localmente.
=======
     * Actualiza el texto de búsqueda de productos.
     *
     * Esta función modifica el valor del flujo interno de consulta de búsqueda,
     * lo que desencadena automáticamente la ejecución de una nueva búsqueda
     * reactiva y la actualización de [searchResults].
     *
     * @param newQuery Nuevo texto ingresado por el usuario en la barra de búsqueda.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /**
     * Agrega un nuevo producto sincronizándolo con la API.
     *
     * El producto se envía al repositorio, que gestiona la creación remota
     * y el guardado local. Luego, el producto creado se agrega a la lista
     * local mantenida por el ViewModel.
     *
     * @param product Producto a crear.
>>>>>>> eeb38f6049920a4c5cd753055d6fec65abb2085e
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
<<<<<<< HEAD
     * Orquesta la eliminación de un producto, sincronizándolo con la API y eliminándolo localmente.
     */
    fun deleteProductSyncApi(product: Product) {
        viewModelScope.launch {
            repository.deleteProductApi(product)
        }
    }

    /**
     * Obtiene los productos que un usuario específico está vendiendo.
=======
     * Agrega un producto a la lista de favoritos del usuario actualmente autenticado.
     *
     * Este método utiliza el `userId` asociado al `ViewModel` para crear la relación
     * en la tabla de favoritos y lo ejecuta de forma asincrónica.
     *
     * @param productId ID del producto a marcar como favorito.
     */
    fun addFavorite(productId: Int, userId: Int) = viewModelScope.launch {
        repository.addFavorite(userId, productId)
    }

    /**
     * Elimina un producto de la lista de favoritos del usuario actual.
     *
     * La eliminación se basa en el `userId` y `productId` para identificar la relación
     * y ejecuta la operación de forma segura dentro del `viewModelScope`.
     *
     * @param productId ID del producto que se desea eliminar de favoritos.
     */
    fun removeFavorite(productId: Int, userId: Int) = viewModelScope.launch {
        repository.removeFavorite(userId, productId)
    }

    /**
     * Obtiene los productos que el usuario con el ID especificado está vendiendo.
     *
     * Esta función recupera una lista reactiva de productos pertenecientes al usuario
     * y la expone como un [StateFlow] que se inicializa de manera perezosa.
     *
     * @param ownerId ID del usuario del cual se desean obtener los productos.
     * @return Un [StateFlow] que contiene una lista de productos publicados por el usuario.
>>>>>>> eeb38f6049920a4c5cd753055d6fec65abb2085e
     */
    fun getMyProducts(ownerId: Int): StateFlow<List<Product>> =
        repository.getProductsByOwner(ownerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    /**
     * Obtiene un producto específico a partir de su identificador.
     *
     * Esta función expone un flujo reactivo del producto solicitado,
     * convirtiendo el [Flow] proporcionado por el repositorio en un [StateFlow].
     *
     * El [StateFlow] se mantiene activo mientras existan suscriptores y se
     * cancela automáticamente tras 5 segundos sin observadores, de acuerdo
     * con la política [SharingStarted.WhileSubscribed].
     *
     * Si el producto no existe, el flujo emitirá `null`.
     *
     * @param productId Identificador único del producto a obtener.
     * @return Un [StateFlow] que emite el [Product] correspondiente o `null`
     * si no se encuentra disponible.
     */
    fun getProduct(productId: Int): StateFlow<Product?> {
        return repository.getProductById(productId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

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
