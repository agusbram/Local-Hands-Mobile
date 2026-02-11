package com.undef.localhandsbrambillafunes.ui.viewmodel.products

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.repository.FavoriteRepository
import com.undef.localhandsbrambillafunes.data.repository.ProductRepository
import com.undef.localhandsbrambillafunes.data.repository.SellerRepository
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class que representa el estado completo de la pantalla de inicio.
 * @param favoriteProducts Un mapa donde la clave es el nombre de la categoría favorita y el valor es la lista de productos.
 * @param otherProducts Una lista con el resto de los productos que no pertenecen a categorías favoritas.
 */
data class HomeScreenState(
    val favoriteProducts: Map<String, List<Product>> = emptyMap(),
    val otherProducts: List<Product> = emptyList()
)

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
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val favoriteRepository: FavoriteRepository,
    private val sellerRepository: SellerRepository
) : ViewModel() {
    /**
     * Flujo interno mutable utilizado para notificar a la UI que debe
     * disparar el envío de un correo electrónico.
     *
     * Contiene un Pair donde:
     * - first: Lista de destinatarios (correos electrónicos).
     * - second: Contenido o mensaje del correo.
     *
     * Se inicializa en null para indicar que no hay evento pendiente.
     */
    private val _emailNotificationEvent = MutableStateFlow<Pair<List<String>, String>?>(null)

    /**
     * Exposición inmutable del evento hacia la UI.
     *
     * La interfaz de usuario debe observar este StateFlow para reaccionar
     * cuando se publique un nuevo evento de notificación por correo.
     */
    val emailNotificationEvent = _emailNotificationEvent.asStateFlow()

    /**
     * Restablece el evento de notificación de correo.
     *
     * Debe llamarse después de que la UI haya procesado el evento,
     * evitando que se vuelva a ejecutar de forma accidental
     * (por ejemplo, tras recomposición).
     */
    fun resetEmailEvent() {
        _emailNotificationEvent.value = null
    }


    // Estado interno y externo que expone la lista de productos
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val userPreferencesRepository: UserPreferencesRepository // Inyectamos el repo de preferencias
) : ViewModel() {

    /**
     * Flujo de estado que expone el estado completo y estructurado de la Home Screen.
     * Combina dos flujos: la lista total de productos y las categorías favoritas del usuario.
     * Cada vez que uno de los dos flujos cambia, este se recalcula automáticamente.
     */
    val homeScreenState: StateFlow<HomeScreenState> = combine(
        repository.getAllProducts(),
        userPreferencesRepository.favoriteCategoriesFlow
    ) { allProducts, favoriteCategories ->
        if (favoriteCategories.isEmpty()) {
            // Si no hay favoritas, todos los productos van a la lista "otherProducts"
            HomeScreenState(otherProducts = allProducts)
        } else {
            // Si hay favoritas, separamos los productos
            val (favorites, others) = allProducts.partition { it.category in favoriteCategories }
            // Agrupamos los favoritos por su categoría
            val groupedFavorites = favorites.groupBy { it.category }
            HomeScreenState(favoriteProducts = groupedFavorites, otherProducts = others)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeScreenState() // Estado inicial vacío
    )

    /**
     * Estado observable de productos que expone una lista de productos a la UI.
     * Se mantiene por compatibilidad con otras pantallas que lo puedan necesitar.
     */
    val products: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    /**
     * Resultados de la búsqueda reactiva de productos.
     */
    val searchResults: StateFlow<List<Product>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllProducts()
            } else {
                repository.searchProducts(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Inicializa el ViewModel ejecutando automáticamente la sincronización
     * de productos con la API al momento de su creación.
     */
    init {
        syncProductsFromApi()
    }

    /**
     * Sincroniza los productos desde la API hacia la base de datos local.
     */
    fun syncProductsFromApi() {
        viewModelScope.launch {
            repository.syncProductsWithApi()
        }
    }

    /**
     * Actualiza el texto de búsqueda de productos.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /**
     * Agrega un nuevo producto sincronizándolo con la API y actualiza el estado local.
     *
     * El flujo de ejecución es el siguiente:
     * 1. Envía el producto al repositorio para su creación remota y persistencia local.
     * 2. Una vez creado, lo agrega a la lista interna mantenida por el ViewModel.
     * 3. Si el producto pertenece a un vendedor:
     *    - Obtiene los correos electrónicos de los usuarios que han marcado
     *      como favorito algún producto de ese vendedor.
     *    - Recupera el nombre del emprendimiento del vendedor.
     *    - Si existen destinatarios, emite un evento para que la UI dispare
     *      el envío de un correo electrónico.
     *
     * El envío real del correo no se realiza aquí; el ViewModel únicamente
     * expone un evento observable para que la interfaz lo gestione.
     *
     * @param product Producto a crear y sincronizar con la API.
     */
    fun addProductSyncApi(product: Product) = viewModelScope.launch {
        val createdProduct = repository.addProductWithSync(product)

        // --- LOGICA DE NOTIFICACION ---
        val sellerId = product.ownerId
        if (sellerId != null) {
            // 1. Obtener emails de interesados en este vendedor
            val emails = favoriteRepository.getEmailsOfUsersInterestedInSeller(sellerId)

            // 2. Obtener el nombre del emprendimiento
            val seller = sellerRepository.getSellerById(sellerId).firstOrNull()
            val entrepreneurship = seller?.entrepreneurship ?: "Nuestro Emprendimiento"

            if (emails.isNotEmpty()) {
                // Disparamos el evento para que la UI abra el Intent
                _emailNotificationEvent.value = Pair(emails, entrepreneurship)
            }
        }
        _products.value = _products.value + createdProduct
    }

    /**
     * Actualiza un producto existente con sincronización hacia la API.
     */
    fun updateProductSyncApi(product: Product) = viewModelScope.launch {
        repository.updateProductWithSync(product)
    }

    /**
     * Elimina un producto sincronizándolo con la API.
     */
    fun deleteProductSyncApi(product: Product) = viewModelScope.launch {
        repository.deleteProductWithSync(product)
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