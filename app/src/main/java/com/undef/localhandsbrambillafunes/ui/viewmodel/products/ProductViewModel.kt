package com.undef.localhandsbrambillafunes.ui.viewmodel.products

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🧠 ViewModel — Encargado de gestionar y exponer datos a la capa de UI.
 *
 * ## Función principal:
 * Mantiene y proporciona los datos necesarios para la interfaz de usuario, incluso durante
 * cambios de configuración como rotaciones de pantalla.
 *
 * ## ¿Para qué sirve?
 * - 🔄 Recupera datos desde el `Repository` y los expone a la UI mediante `State`, `LiveData` o `StateFlow`.
 * - 🎯 Contiene la lógica de presentación (formateo, validación, control de estado).
 * - 🚫 No contiene lógica de negocio ni de acceso directo a la base de datos.
 * - 📦 Actúa como una capa intermedia que separa la UI de la lógica de datos, promoviendo una arquitectura limpia y mantenible.
 *
 * ## Beneficios:
 * - Mejora la organización del código.
 * - Facilita la reutilización y testeo.
 * - Hace que la UI sea más declarativa y reactiva.
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {
    // Estado interno y externo que expone la lista de productos
    private val _products = MutableStateFlow<List<Product>>(emptyList())

    /**
     * Estado observable de productos que expone una lista de productos a la UI.
     *
     * Se actualiza cuando se cargan productos desde la API.
     */
    val products: StateFlow<List<Product>> = _products

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
    init {
        syncProductsFromApi()
    }

    /**
     * Sincroniza los productos desde la API hacia la base de datos local.
     *
     * La operación se ejecuta dentro de [viewModelScope] para respetar
     * el ciclo de vida del ViewModel. Una vez completada la sincronización,
     * se recargan los productos desde el repositorio y se actualiza el
     * estado observable interno.
     */
    fun syncProductsFromApi() {
        viewModelScope.launch {
            repository.syncProductsWithApi()
            // Recargar productos después de sincronizar
            _products.value = repository.getAllProducts().firstOrNull() ?: emptyList()
        }
    }

    /**
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
     */
    fun addProductSyncApi(product: Product) = viewModelScope.launch {
        val createdProduct = repository.addProductWithSync(product)
        // Actualizar lista local
        _products.value = _products.value + createdProduct
    }

    /**
     * Actualiza un producto existente con sincronización hacia la API.
     *
     * Primero se intenta actualizar el producto de forma remota y local
     * a través del repositorio. Posteriormente, se actualiza la lista
     * de productos en memoria reemplazando el elemento modificado.
     *
     * @param product Producto con los datos actualizados.
     */
    fun updateProductSyncApi(product: Product) = viewModelScope.launch {
        repository.updateProductWithSync(product)
        // Actualizar en lista local
        _products.value = _products.value.map {
            if (it.id == product.id) product else it
        }
    }

    /**
     * Elimina un producto sincronizándolo con la API.
     *
     * La eliminación se delega al repositorio, que gestiona tanto la
     * eliminación remota como la local. Luego, el producto se remueve
     * del estado interno del ViewModel.
     *
     * @param product Producto a eliminar.
     */
    fun deleteProductSyncApi(product: Product) = viewModelScope.launch {
        repository.deleteProductWithSync(product)
        // Remover de lista local
        _products.value = _products.value.filter { it.id != product.id }
    }

    /**
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
     *
     * Esta función recupera una lista reactiva de productos que el usuario ha marcado como favoritos
     * y la expone como un [StateFlow], también inicializado de manera perezosa.
     *
     * @param userId ID del usuario del cual se desean obtener los productos favoritos.
     * @return Un [StateFlow] que contiene una lista de productos favoritos del usuario.
     */
    fun getFavorites(userId: Int): StateFlow<List<Product>> =
        repository.getFavoritesForUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}